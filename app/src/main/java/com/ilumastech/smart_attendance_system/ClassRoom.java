package com.ilumastech.smart_attendance_system;

import java.io.Serializable;

public class ClassRoom implements Serializable {

    private String className;
    private String classId;
    private String attendanceId;

    public ClassRoom() {
    }

    public ClassRoom(String className, String classId, String attendanceId) {
        this.className = className;
        this.classId = classId;
        this.attendanceId = attendanceId;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
