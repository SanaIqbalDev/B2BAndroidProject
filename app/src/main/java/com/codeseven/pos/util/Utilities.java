package com.codeseven.pos.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.codeseven.pos.MainApplication;

public class Utilities {
    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MainApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();

    }
}
