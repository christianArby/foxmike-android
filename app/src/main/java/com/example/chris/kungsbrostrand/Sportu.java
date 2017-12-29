package com.example.chris.kungsbrostrand;

import android.app.Application;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

/**
 * Created by chris on 2017-08-16.
 */

public class Sportu extends Application {



    private DatabaseReference rootDbRef;
    private FirebaseAuth mAuth;



    @Override
    public void onCreate() {
        super.onCreate();



        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        rootDbRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();

        final DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        final DatabaseReference currentUserRef = rootDbRef.child("/presence/" + currentUserID);

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)){
                    currentUserRef.setValue(true);
                    currentUserRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });

    }

}
