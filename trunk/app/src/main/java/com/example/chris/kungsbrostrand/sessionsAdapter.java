package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This adapter retrieves the data ArrayList<Session>, context and currentLocation as input, given in the constructor, and generates the view in layout.fragment_list_sessions
 */

public class sessionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final ArrayList<Session> sessions;
    private final Context context;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;

    public sessionsAdapter(ArrayList<Session> sessions, Context context, Location currentLocation, final OnSessionClickedListener onSessionClickedListener) {
        this.sessions = sessions;
        this.context = context;
        this.currentLocation=currentLocation;
        this.onSessionClickedListener = onSessionClickedListener;
    }

    /**Inflate the parent recyclerview with the layout session_card_view which is the session "cardview" */

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType==1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_list_date_header,parent,false);
            return  new sessionsAdapter.SessionListHeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_card_view,parent,false);
            return  new sessionsAdapter.SessionViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /**Get the current session inflated in the sessionViewHolder from the session arraylist" */
        final Session session = sessions.get(position);

        if (session.getImageUrl().equals("dateHeader")) {
            ((SessionListHeaderViewHolder) holder).setHeader(session.getSessionDate().textSDF());
        } else {

            /**Fill the cardview with information of the session" */
            final LatLng sessionLatLng = new LatLng(session.getLatitude(),session.getLongitude());
            String address = getAddress(session.getLatitude(),session.getLongitude())+"  |  "+getDistance(session.getLatitude(),session.getLongitude(), currentLocation);
            ((SessionViewHolder) holder).setTitle(session.getSessionName());
            ((SessionViewHolder) holder).setDesc(session.getSessionType());
            ((SessionViewHolder) holder).setDateAndTime(session.getSessionDate().textFullDay() + " " + session.getSessionDate().day + " " + session.getSessionDate().textMonth() + " " + session.textTime());
            ((SessionViewHolder) holder).setAddress(address);
            ((SessionViewHolder) holder).setImage(this.context,session.getImageUrl());

            /**When button is clicked, start DisplaySessionActivity by sending the LatLng object in order for the activity to find the session" */

            ((SessionViewHolder) holder).mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSessionClickedListener.OnSessionClicked(session.getLatitude(),session.getLongitude());
                    //displaySession(sessionLatLng);
                }
            });

        }



    }

    @Override
    public int getItemViewType(int position) {
        if (sessions.get(position).getImageUrl().equals("dateHeader")) {
            return 1;
        }
        return 0;
    }

    /**Change getItemCount to return the number of items this sessionsAdapter should adapt*/
    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public class SessionListHeaderViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public SessionListHeaderViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setHeader(String header) {
            TextView headerTV = mView.findViewById(R.id.listSessionsDateHeader);
            headerTV.setText(header);
        }
    }

    /**Innerclass which extends RecyclerView.ViewHolder. An object of this class is created in the above OnBindViewholder where all the items are set to the session information" */
    public class SessionViewHolder extends RecyclerView.ViewHolder{

        final View mView;

        public SessionViewHolder(View itemView) {
            super(itemView);
            mView= itemView;
        }

        public void setTitle(String title){
            TextView session_title = mView.findViewById(R.id.session_title);
            session_title.setText(title);
        }

        public void setAddress(String address){
            TextView session_address = mView.findViewById(R.id.session_address);
            session_address.setText(address);
        }

        public void setDesc(String desc){
            TextView session_desc = mView.findViewById(R.id.session_desc);
            session_desc.setText(desc);
        }

        public void setDateAndTime(String dateAndTime){
            TextView sessionDateAndTime = mView.findViewById(R.id.session_date_and_time);
            sessionDateAndTime.setText(dateAndTime);
        }

        public void setImage(Context ctx, String image){
            ImageView session_image = mView.findViewById(R.id.session_image);
            Glide.with(ctx).load(image).into(session_image);
            session_image.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
        }

    }

    /**Method get address from latitude and longitude" */
    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(this.context, Locale.getDefault());

        try {
            /**I have written some of these funtions just in case we will use the further on" */
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

    /**Method get distance from current location to a certain point with latitude, longitude */
    private String getDistance(double latitude, double longitude, Location currentLocation){

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
