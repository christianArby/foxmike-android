package com.foxmike.android.activities;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chris on 2018-08-22.
 */

public class FoxmikeApplication extends Application implements LifecycleObserver{
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
    private static boolean activityVisible;

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void appInResumeState() {
        activityVisible = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void appInPauseState() {
        activityVisible = false;
    }
}