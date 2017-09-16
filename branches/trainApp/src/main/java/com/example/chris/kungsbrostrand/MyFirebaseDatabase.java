package com.example.chris.kungsbrostrand;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by chris on 2017-07-21.
 */

public class MyFirebaseDatabase {

    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public void getSessions(final OnSessionsFoundListener onSessionsFoundListener, final HashMap<String,Boolean> sessionsHashMap) {

        final ArrayList<Session> sessions = new ArrayList<Session>();

        for ( String key : sessionsHashMap.keySet() ) {
            dbRef.child("sessions").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Session session;
                    session = dataSnapshot.getValue(Session.class);
                    sessions.add(session);
                    if (sessions.size() == sessionsHashMap.size()) {
                        onSessionsFoundListener.OnSessionsFound(sessions);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void getSessionsFiltered(final OnSessionsFoundListener onSessionsFoundListener, final TreeMap<Integer,String> nearSessions,final HashMap<String,Boolean> weekdayHashMap) {

        final ArrayList<Session> sessions = new ArrayList<Session>();



        for (final Integer str : nearSessions.keySet()) {

            dbRef.child("sessions").child(nearSessions.get(str)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Session session;
                    session = dataSnapshot.getValue(Session.class);

                    if (weekdayHashMap.get(session.textDay(session.sessionDate))) {
                        sessions.add(session);
                    } else {
                        nearSessions.remove(str);
                    }

                    if (sessions.size() == nearSessions.size()) {
                        onSessionsFoundListener.OnSessionsFound(sessions);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }



    public void getUser(final OnUserFoundListener onUserFoundListener){
        dbRef.child("users").child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = new User();
                User userDb = dataSnapshot.getValue(User.class);
                if (userDb!=null) {
                    if (userDb.sessionsHosting != null) {
                        user.setSessionsHosting(userDb.sessionsHosting); //FIXA DETTA ´, FULT
                    }
                    if (userDb.sessionsAttending != null) {
                        user.setSessionsAttending(userDb.sessionsAttending); //FIXA DETTA ´, FULT
                    }

                    user.setUserName(userDb.name);
                    user.setUserImageURL(userDb.image);
                }
                onUserFoundListener.OnUserFound(user);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //final User user = new User();
                //onUserFoundListener.OnUserFound(user);
            }
        });
    }
}
