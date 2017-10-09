package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;

public class WeekdayFilterFragment extends Fragment {

    public WeekdayFilterFragment() {
        // Required empty public constructor
    }
    DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    ToggleButton toggleButton1;
    ToggleButton toggleButton2;
    ToggleButton toggleButton3;
    ToggleButton toggleButton4;
    ToggleButton toggleButton5;
    ToggleButton toggleButton6;
    ToggleButton toggleButton7;
    public HashMap<String,Boolean> weekdayHashMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    GeoFire geoFire;
    TreeMap<Integer,String> nearSessions;
    View inflatedView;
    ArrayList<Session> sessionsClose = new ArrayList<Session>();
    private OnSessionsFilteredListener onSessionsFilteredListener;

    public static WeekdayFilterFragment newInstance() {
        WeekdayFilterFragment fragment = new WeekdayFilterFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        weekdayHashMap = new HashMap<String,Boolean>();
        Session dummySession = new Session();
        Calendar cal = Calendar.getInstance();
        SessionDate todaysSessionDate = new SessionDate(cal);

        for(int i=1; i<8; i++){
            weekdayHashMap.put(dummySession.textDay(todaysSessionDate), true);
            todaysSessionDate.day = todaysSessionDate.day +1;
        }

        this.inflatedView = inflater.inflate(R.layout.fragment_weekday_filter, container, false);

        toggleButton1 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton1);
        toggleButton1.setText(dummySession.textDay(todaysSessionDate));
        toggleButton1.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton1.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton1.setChecked(true);
        toggleButton2 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton2);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton2.setText(dummySession.textDay(todaysSessionDate));
        toggleButton2.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton2.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton2.setChecked(true);
        toggleButton3 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton3);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton3.setText(dummySession.textDay(todaysSessionDate));
        toggleButton3.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton3.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton3.setChecked(true);
        toggleButton4 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton4);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton4.setText(dummySession.textDay(todaysSessionDate));
        toggleButton4.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton4.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton4.setChecked(true);
        toggleButton5 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton5);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton5.setText(dummySession.textDay(todaysSessionDate));
        toggleButton5.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton5.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton5.setChecked(true);
        toggleButton6 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton6);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton6.setText(dummySession.textDay(todaysSessionDate));
        toggleButton6.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton6.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton6.setChecked(true);
        toggleButton7 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton7);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton7.setText(dummySession.textDay(todaysSessionDate));
        toggleButton7.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton7.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton7.setChecked(true);

        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weekdayHashMap.put(toggleButton1.getText().toString(),true);
                } else {
                    weekdayHashMap.put(toggleButton1.getText().toString(),false);
                }
                filterSessions();
            }
        });

        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weekdayHashMap.put(toggleButton2.getText().toString(),true);
                } else {
                    weekdayHashMap.put(toggleButton2.getText().toString(),false);
                }
                filterSessions();
            }
        });

        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weekdayHashMap.put(toggleButton3.getText().toString(),true);
                } else {
                    weekdayHashMap.put(toggleButton3.getText().toString(),false);
                }
                filterSessions();
            }
        });

        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weekdayHashMap.put(toggleButton4.getText().toString(),true);
                } else {
                    weekdayHashMap.put(toggleButton4.getText().toString(),false);
                }
                filterSessions();
            }
        });

        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weekdayHashMap.put(toggleButton5.getText().toString(),true);
                } else {
                    weekdayHashMap.put(toggleButton5.getText().toString(),false);
                }
                filterSessions();
            }
        });

        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weekdayHashMap.put(toggleButton6.getText().toString(),true);
                } else {
                    weekdayHashMap.put(toggleButton6.getText().toString(),false);
                }
                filterSessions();
            }
        });

        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weekdayHashMap.put(toggleButton7.getText().toString(),true);
                } else {
                    weekdayHashMap.put(toggleButton7.getText().toString(),false);
                }
                filterSessions();
            }
        });

        filterSessions();
        // Inflate the layout for this fragment
        return inflatedView;

    }

    public void filterSessions() {

        geoFire = new GeoFire(mGeofireDbRef);
        nearSessions = new TreeMap<Integer,String>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
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

                                    final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
                                    HashMap<String,Boolean> sessionsCloseHash;
                                    sessionsCloseHash = new HashMap<String,Boolean>();

                                    for (Integer str : nearSessions.keySet()) {
                                        sessionsCloseHash.put(nearSessions.get(str),true);
                                    }

                                    myFirebaseDatabase.getSessionsFiltered(new OnSessionsFoundListener() {
                                        @Override
                                        public void OnSessionsFound(ArrayList<Session> sessions) {
                                            sessionsClose = sessions;

                                            onSessionsFilteredListener.OnSessionsFiltered(sessions,location);

                                        }
                                    },nearSessions, weekdayHashMap);
                                }
                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                }
                            });
                        }
                    }
                });
    }

    public String getDistance(double latitude, double longitude, Location currentLocation){

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionsFilteredListener) {
            onSessionsFilteredListener = (OnSessionsFilteredListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnSessionsFilteredListener {

        void OnSessionsFiltered(ArrayList<Session> sessions, Location location);
    }
}
