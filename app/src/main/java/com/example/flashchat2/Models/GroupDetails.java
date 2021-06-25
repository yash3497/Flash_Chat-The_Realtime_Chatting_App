package com.example.flashchat2.Models;

import android.util.Log;

public class GroupDetails {
    String GroupName,GroupId,GroupAbout,GroupImage,timestamp,createdBy,role,Uid;
    public GroupDetails(){}

    public GroupDetails(String groupName, String groupId, String groupAbout, String groupImage, String timestamp, String createdBy, String role, String uid) {
        GroupName = groupName;
        GroupId = groupId;
        GroupAbout = groupAbout;
        GroupImage = groupImage;
        this.timestamp = timestamp;
        this.createdBy = createdBy;
        this.role = role;
        Uid = uid;
    }
    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public String getGroupAbout() {
        return GroupAbout;
    }

    public void setGroupAbout(String groupAbout) {
        GroupAbout = groupAbout;
    }

    public String getGroupImage() {
        return GroupImage;
    }

    public void setGroupImage(String groupImage) {
        GroupImage = groupImage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
}
