package com.ilumastech.smart_attendance_system.list_classes;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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

    public static class DateComparator implements Comparator<Notification>
    {
        @Override
        public int compare(Notification o1, Notification o2) {

            // sorting notifications according to date time
            Date date1 = null;
            Date date2 = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.US);
                date1 = dateFormat.parse(o2.getDateTime());
                date2 = dateFormat.parse(o1.getDateTime());
            } catch (ParseException ignored) {
            }
            Log.d("DateComparator", date1 + " -> " + date2 + " : " + (Objects.requireNonNull(date1).compareTo(date2)));
            return (Objects.requireNonNull(date1).compareTo(date2));
        }
    }
}
