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
import com.foxmike.android.interfaces.AlertOccasionCancelledListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.SmallAdvertisementViewHolder;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 2018-09-22.
 */


public class ListSmallAdvertisementsFirebaseAdapter extends FirebaseRecyclerAdapter<Advertisement, SmallAdvertisementViewHolder> {

    private Context context;
    private AlertOccasionCancelledListener alertOccasionCancelledListener;
    private OnSessionClickedListener onSessionClickedListener;
    private long mLastClickTime = 0;
    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private HashMap<String, String> sessionTypeDictionary;
    /**
     * This Firebase recycler adapter takes a firebase query and an boolean in order to populate a list of messages (chat).
     * If the boolean is true, the list is populated based on who sent the message. If current user has sent the message the message is shown to the right and
     * if not the message is shown to the left.
     */
    public ListSmallAdvertisementsFirebaseAdapter(FirebaseRecyclerOptions<Advertisement> options, Context context, AlertOccasionCancelledListener alertOccasionCancelledListener, HashMap<String,String> sessionTypeDictionary, OnSessionClickedListener onSessionClickedListener) {
        super(options);
        this.context = context;
        this.alertOccasionCancelledListener = alertOccasionCancelledListener;
        this.sessionTypeDictionary = sessionTypeDictionary;
        this.onSessionClickedListener = onSessionClickedListener;
    }

    @NonNull
    @Override
    public SmallAdvertisementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.advertisement_small_single_layout, parent, false);
        return new SmallAdvertisementViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull SmallAdvertisementViewHolder holder, int position, @NonNull Advertisement model) {
        holder.setSessionImage(model.getImageUrl(), context);
        holder.setText1(model.getSessionName());
        String advDateAndTime = TextTimestamp.textSessionDateAndTime(model.getAdvertisementTimestamp());
        advDateAndTime = advDateAndTime.substring(0,1).toUpperCase() + advDateAndTime.substring(1);
        Long endTimestamp = model.getAdvertisementTimestamp() + (model.getDurationInMin()*1000*60);
        if (model.getStatus().equals("cancelled")) {
            holder.setText2(advDateAndTime);
            holder.setCancelled(true, context);
        } else {
            holder.setText2(advDateAndTime + "-" + TextTimestamp.textTime(endTimestamp));
            holder.setCancelled(false, context);
        }
        populateSessionHashMap(model.getSessionId(), new OnSessionsLoadedListener() {
            @Override
            public void OnSessionsLoaded() {
                Session session = sessionHashMap.get(model.getSessionId());
                holder.setSessionImage(session.getImageUrl(), context);
                holder.setText3(sessionTypeDictionary.get(session.getSessionType()).toUpperCase() + " | " + getAddress(session.getLatitude(), session.getLongitude()));
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (!model.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if (model.getStatus().equals("cancelled")) {
                        if (model.getPrice()>0) {
                            alertOccasionCancelledListener.AlertOccasionCancelled(false, model.getSessionId());
                        } else {
                            alertOccasionCancelledListener.AlertOccasionCancelled(true, model.getSessionId());
                        }
                    } else {
                        onSessionClickedListener.OnSessionClicked(model.getSessionId(), model.getAdvertisementTimestamp());
                    }
                } else {
                    onSessionClickedListener.OnSessionClicked(model.getSessionId(), model.getAdvertisementTimestamp());
                }
            }
        });

    }

    private void populateSessionHashMap(String sessionId, OnSessionsLoadedListener onSessionsLoadedListener) {
        if (!sessionHashMap.containsKey(sessionId)) {
            FirebaseDatabase.getInstance().getReference().child("sessions").child(sessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    Session session = dataSnapshot.getValue(Session.class);
                    sessionHashMap.put(sessionId, session);
                    onSessionsLoadedListener.OnSessionsLoaded();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            onSessionsLoadedListener.OnSessionsLoaded();
        }
    }

    public interface OnSessionsLoadedListener{
        void OnSessionsLoaded();
    }

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
