package com.codeseven.pos.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.codeseven.pos.R;

public class ProgressDialog {
    private Activity activity;
    private AlertDialog alertDialog;


    public ProgressDialog(Activity activity) {
        this.activity = activity;
    }


    @SuppressLint("InflateParams")
    void StartLoadingdialog()
    {

        AlertDialog.Builder  builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_progress_dialog,null));
        builder.setCancelable(true);

        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    void dismissDialog()
    {
        if(alertDialog!=null) {
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    }

}
