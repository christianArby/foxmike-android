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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeekdayFilterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeekdayFilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeekdayFilterFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public ListSessionsActivity listSessionsActivity;

    public interface OnSessionsFilteredListener {

        public void OnSessionsFiltered(ArrayList<Session> sessions, Location location);
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

    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    ArrayList<Session> sessionsClose = new ArrayList<Session>();



    private OnSessionsFilteredListener mListener;

    public WeekdayFilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WeekdayFilterFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        listSessionsActivity = (ListSessionsActivity) getActivity();

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

                                    String distString = listSessionsActivity.getDistance(location.latitude,location.longitude, currentLocation);
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

                                            mListener.OnSessionsFiltered(sessions,location);

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

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionsFilteredListener) {
            mListener = (OnSessionsFilteredListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
