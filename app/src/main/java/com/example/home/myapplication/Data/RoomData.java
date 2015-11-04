package com.example.home.myapplication.Data;

/**
 * Created by HOME on 2015-09-15.
 */
public class RoomData {
    public String name,email,profile_src,text;

    public String getName() {return name;}

    public String getEmail() {return email;}

    public String getText() {
        return text;
    }

    public RoomData(String email, String text) {
        this.text = text;
        this.email = email;
    }
}
