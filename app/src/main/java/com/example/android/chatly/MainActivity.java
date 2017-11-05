package com.example.android.chatly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    private ListView messageListView;
    private ImageButton imageButton;
    private EditText editTextField;
    private Button sendButton;

    private String username;
    private ArrayList<Message> messageList;
    private ArrayAdapter<Message> messageAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //I've split the code into many small methods to simplify the onCreate method
        // and increase readability
        initializeAllInstanceVariables();
        addListenerToEditText();
        attachClickListenerToSendButton();
        initializeAndSetArrayAdapter();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initializeAllInstanceVariables()
    {
        messageListView = findViewById(R.id.messageListView);
        imageButton = findViewById(R.id.photoPickerButton);
        editTextField = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        username = ANONYMOUS;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("messages");
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
                //messageAdapter.add(new Message(textInEditText, username, null));
                editTextField.setText("");
            }
        });
    }

    private void initializeAndSetArrayAdapter()
    {
        messageList = new ArrayList<>();
        messageAdapter = new ArrayAdapter<>(this, R.layout.message_item, messageList);
        messageListView.setAdapter(messageAdapter);
    }

}
