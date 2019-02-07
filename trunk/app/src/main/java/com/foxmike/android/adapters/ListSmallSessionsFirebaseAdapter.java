package com.foxmike.android.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.ListSmallSessionsViewHolder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 2018-09-23.
 */

public class ListSmallSessionsFirebaseAdapter extends FirebaseRecyclerAdapter<Session, ListSmallSessionsViewHolder> {
    
    private Context context;
    private OnSessionClickedListener onSessionClickedListener;
    private long mLastClickTime = 0;
  
    
    public ListSmallSessionsFirebaseAdapter(@NonNull FirebaseRecyclerOptions<Session> options, Context context, OnSessionClickedListener onSessionClickedListener) {
        super(options);
        this.context = context;
        this.onSessionClickedListener = onSessionClickedListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ListSmallSessionsViewHolder holder, int position, @NonNull Session model) {
        holder.setSessionImage(model.getImageUrl(), context);
        holder.setText0(model.getSessionType());
        holder.setText1(model.getSessionName());
        String address = getAddress(model.getLatitude(),model.getLongitude());
        holder.setText3(address);

        long earliestUpcomingAd = 0;
        Long currentTimestamp = System.currentTimeMillis();

        if (model.getAdvertisements()!=null) {
            for (long adTimestamp: model.getAdvertisements().values()) {
                if (adTimestamp > currentTimestamp) {
                    if (earliestUpcomingAd==0) {
                        earliestUpcomingAd = adTimestamp;
                    } else {
                        if (adTimestamp<earliestUpcomingAd) {
                            earliestUpcomingAd = adTimestamp;
                        }
                    }
                }

            }
        }

        holder.setText2(earliestUpcomingAd, context);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onSessionClickedListener.OnSessionClicked(model.getSessionId());
            }
        });
        
    }

    @NonNull
    @Override
    public ListSmallSessionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_small_single_layout, parent, false);
        return new ListSmallSessionsViewHolder(view);
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
