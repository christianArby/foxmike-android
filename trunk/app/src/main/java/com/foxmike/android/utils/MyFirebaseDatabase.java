package com.foxmike.android.utils;
// Checked
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
import com.foxmike.android.interfaces.OnNearSessionsFoundListener;
import com.foxmike.android.interfaces.OnNearStudiosFoundListener;
import com.foxmike.android.interfaces.OnSessionBranchesFoundListener;
import com.foxmike.android.interfaces.OnSessionsFilteredListener;
import com.foxmike.android.interfaces.OnSessionsFoundListener;
import com.foxmike.android.interfaces.OnStudioBranchesFoundListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.interfaces.OnUsersFoundListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.models.SessionMap;
import com.foxmike.android.models.Studio;
import com.foxmike.android.models.StudioBranch;
import com.foxmike.android.models.User;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by chris on 2017-07-21.
 */

public class MyFirebaseDatabase extends Service {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private HashMap<String,Integer> sessionDistances = new HashMap<>();
    private ArrayList<String> nearSessionIdsArray = new ArrayList<>();
    private TreeMap<Integer, String> nearStudioIDs = new TreeMap<Integer, String>();
    private Location currentLocation;
    private GeoFire geoFire;
    private SessionMap sessionMap;
    private int studioDownloadedCounter = 0;
    private HashMap<String,Integer> studioDistances = new HashMap<>();

    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");

    public void getSessions(final OnSessionsFoundListener onSessionsFoundListener, final HashMap<String, Long> sessionsHashMap) {

        final ArrayList<Session> sessions = new ArrayList<Session>();
        for (String key : sessionsHashMap.keySet()) {
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

    public void getSessionBranches(final HashMap<String, Long> sessionsHashMap, final OnSessionBranchesFoundListener onSessionBranchesFoundListener) {

        final ArrayList<SessionBranch> sessionBranches = new ArrayList<SessionBranch>();
        for (String key : sessionsHashMap.keySet()) {
            dbRef.child("sessions").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Session session;
                    session = dataSnapshot.getValue(Session.class);
                    SessionBranch sessionBranch = new SessionBranch(dataSnapshot.getKey(),session);
                    sessionBranches.add(sessionBranch);
                    if (sessionBranches.size() == sessionsHashMap.size()) {
                        onSessionBranchesFoundListener.OnSessionBranchesFound(sessionBranches);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void getStudioBranches(final HashMap<String, Boolean> studioHashMap, final OnStudioBranchesFoundListener onStudioBranchesFoundListener) {

        final ArrayList<StudioBranch> studioBranches = new ArrayList<StudioBranch>();
        for (String key : studioHashMap.keySet()) {
            dbRef.child("studiosTEST").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Studio studio;
                    studio = dataSnapshot.getValue(Studio.class);
                    StudioBranch studioBranch = new StudioBranch(dataSnapshot.getKey(),studio);
                    studioBranches.add(studioBranch);
                    if (studioBranches.size() == studioHashMap.size()) {
                        onStudioBranchesFoundListener.OnStudioBranchesFound(studioBranches);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void getCurrentUser(final OnUserFoundListener onUserFoundListener) {
        dbRef.child("users").child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userDb = dataSnapshot.getValue(User.class);
                onUserFoundListener.OnUserFound(userDb);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUsers(final HashMap<String, Boolean> usersHashMap, final OnUsersFoundListener onUsersFoundListener) {
        final ArrayList<User> users = new ArrayList<User>();
        for (String key : usersHashMap.keySet()) {
            dbRef.child("users").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user;
                    user = dataSnapshot.getValue(User.class);
                    users.add(user);
                    if (users.size() == usersHashMap.size()) {
                        onUsersFoundListener.OnUsersFound(users);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void filterSessions(ArrayList<Session> nearSessions, ArrayList<ArrayList<Session>> nearSessionsArrays,final HashMap<String, Boolean> firstWeekdayHashMap, final HashMap<String, Boolean> secondWeekdayHashMap, String sortType, final OnSessionsFilteredListener onSessionsFilteredListener) {

        ArrayList<Session> sessions = new ArrayList<>();
        Long currentTimestamp = System.currentTimeMillis();

        // filter listArray
        for (Session nearSession : nearSessions) {
            if (firstWeekdayHashMap.containsKey(nearSession.supplyTextTimeStamp().textSDF())) {
                if (firstWeekdayHashMap.get(nearSession.supplyTextTimeStamp().textSDF())) {
                    if (nearSession.getSessionTimestamp() > currentTimestamp) {
                        sessions.add(nearSession);
                    }
                }
            }

            if (secondWeekdayHashMap.containsKey(nearSession.supplyTextTimeStamp().textSDF())) {
                if (secondWeekdayHashMap.get(nearSession.supplyTextTimeStamp().textSDF())) {
                    if (nearSession.getSessionTimestamp() > currentTimestamp) {
                        sessions.add(nearSession);
                    }
                }
            }
        }
        // filter mapsArray
        for (ArrayList<Session> nearSessionArray : nearSessionsArrays) {
            for (Session nearSession: nearSessionArray) {
                if (!firstWeekdayHashMap.containsKey(nearSession.supplyTextTimeStamp().textSDF())) {
                    if (!firstWeekdayHashMap.containsKey(nearSession.supplyTextTimeStamp().textSDF())) {
                        if (nearSession.getSessionTimestamp() < currentTimestamp) {
                            nearSessionsArrays.get(nearSessionsArrays.indexOf(nearSessionArray)).remove(nearSession);
                        }
                    }
                }

                if (!secondWeekdayHashMap.containsKey(nearSession.supplyTextTimeStamp().textSDF())) {
                    if (!secondWeekdayHashMap.containsKey(nearSession.supplyTextTimeStamp().textSDF())) {
                        if (nearSession.getSessionTimestamp() < currentTimestamp) {
                            nearSessionsArrays.get(nearSessionsArrays.indexOf(nearSessionArray)).remove(nearSession);
                        }
                    }
                }

            }
        }

        // filter listArray
        if (sortType.equals("date")) {
            Collections.sort(sessions);
            TextTimestamp prevTextTimestamp = new TextTimestamp();
            HashMap<Integer, Session> headerSessions = new HashMap<>();

            int i = 0;
            while (i < sessions.size()) {
                if (!prevTextTimestamp.textSDF().equals(sessions.get(i).supplyTextTimeStamp().textSDF())) {
                    Session dummySession = new Session();
                    dummySession.setImageUrl("dateHeader");
                    dummySession.setSessionTimestamp(sessions.get(i).getSessionTimestamp());
                    sessions.add(i, dummySession);
                    prevTextTimestamp = sessions.get(i).supplyTextTimeStamp();
                }
                i++;
            }
        }

        onSessionsFilteredListener.OnSessionsFiltered(sessions, nearSessionsArrays);
    }

    public void getNearSessions(Activity activity, final int distanceRadius, final OnNearSessionsFoundListener onNearSessionsFoundListener) {
        FusedLocationProviderClient mFusedLocationClient;
        geoFire = new GeoFire(mGeofireDbRef);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            currentLocation = location;
                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), distanceRadius);
                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    //Any location key which is within distanceRadius from the user's location will show up here as the key parameter in this method
                                    //You can fetch the actual data for this location by creating another firebase query here
                                    String distString = getDistance(location.latitude, location.longitude, currentLocation);
                                    Integer dist = Integer.parseInt(distString);

                                    sessionDistances.put(key, dist);
                                }

                                @Override
                                public void onKeyExited(String key) {
                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {
                                }

                                @Override
                                public void onGeoQueryReady() {
                                    final ArrayList<Session> sessions = new ArrayList<Session>();
                                    final ArrayList<SessionMap> sessionMapArrayList = new ArrayList<SessionMap>();
                                    ArrayList<ArrayList<Session>> sessionArrayArraylist = new ArrayList<>();
                                    for (String sessionId : sessionDistances.keySet()) {

                                        dbRef.child("sessions").child(sessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Session session;
                                                Long currentTimestamp = System.currentTimeMillis();
                                                session = dataSnapshot.getValue(Session.class);

                                                SessionMap sessionMap = new SessionMap(session, sessionDistances.get(dataSnapshot.getKey()));
                                                sessionMapArrayList.add(sessionMap);
                                                if (sessionMapArrayList.size() == sessionDistances.size()) {
                                                    Collections.sort(sessionMapArrayList);
                                                    int previousDistance = 0;

                                                    ArrayList<Session> prevSession = new ArrayList<>();

                                                    for (SessionMap sessionMapSorted: sessionMapArrayList) {
                                                        // If the session is in the future
                                                        if (sessionMapSorted.getSession().getSessionTimestamp()>currentTimestamp) {
                                                            // Add it to the sessionArray
                                                            sessions.add(sessionMapSorted.getSession());
                                                            // If the distance is the same as the previous session save it in the same array
                                                            if (sessionMapSorted.getDistance()==previousDistance) {
                                                                prevSession.add(sessionMapSorted.getSession());
                                                            } else {
                                                                // If not save the previous array
                                                                if (prevSession.size()>0) {
                                                                    sessionArrayArraylist.add(prevSession);
                                                                    prevSession = new ArrayList<>();
                                                                }
                                                                // Clear the sessionArray and put this session in it
                                                                prevSession.add(sessionMapSorted.getSession());
                                                            }
                                                            // Remember this distance to the next one
                                                            previousDistance = sessionMapSorted.getDistance();
                                                        }
                                                    }
                                                    if (prevSession.size()>0) {
                                                        sessionArrayArraylist.add(prevSession);
                                                    }
                                                    onNearSessionsFoundListener.OnNearSessionsFound(sessions, location, sessionArrayArraylist);
                                                }


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                    if (sessionDistances.size() < 1) {
                                        onNearSessionsFoundListener.OnNearSessionsFound(sessions, location, sessionArrayArraylist);
                                    }
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                }
                            });
                        } else {
                            onNearSessionsFoundListener.OnLocationNotFound();
                        }
                    }
                });
    }

    /*public void getNearStudiosAndSessions(Activity activity, final int distanceRadius, final OnNearSessionsFoundListener onNearSessionsFoundListener) {
        FusedLocationProviderClient mFusedLocationClient;
        geoFire = new GeoFire(mGeofireDbRef);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        *//*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*//*
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            currentLocation = location;
                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), distanceRadius);
                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    //Any location key which is within distanceRadius from the user's location will show up here as the key parameter in this method
                                    //You can fetch the actual data for this location by creating another firebase query here
                                    String distString = getDistance(location.latitude, location.longitude, currentLocation);
                                    Integer dist = Integer.parseInt(distString);
                                    studioDistances.put(key,dist);
                                    nearStudioIDs.put(dist, key);
                                }
                                @Override
                                public void onKeyExited(String key) {
                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {
                                }

                                @Override
                                public void onGeoQueryReady() {
                                    studioDownloadedCounter = 0;

                                    for (final Integer str : nearStudioIDs.keySet()) {
                                        dbRef.child("studiosTEST").child(nearStudioIDs.get(str)).child("sessions").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                HashMap<String, Long> studioSessions;
                                                studioDownloadedCounter++;
                                                if (dataSnapshot.getValue()!=null) {
                                                    studioSessions = (HashMap<String, Long>) dataSnapshot.getValue();
                                                    Long currentTimestamp = System.currentTimeMillis();
                                                    for (String sessionId: studioSessions.keySet()) {
                                                        if (studioSessions.get(sessionId)>currentTimestamp) {
                                                            nearSessionIdsArray.add(sessionId);
                                                        }
                                                    }
                                                }
                                                if (studioDownloadedCounter == nearStudioIDs.size()) {
                                                    final ArrayList<Session> sessions = new ArrayList<Session>();
                                                    final ArrayList<SessionMap> sessionMapArrayList = new ArrayList<SessionMap>();
                                                    for (String sessionId : nearSessionIdsArray) {
                                                        dbRef.child("sessions").child(sessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Session session;
                                                                session = dataSnapshot.getValue(Session.class);
                                                                SessionMap sessionMap = new SessionMap(session, studioDistances.get(session.getStudioId()));
                                                                sessionMapArrayList.add(sessionMap);
                                                                if (sessionMapArrayList.size() == nearSessionIdsArray.size()) {
                                                                    Collections.sort(sessionMapArrayList);
                                                                    for (SessionMap sessionMapSorted: sessionMapArrayList) {
                                                                        sessions.add(sessionMapSorted.getSession());
                                                                    }
                                                                    onNearSessionsFoundListener.OnNearSessionsFound(sessions, location);
                                                                }
                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                            }
                                                        });
                                                    }
                                                    if (nearSessionIdsArray.size() < 1) {
                                                        onNearSessionsFoundListener.OnNearSessionsFound(sessions, location);
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                    if (nearStudioIDs.size() < 1) {
                                        final ArrayList<Session> sessions = new ArrayList<Session>();
                                        onNearSessionsFoundListener.OnNearSessionsFound(sessions, location);
                                    }
                                }
                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                }
                            });
                        } else {
                            onNearSessionsFoundListener.OnLocationNotFound();
                        }
                    }
                });
    }*/

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
