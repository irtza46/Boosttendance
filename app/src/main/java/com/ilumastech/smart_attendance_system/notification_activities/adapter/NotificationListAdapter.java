package com.ilumastech.smart_attendance_system.notification_activities.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.list_classes.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationListAdapter extends ArrayAdapter<Notification> {

    private List<Notification> notificationList = new ArrayList<>();

    public NotificationListAdapter(Context context, int i) {
        super(context, i);
    }

    @Override
    public void add(Notification object) {
        notificationList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.notificationList.size();
    }

    @Override
    public Notification getItem(int index) {
        return this.notificationList.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        com.ilumastech.smart_attendance_system.notification_activities.adapter.NotificationListAdapter.NotificationView viewHolder;

        // if view is not created already
        if (view == null) {

            // setting layout of view
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.notification_card, parent, false);

            // creating view holder
            viewHolder = new com.ilumastech.smart_attendance_system.notification_activities.adapter.NotificationListAdapter.NotificationView();
            viewHolder.class_name = view.findViewById(R.id.class_name);
            viewHolder.id = view.findViewById(R.id.id);
            viewHolder.dateTime = view.findViewById(R.id.date_time);

            // setting view tag
            view.setTag(viewHolder);
        }

        // if view is already created
        else
            viewHolder = (com.ilumastech.smart_attendance_system.notification_activities.adapter.NotificationListAdapter.NotificationView) view.getTag();

        // getting notification
        Notification notification = getItem(position);

        // setting values in view
        if (notification != null) {
            viewHolder.class_name.setText(notification.getClassName());
            viewHolder.id.setText(notification.getId());
            viewHolder.dateTime.setText(notification.getDateTime());
        }
        return view;
    }

    private static class NotificationView {
        TextView class_name;
        TextView id;
        TextView dateTime;
    }
}

