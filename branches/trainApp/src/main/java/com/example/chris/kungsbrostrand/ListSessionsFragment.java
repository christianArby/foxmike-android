package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListSessionsFragment extends Fragment {

    private RecyclerView mSessionList;
    private RecyclerView.Adapter adapter;
    private Location currentLocation;


    public ListSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSessionsFragment newInstance() {
        ListSessionsFragment fragment = new ListSessionsFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_sessions, container, false);
        mSessionList = (RecyclerView) view.findViewById(R.id.session_list);
        mSessionList.setHasFixedSize(true);
        mSessionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSessionList.setAdapter(adapter);
    }

    public void FilterSessions(ArrayList<Session> sessions, Location location) {
        adapter = new sessionsAdapter(sessions, getActivity());
        currentLocation =location;
        if (mSessionList!=null) {
            mSessionList.setAdapter(adapter);
        }
    }


    public class sessionsAdapter extends RecyclerView.Adapter<sessionsAdapter.SessionViewHolder>{

        private ArrayList<Session> sessions;
        private Context context;

        public sessionsAdapter(ArrayList<Session> sessions, Context context) {
            this.sessions = sessions;
            this.context = context;
        }

        @Override
        public sessionsAdapter.SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_row,parent,false);
            return  new sessionsAdapter.SessionViewHolder(v);
        }

        @Override
        public void onBindViewHolder(sessionsAdapter.SessionViewHolder holder, int position) {
            Session session = sessions.get(position);

            final LatLng sessionLatLng = new LatLng(session.latitude,session.longitude);
            String address = getAddress(session.latitude,session.longitude)+"  |  "+getDistance(session.latitude,session.longitude, currentLocation);
            holder.setTitle(session.getSessionName());
            holder.setDesc(session.getSessionType());
            String sessionTime = String.format("%02d:%02d", session.sessionDate.hour, session.sessionDate.minute);
            holder.setDateAndTime(session.textFullDay(session.sessionDate) + " " + session.sessionDate.day + " " + session.textMonth(session.sessionDate) + " " + sessionTime);
            holder.setAddress(address);
            holder.setImage(getActivity(),session.getImageUri());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    joinSession(sessionLatLng);

                }
            });

        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        public class SessionViewHolder extends RecyclerView.ViewHolder{

            View mView;

            public SessionViewHolder(View itemView) {
                super(itemView);

                mView= itemView;
            }

            public void setTitle(String title){

                TextView session_title = (TextView) mView.findViewById(R.id.session_title);
                session_title.setText(title);

            }

            public void setAddress(String address){

                TextView session_address = (TextView) mView.findViewById(R.id.session_address);
                session_address.setText(address);

            }

            public void setDesc(String desc){

                TextView session_desc = (TextView) mView.findViewById(R.id.session_desc);
                session_desc.setText(desc);

            }

            public void setDateAndTime(String dateAndTime){

                TextView sessionDateAndTime = (TextView) mView.findViewById(R.id.session_date_and_time);
                sessionDateAndTime.setText(dateAndTime);

            }

            public void setImage(Context ctx, String image){
                ImageView session_image = (ImageView) mView.findViewById(R.id.session_image);
                Glide.with(ctx).load(image).into(session_image);
                session_image.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
            }

        }

    }

    public void joinSession(LatLng markerLatLng) {
        Intent intent = new Intent(getActivity(), JoinSessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        startActivity(intent);
    }

    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
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
                returnAddress = "Unknown area";
            }

        } catch (IOException ex) {

            returnAddress = "failed";
        }

        return returnAddress;

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
        String distanceString = Float.toString(b).replaceAll("\\.?0*$", "") + " m";

        if (b>1000) {
            b=b/1000;
            distanceString = String.format("%.1f", b) + " km";
        }

        return  distanceString;

    }
}