package com.ilumastech.smart_attendance_system;

import android.app.ProgressDialog;
import android.content.Context;

class Progress {

    private Context context;
    private ProgressDialog progressDialog;

    Progress(Context context) {
        this.context = context;
    }

    void showProgressDialog(String msg) {

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setIndeterminate(true);
        }

        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
