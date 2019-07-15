package com.ilumastech.smart_attendance_system;

import java.util.ArrayList;
import java.util.List;

public class AttendaneRecord {

    private String date;
    private List<String> studentsId;

    public AttendaneRecord() {
    }

    public AttendaneRecord(String date, String studentId) {
        this.date = date;
        this.studentsId = new ArrayList<>();
        this.studentsId.add(studentId);
    }

    public AttendaneRecord(String date, List<String> studentsId) {
        this.date = date;
        this.studentsId = studentsId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getStudentsId() {
        return studentsId;
    }

    public void setStudentsId(List<String> studentsId) {
        this.studentsId = studentsId;
    }
}
