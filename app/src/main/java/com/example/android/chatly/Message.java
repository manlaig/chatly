package com.example.android.chatly;

/**
 * Created by manlai on 11/4/2017.
 */

public class Message
{
    private String text;
    private String name;
    private String photoURL;

    public Message() { }

    public Message(String text, String name, String photoURL)
    {
        this.text = text;
        this.name = name;
        this.photoURL = photoURL;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
}
