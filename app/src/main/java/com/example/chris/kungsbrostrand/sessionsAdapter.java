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

public class sessionsAdapter extends RecyclerView.Adapter<sessionsAdapter.SessionViewHolder>{

    private final ArrayList<Session> sessions;
    private final Context context;
    private Location currentLocation;

    public sessionsAdapter(ArrayList<Session> sessions, Context context, Location currentLocation) {
        this.sessions = sessions;
        this.context = context;
        this.currentLocation=currentLocation;
    }

    /**Inflate the parent recyclerview with the layout session_card_view which is the session "cardview" */

    @Override
    public sessionsAdapter.SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_card_view,parent,false);
        return  new sessionsAdapter.SessionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(sessionsAdapter.SessionViewHolder holder, int position) {
        /**Get the current session inflated in the sessionViewHolder from the session arraylist" */
        Session session = sessions.get(position);

        /**Fill the cardview with information of the session" */
        final LatLng sessionLatLng = new LatLng(session.getLatitude(),session.getLongitude());
        String address = getAddress(session.getLatitude(),session.getLongitude())+"  |  "+getDistance(session.getLatitude(),session.getLongitude(), currentLocation);
        holder.setTitle(session.getSessionName());
        holder.setDesc(session.getSessionType());
        String sessionTime = String.format("%02d:%02d", session.getSessionDate().hour, session.getSessionDate().minute);
        holder.setDateAndTime(session.textFullDay(session.getSessionDate()) + " " + session.getSessionDate().day + " " + session.textMonth(session.getSessionDate()) + " " + sessionTime);
        holder.setAddress(address);
        holder.setImage(this.context,session.getImageUri());

        /**When button is clicked, start DisplaySessionActivity by sending the LatLng object in order for the activity to find the session" */
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySession(sessionLatLng);
            }
        });

    }

    /**Change getItemCount to return the number of items this sessionsAdapter should adapt*/
    @Override
    public int getItemCount() {
        return sessions.size();
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

    /**Method to start DisplaySessionActivity" */
    private void displaySession(LatLng markerLatLng) {
        Intent intent = new Intent(this.context, DisplaySessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        this.context.startActivity(intent);
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
