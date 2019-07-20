package com.ilumastech.smart_attendance_system.list_classes;

public class Notification {

    private String msg;
    private String dateTime;
    private String className;
    private String id;

    public Notification(String msg, String dateTime, String className, String id) {
        this.msg = msg;
        this.dateTime = dateTime;
        this.className = className;
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
