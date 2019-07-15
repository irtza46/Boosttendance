package com.ilumastech.smart_attendance_system;

import java.util.List;

public class User {

    private String uid;
    private String fullName;
    private String email;
    private String accType;
    private String phoneNumber;
    private Classes classes;

    public User() {
    }

    public User(String uid, String fullName, String email, String accType, String phoneNumber,
                List<ClassRoom> createdClassRooms, List<ClassRoom> joinedClassRooms) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.accType = accType;
        this.phoneNumber = phoneNumber;
        this.classes = new Classes(createdClassRooms, joinedClassRooms);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<ClassRoom> getJoinedClasses() {
        return classes.getJoined();
    }

    public void setJoinedClasses(List<ClassRoom> joinedClasses) {
        this.classes.setJoined(joinedClasses);
    }

    public List<ClassRoom> getCreatedClasses() {
        return classes.getCreated();
    }

    public void setCreatedClasses(List<ClassRoom> createdClasses) {
        this.classes.setCreated(createdClasses);
    }
}

class Classes {
    private List<ClassRoom> created;
    private List<ClassRoom> joined;

    public Classes() {
    }

    Classes(List<ClassRoom> created, List<ClassRoom> joined) {
        this.created = created;
        this.joined = joined;
    }

    List<ClassRoom> getCreated() {
        return created;
    }

    void setCreated(List<ClassRoom> created) {
        this.created = created;
    }

    List<ClassRoom> getJoined() {
        return joined;
    }

    void setJoined(List<ClassRoom> joined) {
        this.joined = joined;
    }
}