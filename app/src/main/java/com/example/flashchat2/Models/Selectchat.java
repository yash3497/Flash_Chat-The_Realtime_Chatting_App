package com.example.flashchat2.Models;
public class Selectchat implements java.io.Serializable{
    String username,about,profileimageurl,uid,phoneNumber;
    Boolean isSelected= false;

    public Selectchat(String username, String about, String profileimageurl, String uid, String phoneNumber, Boolean isSelected) {
        this.username = username;
        this.about = about;
        this.profileimageurl = profileimageurl;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.isSelected = isSelected;
    }

    public Selectchat(){}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getProfileimageurl() {
        return profileimageurl;
    }

    public void setProfileimageurl(String profileimageurl) {
        this.profileimageurl = profileimageurl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}

