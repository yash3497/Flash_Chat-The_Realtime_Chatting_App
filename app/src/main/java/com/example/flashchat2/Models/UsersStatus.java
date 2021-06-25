package com.example.flashchat2.Models;

import java.util.ArrayList;

public class UsersStatus {
    private  String name,profileImage,uid;
    private Long lastUpdated;
    private ArrayList<Status> statuses;

    public UsersStatus() {
    }

    public UsersStatus(String name, String profileImage, String uid, Long lastUpdated, ArrayList<Status> statuses) {
        this.name = name;
        this.profileImage = profileImage;
        this.uid = uid;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }
}
