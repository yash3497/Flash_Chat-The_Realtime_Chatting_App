package com.example.flashchat2.Models;

public class Memes {
    private String uid,imageurl,memeid;

    public Memes() {}

    public Memes(String uid,  String imageurl, String memeid) {
        this.uid = uid;
        this.imageurl = imageurl;
        this.memeid = memeid;
    }

    public String getMemeid() {
        return memeid;
    }

    public void setMemeid(String memeid) {
        this.memeid = memeid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
