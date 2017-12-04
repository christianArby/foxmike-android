package com.example.chris.kungsbrostrand;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chris on 2017-08-16.
 */

public class Sportu extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
