package com.foxmike.android.activities;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chris on 2018-08-22.
 */

public class FoxmikeApplication extends Application {
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;
}
