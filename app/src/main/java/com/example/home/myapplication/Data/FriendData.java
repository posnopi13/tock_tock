package com.example.home.myapplication.Data;

/**
 * Created by HOME on 2015-09-14.
 */
public class FriendData {
    public String name,email,profile_src;
    public int seq;
    public String getName() {return name;}

    public String getEmail() {
        return email;
    }

    //public String getProfile_src() { return profile_src; }

    public FriendData(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
