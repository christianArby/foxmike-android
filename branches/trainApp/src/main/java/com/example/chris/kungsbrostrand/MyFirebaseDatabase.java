package com.example.chris.kungsbrostrand;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by chris on 2017-07-21.
 */

public class MyFirebaseDatabase extends Service{

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    private Location currentLocation;
    private GeoFire geoFire;
    private TreeMap<Integer,String> nearSessions;
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");



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
                    user.setTrainerMode(userDb.isTrainerMode());
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

    public void filterSessions(final OnSessionsFilteredListener onSessionsFilteredListener,final HashMap<String,Boolean> firstWeekdayHashMap,final HashMap<String,Boolean> secondWeekdayHashMap, Activity activity) {

        FusedLocationProviderClient mFusedLocationClient;

        geoFire = new GeoFire(mGeofireDbRef);
        nearSessions = new TreeMap<Integer,String>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);


        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            currentLocation = location;
                            // ...

                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 3000);

                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    //Any location key which is within 3000km from the user's location will show up here as the key parameter in this method
                                    //You can fetch the actual data for this location by creating another firebase query here

                                    String distString = getDistance(location.latitude,location.longitude, currentLocation);
                                    Integer dist = Integer.parseInt(distString);
                                    nearSessions.put(dist,key);
                                }

                                @Override
                                public void onKeyExited(String key) {}

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {}

                                @Override
                                public void onGeoQueryReady() {




                                    final ArrayList<Session> sessions = new ArrayList<Session>();

                                    for (final Integer str : nearSessions.keySet()) {

                                        dbRef.child("sessions").child(nearSessions.get(str)).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Session session;
                                                session = dataSnapshot.getValue(Session.class);

                                                if (firstWeekdayHashMap.containsKey(session.textSDF(session.getSessionDate()))) {
                                                    if (firstWeekdayHashMap.get(session.textSDF(session.getSessionDate()))) {
                                                        sessions.add(session);
                                                    } else {
                                                        nearSessions.remove(str);
                                                    }
                                                }

                                                if (secondWeekdayHashMap.containsKey(session.textSDF(session.getSessionDate()))) {

                                                    if (secondWeekdayHashMap.get(session.textSDF(session.getSessionDate()))) {
                                                        sessions.add(session);
                                                    } else {
                                                        nearSessions.remove(str);
                                                    }
                                                }

                                                if (!firstWeekdayHashMap.containsKey(session.textSDF(session.getSessionDate())) && !secondWeekdayHashMap.containsKey(session.textSDF(session.getSessionDate()))) {
                                                    nearSessions.remove(str);
                                                }

                                                if (sessions.size() == nearSessions.size()) {///////TODO //KOLLA DETTA
                                                    onSessionsFilteredListener.OnSessionsFiltered(sessions,location);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                }
                            });
                        }
                    }
                });
    }

    private String getDistance(double latitude, double longitude, Location currentLocation){

        Location locationA = new Location("point A");
        locationA.setLatitude(currentLocation.getLatitude());
        locationA.setLongitude(currentLocation.getLongitude());
        Location locationB = new Location("point B");
        locationB.setLatitude(latitude);
        locationB.setLongitude(longitude);
        float distance = locationA.distanceTo(locationB);
        float b = (float)Math.round(distance);
        String distanceString = Float.toString(b).replaceAll("\\.?0*$", "");
        return  distanceString;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
