package com.example.chris.kungsbrostrand;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by chris on 2017-06-28.
 */

public class User {


    public ArrayList<String> sessionsAttending;
    public ArrayList<String> sessionsHosting;
    DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public User(ArrayList<String> sessionsAttending, ArrayList<String> sessionsHosting) {
        this.sessionsAttending = sessionsAttending;
        this.sessionsHosting = sessionsHosting;
    }



    public User() {
        this.sessionsAttending = new ArrayList<String>();
        this.sessionsHosting = new ArrayList<String>();
    }

/*    public ArrayList<String> getSessionsAttending() {
        usersDbRef.child(currentFirebaseUser.getUid()).child("sessionsAttending").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user.sessionsAttending.add(snapshot.getKey().toString());
                }
                sessionsAttending =user.sessionsAttending;
                //populateSessionArray(user.sessions);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return sessionsAttending;
    }*/

    public ArrayList<String> getSessionsHosting() {
        return sessionsHosting;
    }
}
