package com.foxmike.android.utils;
// Checked
import android.app.Application;
import android.content.Intent;

import com.foxmike.android.activities.LoginActivity;
import com.foxmike.android.activities.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by chris on 2017-08-16.
 */

public class Sportu extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        String test = "test";

    }
}
