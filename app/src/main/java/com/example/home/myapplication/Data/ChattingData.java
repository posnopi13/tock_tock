package com.example.home.myapplication.Data;

/**
 * Created by HOME on 2015-09-13.
 */
public class ChattingData {
    public String name,message,profile_src;

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getProfile_src() {
        return profile_src;
    }

    public ChattingData(String name, String message, String profile_src) {
        this.name = name;
        this.message = message;
        this.profile_src = profile_src;
    }
    public ChattingData(String name, String message) {
        this.name = name;
        this.message = message;
    }
}
