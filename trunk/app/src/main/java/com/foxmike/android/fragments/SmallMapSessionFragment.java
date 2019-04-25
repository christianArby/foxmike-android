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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.viewmodels.SessionViewModel;

import java.io.IOException;
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


    public SmallMapSessionFragment() {
        // Required empty public constructor
    }


    public static SmallMapSessionFragment newInstance(String sessionId) {
        SmallMapSessionFragment fragment = new SmallMapSessionFragment();
        Bundle args = new Bundle();
        args.putString(SESSIONID, sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionId = getArguments().getString(SESSIONID);
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
                text0TV.setText(session.getSessionType());
                sessionNameTV.setText(session.getSessionName());
                sessionTypeAndDateTV.setText("Here goes filtered info");
                sessionAddressTV.setText(getAddress(session.getLatitude(), session.getLongitude()));
                distanceTV.setText(getDistance(session.getLatitude(), session.getLongitude()));
                Glide.with(getActivity().getApplicationContext()).load(session.getImageUrl()).into(sessionIV);
            }
        });

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
        ExploreMapsFragment exploreMapsFragment = ((ExploreMapsFragment) SmallMapSessionFragment.this.getParentFragment());
        if (exploreMapsFragment!=null) {
            Location currentLocation = exploreMapsFragment.getmLastKnownLocation();


            if (currentLocation!=null) {
                Location locationA = new Location("point A");

                locationA.setLatitude(currentLocation.getLatitude());
                locationA.setLongitude(currentLocation.getLongitude());

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
            } else {
                distanceString = "";
            }
        } else {
            distanceString = "";
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
