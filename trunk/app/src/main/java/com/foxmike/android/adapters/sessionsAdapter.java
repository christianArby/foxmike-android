package com.foxmike.android.adapters;
//Checked
import android.content.Context;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.utils.HeaderItemDecoration;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.TextTimestamp;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This adapter retrieves the data ArrayList<Session>, context and currentLocation as input, given in the constructor, and generates the view in layout.fragment_list_sessions
 */
public class sessionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HeaderItemDecoration.StickyHeaderInterface {

    private ArrayList<Session> sessions;
    private final Context context;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;

    public sessionsAdapter(ArrayList<Session> sessions, Context context, Location currentLocation, final OnSessionClickedListener onSessionClickedListener) {
        this.sessions = sessions;
        this.context = context;
        this.currentLocation=currentLocation;
        this.onSessionClickedListener = onSessionClickedListener;
    }

    public void refreshData(ArrayList<Session> sessions, Location currentLocation) {
        this.sessions = sessions;
        this.currentLocation=currentLocation;
        this.notifyDataSetChanged();
    }

    /**Inflate the parent recyclerview with the layout session_card_view which is the session "cardview" */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType==1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_list_date_header,parent,false);
            return  new sessionsAdapter.SessionListHeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_card_view,parent,false);
            final ImageView session_image = v.findViewById(R.id.session_image);
            // Setup standard aspect ratio of session image
            session_image.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams mParams;
                    mParams = (RelativeLayout.LayoutParams) session_image.getLayoutParams();
                    mParams.height = session_image.getWidth()*context.getResources().getInteger(R.integer.heightOfSessionImageNumerator)/context.getResources().getInteger(R.integer.heightOfSessionImageDenominator);
                    session_image.setLayoutParams(mParams);
                    session_image.postInvalidate();
                }
            });

            return  new sessionsAdapter.SessionViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /**Get the current session inflated in the sessionViewHolder from the session arraylist" */
        final Session session = sessions.get(position);

        if (session.getImageUrl().equals("dateHeader")) {
            DateTime currentTime = DateTime.now();
            DateTime tomorrowTime = currentTime.plusDays(1);
            DateTime sessionTime = new DateTime(session.getSessionTimestamp());
            if (currentTime.getYear()==sessionTime.getYear() &&
                    currentTime.getMonthOfYear()==sessionTime.getMonthOfYear() &&
                    currentTime.getDayOfMonth()==sessionTime.getDayOfMonth()
                    ) {
                ((SessionListHeaderViewHolder) holder).setHeader(context.getString(R.string.today_text));
            } else if (tomorrowTime.getYear()==sessionTime.getYear() &&
                    tomorrowTime.getMonthOfYear()==sessionTime.getMonthOfYear() &&
                    tomorrowTime.getDayOfMonth()==sessionTime.getDayOfMonth()
                    ) {
                ((SessionListHeaderViewHolder) holder).setHeader(context.getString(R.string.tomorrow_text));
            } else {
                ((SessionListHeaderViewHolder) holder).setHeader(session.supplyTextTimeStamp().textSessionDate());
            }
        } else {

            /**Fill the cardview with information of the session" */
            final LatLng sessionLatLng = new LatLng(session.getLatitude(),session.getLongitude());
            String address = getAddress(session.getLatitude(),session.getLongitude())+"  |  "+getDistance(session.getLatitude(),session.getLongitude(), currentLocation);
            ((SessionViewHolder) holder).setTitle(session.getSessionName());
            ((SessionViewHolder) holder).setDesc(session.getSessionType());
            ((SessionViewHolder) holder).setDateAndTime(session.supplyTextTimeStamp().textSessionDateAndTime());
            ((SessionViewHolder) holder).setAddress(address);
            ((SessionViewHolder) holder).setImage(this.context,session.getImageUrl());

            if (position > 0 && !sessions.get(position-1).getImageUrl().equals("dateHeader")) {
                ((SessionViewHolder) holder).setMargin();
            } else {
                ((SessionViewHolder) holder).resetMargin();
            }
            /**When button is clicked, start DisplaySessionActivity by sending the LatLng object in order for the activity to find the session" */
            ((SessionViewHolder) holder).mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSessionClickedListener.OnSessionClicked(session.getSessionId());
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

    /**
     * This method gets called by {@link HeaderItemDecoration} to fetch the position of the header item in the adapter
     * that is used for (represents) item at specified position.
     * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
     * @return int. Position of the header item in the adapter.
     */
    @Override
    public int getHeaderPositionForItem(int itemPosition) {

        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    /**
     * This method gets called by {@link HeaderItemDecoration} to get layout resource id for the header item at specified adapter's position.
     * @param headerPosition int. Position of the header item in the adapter.
     * @return int. Layout resource id.
     */
    @Override
    public int getHeaderLayout(int headerPosition) {
        int layoutResource = 0;
        if (sessions.size() > 0  && sessions.get(0).getImageUrl().equals("dateHeader")) {
            return R.layout.session_list_date_header;
        } else {
            return R.layout.blank_dummy_for_header_list;
        }
    }

    /**
     * This method gets called by {@link HeaderItemDecoration} to setup the header View.
     * @param header View. Header to set the data on.
     * @param headerPosition int. Position of the header item in the adapter.
     */

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        if (sessions.size()>headerPosition && sessions.get(0).getImageUrl().equals("dateHeader")) {
            TextView headerTV = header.findViewById(R.id.listSessionsDateHeader);
            DateTime currentTime = DateTime.now();
            DateTime tomorrowTime = currentTime.plusDays(1);
            DateTime sessionTime = new DateTime(sessions.get(headerPosition).getSessionTimestamp());
            if (currentTime.getYear()==sessionTime.getYear() &&
                    currentTime.getMonthOfYear()==sessionTime.getMonthOfYear() &&
                    currentTime.getDayOfMonth()==sessionTime.getDayOfMonth()
                    ) {
                headerTV.setText(context.getString(R.string.today_text));
            } else if (tomorrowTime.getYear()==sessionTime.getYear() &&
                    tomorrowTime.getMonthOfYear()==sessionTime.getMonthOfYear() &&
                    tomorrowTime.getDayOfMonth()==sessionTime.getDayOfMonth()
                    ) {
                headerTV.setText(context.getString(R.string.tomorrow_text));
            } else {
                headerTV.setText(sessions.get(headerPosition).supplyTextTimeStamp().textSessionDate());
            }
        }
    }


    /**
     * This method gets called by {@link HeaderItemDecoration} to verify whether the item represents a header.
     * @param itemPosition int.
     * @return true, if item at the specified adapter's position represents a header.
     */
    @Override
    public boolean isHeader(int itemPosition) {

        if (sessions.size()>itemPosition) {
            if (sessions.get(itemPosition).getImageUrl().equals("dateHeader")) {
                return true;
            } else {
                return false;
            }
        } else  {
            return false;
        }

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

        public void setMargin(){
            ConstraintLayout constraintLayout = mView.findViewById(R.id.sessionCardViewFrame);
            int dpValue = 5; // margin in dips
            float d = context.getResources().getDisplayMetrics().density;
            int margin = (int)(dpValue * d);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(0, margin, 0, 0);
            constraintLayout.setLayoutParams(params);
        }

        public void resetMargin(){
            ConstraintLayout constraintLayout = mView.findViewById(R.id.sessionCardViewFrame);
            int dpValue = 0; // margin in dips
            float d = context.getResources().getDisplayMetrics().density;
            int margin = (int)(dpValue * d);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(0, margin, 0, 0);
            constraintLayout.setLayoutParams(params);
        }

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