package com.ilumastech.smart_attendance_system.main_activities.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ilumastech.smart_attendance_system.R;
import com.ilumastech.smart_attendance_system.list_classes.ClassRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassListAdapter extends ArrayAdapter<ClassRoom> {

    private List<ClassRoom> classRoomList = new ArrayList<>();

    public ClassListAdapter(Context context, int i) {
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

        if (classRoom != null) {
            viewHolder.class_name.setText(classRoom.getClass_Name());

            // if attendance date is empty
            if (classRoom.getAttendance_Date() == null)
                viewHolder.attendance_id.setText(Objects.requireNonNull(classRoom).getAttendance_Id());

            // if attendance date is not empty
            else {

                // if there is no attendance date yet
                if (classRoom.getAttendance_Date().isEmpty())
                    viewHolder.attendance_id.setText("No attendance taken yet.");

                    // if there is attendance date yet
                else
                    viewHolder.attendance_id.setText(Objects.requireNonNull(classRoom).getAttendance_Date());
            }
        }
        return view;
    }

    public void clearList()
    {
        classRoomList.clear();
        notifyDataSetChanged();
    }

    private static class ClassViewHolder {
        TextView class_name;
        TextView attendance_id;
    }
}