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

    private static final String TAG = "ClassArrayAdapter";
    private List<ClassRoom> classRoomList = new ArrayList<>();

    ClassArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
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
        View row = convertView;
        ClassViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.class_card, parent, false);
            viewHolder = new ClassViewHolder();
            viewHolder.class_name = row.findViewById(R.id.class_name);
            viewHolder.attendance_id = row.findViewById(R.id.attendance_id);
            row.setTag(viewHolder);
        } else
            viewHolder = (ClassViewHolder) row.getTag();
        ClassRoom ClassRoom = getItem(position);
        viewHolder.class_name.setText(Objects.requireNonNull(ClassRoom).getClassName());
        viewHolder.attendance_id.setText(Objects.requireNonNull(ClassRoom).getAttendanceId());
        return row;
    }

    private static class ClassViewHolder {
        TextView class_name;
        TextView attendance_id;
    }
}