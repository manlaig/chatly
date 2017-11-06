package com.example.android.chatly;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    //request code sent using FirebaseUI for signing in
    public static final int RC_SIGN_IN = 1;
    //request code for opening the photo picker when the photoPicker button is clicked
    public static final int RC_PHOTO_PICKER = 2;

    private ListView messageListView;
    private ImageButton imageButton;
    private EditText editTextField;
    private Button sendButton;

    private String username;
    private ArrayList<Message> messageList;
    private MessageAdapter messageAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ChildEventListener childEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //I've split the code into many small methods to simplify the onCreate method
        // and increase readability
        initializeInstanceVariables();
        initializeAndSetArrayAdapter();
        initializeFirebaseVariables();
        authorizeAndManageStates();
        addListenerToEditText();
        attachClickListenerToSendButton();
        setClickListenerOnPhotoPicker();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //When we override this method, we can display our custom menu on the screen
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //This method called when the user opens the menu and presses sign out
        switch(item.getItemId())
        {
            case R.id.sign_out_button:
                AuthUI.getInstance().signOut(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN)
        {
            if(resultCode == RESULT_CANCELED)
                finish();
            else
                Toast.makeText(this, "Successfully signed it!", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK)
        {
            storePhotoAndDisplay(data);
        }
    }

    private void initializeInstanceVariables()
    {
        messageListView = findViewById(R.id.messageListView);
        imageButton = findViewById(R.id.photoPickerButton);
        editTextField = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
    }


    private void initializeAndSetArrayAdapter()
    {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.message_item, messageList);
        messageListView.setAdapter(messageAdapter);
    }


    private void initializeFirebaseVariables()
    {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = firebaseDatabase.getReference().child("messages");
        storageReference = firebaseStorage.getReference().child("chat-photos");
    }


    private void addListenerToEditText()
    {
        editTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0)
                    sendButton.setEnabled(true);
                else
                    sendButton.setEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }


    private void attachClickListenerToSendButton()
    {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textInEditText = editTextField.getText().toString();
                databaseReference.push().setValue(new Message(textInEditText, username, null));
                editTextField.setText("");
            }
        });
    }


    private void setClickListenerOnPhotoPicker()
    {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete Action Using"), RC_PHOTO_PICKER);
            }
        });
    }


    private void attachListenerToDatabaseReference() {
        //This method is called when the user is signed in and authorized

        //This method attaches an EventListener to 'Messages' reference in the database,
        //so that whenever a new message gets added to database, we get notified. And we display it

        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message tempMessage = dataSnapshot.getValue(Message.class);
                    messageAdapter.add(tempMessage);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            databaseReference.addChildEventListener(childEventListener);
        }
    }


    private void authorizeAndManageStates()
    {
        //This method implements the AuthStateListener interface and attaches it to firebaseAuth
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //Overriding this method returns either a value if signed in and NULL if not signed it
                if(user != null)
                    onSignedInInitialize(user.getDisplayName());
                else
                {
                    onSignedOutCleanUp();

                    //if not signed in, we use FirebaseUI to display a sign in screen
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);
    }


    private void onSignedInInitialize(String username)
    {
        this.username = username;
        attachListenerToDatabaseReference();
    }


    private void onSignedOutCleanUp()
    {
        username = ANONYMOUS;
        messageAdapter.clear();
    }


    private void storePhotoAndDisplay(Intent data)
    {
        Uri downloadUri = data.getData();
        Message tempMessage = new Message(null, username, downloadUri.toString());
        databaseReference.push().setValue(tempMessage);
    }

}
