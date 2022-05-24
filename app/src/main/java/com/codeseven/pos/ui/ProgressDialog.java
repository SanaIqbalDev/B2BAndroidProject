package com.codeseven.pos.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.codeseven.pos.R;

public class ProgressDialog {
    private Activity activity;
    private AlertDialog alertDialog;
    private AlertDialog cacheAlertDialog;
    private ProgressBar cacheProgressBar;


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

    void StartCachingDialog(){
        AlertDialog.Builder  builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View cacheView =  inflater.inflate(R.layout.loading_cache_dialog,null);
        builder.setView(cacheView);
        cacheProgressBar = cacheView.findViewById(R.id.progressBarCache);
        builder.setCancelable(true);

        cacheAlertDialog = builder.create();
        cacheAlertDialog.setCancelable(false);

        cacheAlertDialog.show();

    }
    void setDialogMessage(String message)
    {
        alertDialog.setMessage(message);
    }
    void setProgressValueCacheDialog(final int progress) {

        // set the progress
        cacheProgressBar.setProgress(progress);
        // thread is used to change the progress value
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                setProgressValue(progress + 10);
//            }
//        });
//        thread.start();
    }

    void dismissDialog()
    {
        if(alertDialog!=null) {
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    }
    void dismissCacheDialog(){
        if(cacheAlertDialog!=null) {
            if (cacheAlertDialog.isShowing())
                cacheAlertDialog.dismiss();
        }
    }

}
