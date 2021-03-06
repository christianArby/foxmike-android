package com.foxmike.android.adapters;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 2018-08-08.
 */

public class ListSmallSessionsHorizontalAdapter extends RecyclerView.Adapter<ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder> {

    private HashMap<String, Session> sessionHashMap;
    ArrayList<String> sessionIdsFiltered = new ArrayList<>();
    private Context context;
    private OnSessionClickedListener onSessionClickedListener;
    private Location lastLocation;
    private long mLastClickTime = 0;

    public ListSmallSessionsHorizontalAdapter(HashMap<String, Session> sessionHashMap, ArrayList<String> sessionIdsFiltered, Context context, OnSessionClickedListener onSessionClickedListener, Location lastLocation, long mLastClickTime) {
        this.sessionHashMap = sessionHashMap;
        this.sessionIdsFiltered = sessionIdsFiltered;
        this.context = context;
        this.onSessionClickedListener = onSessionClickedListener;
        this.lastLocation = lastLocation;
        this.mLastClickTime = mLastClickTime;
    }

    @NonNull
    @Override
    public ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_horizontal_layout, parent, false);
        return new ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder holder, int position) {

        holder.setText0(sessionHashMap.get(sessionIdsFiltered.get(position)).getSessionType());
        String sessionName = sessionHashMap.get(sessionIdsFiltered.get(position)).getSessionName();
        holder.setSessionName(sessionName);

        /*if (sessionAdvertisementsHashMap.get(sessionIdsFiltered.get(position)).size()>0) {
            Long currentTimestamp = System.currentTimeMillis();
            Long earliestAd = 0L;
            for (Long adTimestamp : sessionAdvertisementsHashMap.get(sessionIdsFiltered.get(position)).values()) {
                if (earliestAd == 0L) {
                    earliestAd = adTimestamp;
                }
                if (adTimestamp<earliestAd && adTimestamp>currentTimestamp) {
                    earliestAd = adTimestamp;
                }
            }
            if (earliestAd!= 0L) {
                holder.setText2(TextTimestamp.textSessionDate(earliestAd));
            } else {
                holder.setText2(context.getString(R.string.no_upcoming_sessions));
            }
        } else {
            holder.setText2(context.getString(R.string.no_upcoming_sessions));
        }*/

        String address = getAddress(sessionHashMap.get(sessionIdsFiltered.get(position)).getLatitude(), sessionHashMap.get(sessionIdsFiltered.get(position)).getLongitude());
        holder.setText3(address);
        String distance = getDistance(sessionHashMap.get(sessionIdsFiltered.get(position)).getLatitude(), sessionHashMap.get(sessionIdsFiltered.get(position)).getLongitude(), lastLocation);
        holder.setText4(" \u2022 " + distance);
        holder.setSessionImage(sessionHashMap.get(sessionIdsFiltered.get(position)).getImageUrl());
        holder.setSessionClickedListener(sessionHashMap.get(sessionIdsFiltered.get(position)).getSessionId());
    }

    @Override
    public int getItemCount() {
        return sessionIdsFiltered.size();
    }

    public class ListSmallSessionsHorizontalViewholder extends RecyclerView.ViewHolder {

        View mView;

        public void setSessionClickedListener(String sessionId) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    onSessionClickedListener.OnSessionClicked(sessionId);
                }
            });
        }

        public ListSmallSessionsHorizontalViewholder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setText0(String text0) {
            TextView text0TV = (TextView) mView.findViewById(R.id.text0);
            text0TV.setText(text0);
        }

        public void setSessionName(String sessionName) {
            TextView sessionNameTV = (TextView) mView.findViewById(R.id.text1);
            sessionNameTV.setText(sessionName);
        }

        public void setText2(String sessionTypeAndDate) {
            TextView sessionTypeAndDateTV = (TextView) mView.findViewById(R.id.text2);
            sessionTypeAndDateTV.setText(sessionTypeAndDate);
        }

        public void setText3(String address) {
            TextView sessionAddressTV = (TextView) mView.findViewById(R.id.text3);
            sessionAddressTV.setText(address);
        }

        public void setText4(String distance) {
            TextView distanceTV = (TextView) mView.findViewById(R.id.text4);
            distanceTV.setText(distance);
        }

        public void setSessionImage(String sessionImage) {
            ImageView sessionIV = (ImageView) mView.findViewById(R.id.session_image);
            Glide.with(context).load(sessionImage).into(sessionIV);
        }
    }

    public void refreshData(HashMap<String, Session> sessionHashMap, ArrayList<String> sessionIdsFiltered) {
        this.sessionHashMap = sessionHashMap;
        this.sessionIdsFiltered = sessionIdsFiltered;
        this.notifyDataSetChanged();
    }

    public void notifySessionChange(String sessionId, HashMap<String, Session> sessionHashMap) {
        if (sessionIdsFiltered.contains(sessionId)) {
            this.sessionHashMap = sessionHashMap;
            this.notifyItemChanged(sessionIdsFiltered.indexOf(sessionId));
        }
    }

    public void notifySessionRemoved(String sessionId) {
        if (sessionIdsFiltered.contains(sessionId)) {
            sessionIdsFiltered.remove(sessionId);
            this.notifyItemRemoved(sessionIdsFiltered.indexOf(sessionId));
        }
    }

    /**Method get distance from current location to a certain point with latitude, longitude */
    private String getDistance(double latitude, double longitude, Location currentLocation){

        String distanceString;

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


        return  distanceString;
    }

    /**Method get address from latitude and longitude" */
    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(this.context, Locale.getDefault());

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

}


