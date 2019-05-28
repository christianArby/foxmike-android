package com.foxmike.android.activities;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

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
        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/

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
