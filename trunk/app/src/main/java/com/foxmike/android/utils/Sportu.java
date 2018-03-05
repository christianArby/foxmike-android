package com.foxmike.android.utils;

import android.app.Application;
import android.content.Intent;

import com.foxmike.android.activities.LoginActivity;
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



    private DatabaseReference rootDbRef;
    private FirebaseAuth mAuth;



    @Override
    public void onCreate() {
        super.onCreate();



        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()==null) {
            Intent loginIntent = new Intent(Sportu.this, LoginActivity.class);
            //mainPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        } else {

            String currentUserID = mAuth.getCurrentUser().getUid();

            // since I can connect from multiple devices, we store each connection instance separately
            // any time that connectionsRef's value is null (i.e. has no children) I am offline
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myConnectionsRef = database.getReference("presence/"+currentUserID+"/connections");

            // stores the timestamp of my last disconnect (the last time I was seen online)
            final DatabaseReference lastOnlineRef = database.getReference("/presence/"+currentUserID+"/lastOnline");

            final DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        DatabaseReference con = myConnectionsRef.push();

                        // when this device disconnects, remove it
                        con.onDisconnect().removeValue();

                        // when I disconnect, update the last time I was seen online
                        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                        // add this device to my connections list
                        // this value could contain info about the device or a timestamp too
                        con.setValue(Boolean.TRUE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Listener was cancelled at .info/connected");
                }
            });
        }
    }

}
