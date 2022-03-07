package com.codeseven.pos;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MainApplication extends Application {

    private static MainApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        int memClass = ((ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getLargeMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 4;

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        builder.memoryCache(new LruCache(cacheSize));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);

    }

    public static MainApplication getContext() {
        return mContext;
    }
}
