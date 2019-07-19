package com.ilumastech.smart_attendance_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassArrayAdapter extends ArrayAdapter<ClassRoom> {

    private List<ClassRoom> classRoomList = new ArrayList<>();

    ClassArrayAdapter(Context context, int i) {
        super(context, i);
    }

    @Override
    public void add(ClassRoom object) {
        classRoomList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.classRoomList.size();
    }

    @Override
    public ClassRoom getItem(int index) {
        return this.classRoomList.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ClassViewHolder viewHolder;

        // if view is not created already
        if (view == null) {

            // setting layout of view
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.class_card, parent, false);

            // creating view holder
            viewHolder = new ClassViewHolder();
            viewHolder.class_name = view.findViewById(R.id.class_name);
            viewHolder.attendance_id = view.findViewById(R.id.attendance_id);

            // setting view tag
            view.setTag(viewHolder);
        }

        // if view is already created
        else
            viewHolder = (ClassViewHolder) view.getTag();

        // getting class room
        ClassRoom classRoom = getItem(position);
        viewHolder.class_name.setText(Objects.requireNonNull(classRoom).getClass_Name());

        // if attendance date is empty
        if (classRoom.getAttendace_Date() == null)
            viewHolder.attendance_id.setText(Objects.requireNonNull(classRoom).getAttendance_Id());

        // if attendance date is not empty
        else
            viewHolder.attendance_id.setText(Objects.requireNonNull(classRoom).getAttendace_Date());
        return view;
    }

    private static class ClassViewHolder {
        TextView class_name;
        TextView attendance_id;
    }
}