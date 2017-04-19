package com.example.chris.kungsbrostrand;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by chris on 2017-04-19.
 */

public class Tag extends Application {
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
    }
}
