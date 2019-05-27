package com.foxmike.android.fragments;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.foxmike.android.viewmodels.SessionViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SmallMapSessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmallMapSessionFragment extends Fragment {

    private static final String SESSIONID = "sessionId";

    private String sessionId;
    private TextView text0TV;
    private TextView sessionNameTV;
    private TextView sessionTypeAndDateTV;
    private TextView sessionAddressTV;
    private TextView distanceTV;
    private ImageView sessionIV;
    private OnSessionClickedListener onSessionClickedListener;
    private HashMap<String, String> sessionTypeDictionary;
    private Session mSession;
    private boolean nextSessionLoaded;
    private boolean sessionLoaded;
    private boolean uIupdated;
    private Long nextSession;
    private Double currentLatitude;
    private Double currentLongitude;
    private FrameLayout dotProgressBarContainer;
    private ConstraintLayout content;


    public SmallMapSessionFragment() {
        // Required empty public constructor
    }


    public static SmallMapSessionFragment newInstance(String sessionId, HashMap<String,String> sessionTypeDictionary, Double currentLatitude, Double currentLongitude) {
        SmallMapSessionFragment fragment = new SmallMapSessionFragment();
        Bundle args = new Bundle();
        args.putString(SESSIONID, sessionId);
        args.putDouble("currentLatitude", currentLatitude);
        args.putDouble("currentLongitude", currentLongitude);
        args.putSerializable("sessionTypeDictionary", sessionTypeDictionary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionId = getArguments().getString(SESSIONID);
            currentLatitude = getArguments().getDouble("currentLatitude");
            currentLongitude = getArguments().getDouble("currentLongitude");
            sessionTypeDictionary = (HashMap<String, String>)getArguments().getSerializable("sessionTypeDictionary");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_small_map_session, container, false);

        text0TV = (TextView) mView.findViewById(R.id.text0);
        sessionNameTV = (TextView) mView.findViewById(R.id.text1);
        sessionTypeAndDateTV = (TextView) mView.findViewById(R.id.text2);
        sessionAddressTV = (TextView) mView.findViewById(R.id.text3);
        distanceTV = (TextView) mView.findViewById(R.id.text4);
        sessionIV = (ImageView) mView.findViewById(R.id.session_image);
        dotProgressBarContainer = mView.findViewById(R.id.dotProgressBarContainer);
        content = mView.findViewById(R.id.content);

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSessionClickedListener.OnSessionClicked(sessionId);
            }
        });

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SessionViewModel sessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);
        LiveData<Session> sessionLiveData = sessionViewModel.getSessionLiveData(sessionId);
        sessionLiveData.observe(getViewLifecycleOwner(), new Observer<Session>() {

            @Override
            public void onChanged(@Nullable Session session) {
                mSession = session;
                sessionLoaded = true;
                uIupdated = false;
                updateUI();

            }
        });

        Long currentTimestamp = System.currentTimeMillis();
        // Create query to get all the advertisement keys from the current mSession
        Query keyQuery = FirebaseDatabase.getInstance().getReference().child("sessionAdvertisements").child(sessionId).orderByValue().startAt(currentTimestamp);

        FirebaseDatabaseViewModel sessionsComingViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> sessionsComingLiveData = sessionsComingViewModel.getDataSnapshotLiveData(keyQuery);
        sessionsComingLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        nextSession = (Long) snapshot.getValue();
                        break;
                    }
                } else {
                    nextSession = 0L;
                }
                nextSessionLoaded = true;
                uIupdated = false;
                updateUI();
            }
        });





    }

    private void updateUI() {
        if (!uIupdated && nextSessionLoaded && sessionLoaded) {
            uIupdated = true;

            if (!sessionTypeDictionary.containsKey(mSession.getSessionType())) {
                text0TV.setText(getResources().getString(R.string.other));
            } else {
                text0TV.setText(sessionTypeDictionary.get(mSession.getSessionType()));
            }
            sessionNameTV.setText(mSession.getSessionName());

            if (!nextSession.equals(0L)) {
                sessionTypeAndDateTV.setText(getResources().getString(R.string.next_session) + TextTimestamp.textSessionDateAndTime(nextSession));
            } else {
                sessionTypeAndDateTV.setText(getResources().getString(R.string.no_upcoming_sessions));
            }

            sessionAddressTV.setText(getAddress(mSession.getLatitude(), mSession.getLongitude()));
            distanceTV.setText(" | " + getDistance(mSession.getLatitude(), mSession.getLongitude()));
            Glide.with(getActivity().getApplicationContext()).load(mSession.getImageUrl()).into(sessionIV);

            dotProgressBarContainer.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);

        }

    }

    /**Method get address from latitude and longitude" */
    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if (addresses.size()!=0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String address2 = addresses.get(0).getAddressLine(1);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                String street = addresses.get(0).getThoroughfare();// Only if available else return NULL

                if (street != null) {

                    if (!street.equals(knownName)) {
                        returnAddress = street + " " + knownName;
                    } else {
                        returnAddress = street;
                    }
                } else {
                    if (addresses.get(0).getLocality()!=null) {
                        returnAddress = addresses.get(0).getLocality() + " " + addresses.get(0).getPremises();
                    } else {
                        returnAddress = "Unknown area";
                    }

                }
            } else {
                returnAddress = "Unknown area";
            }

        } catch (IOException ex) {
            returnAddress = "failed";
        }
        return returnAddress;
    }

    /**Method get distance from current location to a certain point with latitude, longitude */
    private String getDistance(double latitude, double longitude){
        String distanceString;


        Location locationA = new Location("point A");

        locationA.setLatitude(currentLatitude);
        locationA.setLongitude(currentLongitude);

        Location locationB = new Location("point B");

        locationB.setLatitude(latitude);
        locationB.setLongitude(longitude);

        float distance = locationA.distanceTo(locationB);

        float b = (float)Math.round(distance);
        distanceString = Float.toString(b).replaceAll("\\.?0*$", "") + " m";

        if (b>1000) {
            b=b/1000;
            distanceString = String.format("%.1f", b) + " km";
        }

        return  distanceString;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
    }
}
