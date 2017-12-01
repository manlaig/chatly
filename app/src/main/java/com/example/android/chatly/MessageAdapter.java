package com.example.android.chatly;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by manlai on 11/5/2017.
 */

public class MessageAdapter extends ArrayAdapter<Message>
{
    private View viewToConvertToMessage;
    private TextView text, name;
    private ImageView image;
    private Message message;
    private boolean hasPhoto;


    public MessageAdapter(Context context, int resource, ArrayList<Message> objects)
    {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        viewToConvertToMessage = convertView;
        inflateViewToMessageItem(parent);
        initializeMessageFields();

        message = getItem(position);
        hasPhoto = message.getPhotoURL() != null;

        drawMessage();

        return viewToConvertToMessage;
    }


    private void inflateViewToMessageItem(ViewGroup parent)
    {
        if(viewToConvertToMessage == null)
            viewToConvertToMessage = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item, parent, false);
    }


    private void initializeMessageFields()
    {
        text = viewToConvertToMessage.findViewById(R.id.text_field);
        name = viewToConvertToMessage.findViewById(R.id.name_field);
        image = viewToConvertToMessage.findViewById(R.id.image_field);
    }


    private void drawMessage()
    {
        if(hasPhoto)
        {
            text.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            Glide.with(image.getContext()).load(message.getPhotoURL()).into(image);
        }
        else
        {
            text.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
            text.setText(message.getText());
        }
        name.setText(message.getName());
    }
}
