package com.ilumastech.smart_attendance_system.list_classes;

import java.io.Serializable;

public class ClassRoom implements Serializable {

    private String class_Id;
    private String class_Name;
    private String u_Id;
    private String attendance_Id;
    private String email;
    private String attendance_Date;

    public ClassRoom(String class_Id, String class_Name, String u_Id, String attendance_Id, String email) {
        this.class_Id = class_Id;
        this.class_Name = class_Name;
        this.u_Id = u_Id;
        this.attendance_Id = attendance_Id;
        this.email = email;
    }

    public ClassRoom(String class_Id, String class_Name, String attendance_Date) {
        this.class_Id = class_Id;
        this.class_Name = class_Name;
        this.attendance_Date = attendance_Date;
    }

    public String getClass_Id() {
        return class_Id;
    }

    public void setClass_Id(String class_Id) {
        this.class_Id = class_Id;
    }

    public String getClass_Name() {
        return class_Name;
    }

    public void setClass_Name(String class_Name) {
        this.class_Name = class_Name;
    }

    public String getU_Id() {
        return u_Id;
    }

    public void setU_Id(String u_Id) {
        this.u_Id = u_Id;
    }

    public String getAttendance_Id() {
        return attendance_Id;
    }

    public void setAttendance_Id(String attendance_Id) {
        this.attendance_Id = attendance_Id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAttendance_Date() {
        return attendance_Date;
    }

    public void setAttendace_Date(String attendance_Date) {
        this.attendance_Date = attendance_Date;
    }
}
