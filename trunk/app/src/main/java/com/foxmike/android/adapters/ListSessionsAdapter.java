package com.foxmike.android.adapters;

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
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.AdvertisementIdsAndTimestamps;
import com.foxmike.android.utils.HeaderItemDecoration;
import com.foxmike.android.utils.TextTimestamp;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 2019-03-15.
 */

public class ListSessionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HeaderItemDecoration.StickyHeaderInterface {

    private ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList;
    private HashMap<String, Advertisement> advertisementHashMap = new HashMap<>();
    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private final Context context;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;

    public ListSessionsAdapter(ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList, HashMap<String, Advertisement> advertisementHashMap, HashMap<String, Session> sessionHashMap, Context context, Location currentLocation, OnSessionClickedListener onSessionClickedListener) {
        this.advertisementIdsAndTimestampsFilteredArrayList = advertisementIdsAndTimestampsFilteredArrayList;
        this.advertisementHashMap = advertisementHashMap;
        this.sessionHashMap = sessionHashMap;
        this.context = context;
        this.currentLocation = currentLocation;
        this.onSessionClickedListener = onSessionClickedListener;
    }

    public void refreshData(ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList, HashMap<String, Advertisement> advertisementHashMap, HashMap<String, Session> sessionHashMap, Location currentLocation) {
        this.advertisementIdsAndTimestampsFilteredArrayList = advertisementIdsAndTimestampsFilteredArrayList;
        this.advertisementHashMap = advertisementHashMap;
        this.sessionHashMap = sessionHashMap;
        this.currentLocation = currentLocation;
        this.notifyDataSetChanged();
    }

    /**Inflate the parent recyclerview with the layout session_card_view which is the session "cardview" */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType==1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_list_date_header,parent,false);
            return  new ListSessionsAdapter.SessionListHeaderViewHolder(v);
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

            return new ListSessionsAdapter.SessionViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /**Get the current session inflated in the sessionViewHolder from the session arraylist" */


        if (advertisementIdsAndTimestampsFilteredArrayList.get(position).getAdvertisementId().equals("dateHeader")) {
            DateTime currentTime = DateTime.now();
            DateTime tomorrowTime = currentTime.plusDays(1);
            DateTime sessionTime = new DateTime(advertisementIdsAndTimestampsFilteredArrayList.get(position).getAdTimestamp());
            if (currentTime.getYear()==sessionTime.getYear() &&
                    currentTime.getMonthOfYear()==sessionTime.getMonthOfYear() &&
                    currentTime.getDayOfMonth()==sessionTime.getDayOfMonth()
                    ) {
                ((ListSessionsAdapter.SessionListHeaderViewHolder) holder).setHeader(context.getString(R.string.today_text));
            } else if (tomorrowTime.getYear()==sessionTime.getYear() &&
                    tomorrowTime.getMonthOfYear()==sessionTime.getMonthOfYear() &&
                    tomorrowTime.getDayOfMonth()==sessionTime.getDayOfMonth()
                    ) {
                ((ListSessionsAdapter.SessionListHeaderViewHolder) holder).setHeader(context.getString(R.string.tomorrow_text));
            } else {
                ((ListSessionsAdapter.SessionListHeaderViewHolder) holder).setHeader(TextTimestamp.textSessionDate(advertisementIdsAndTimestampsFilteredArrayList.get(position).getAdTimestamp()));
            }
        } else {
            Advertisement advertisement = advertisementHashMap.get(advertisementIdsAndTimestampsFilteredArrayList.get(position).getAdvertisementId());
            Session session = sessionHashMap.get(advertisement.getSessionId());

            /**Fill the cardview with information of the session" */
            final LatLng sessionLatLng = new LatLng(session.getLatitude(), session.getLongitude());
            String addressName = getAddress(session.getLatitude(),session.getLongitude());
            if (addressName.length()>40) {
                addressName = addressName.substring(0,40)+ "...";
            }
            String address = addressName +"  |  " + getDistance(session.getLatitude(),session.getLongitude(), this.currentLocation);
            ((ListSessionsAdapter.SessionViewHolder) holder).setTitle(session.getSessionName());
            ((ListSessionsAdapter.SessionViewHolder) holder).setDesc(session.getSessionType());
            ((ListSessionsAdapter.SessionViewHolder) holder).setDateAndTime(TextTimestamp.textSessionDateAndTime(advertisement.getAdvertisementTimestamp()));
            ((ListSessionsAdapter.SessionViewHolder) holder).setAddress(address);
            ((ListSessionsAdapter.SessionViewHolder) holder).setImage(this.context,session.getImageUrl());

            if (position > 0 && !advertisementIdsAndTimestampsFilteredArrayList.get(position-1).getAdvertisementId().equals("dateHeader")) {
                ((ListSessionsAdapter.SessionViewHolder) holder).setMargin();
            } else {
                ((ListSessionsAdapter.SessionViewHolder) holder).resetMargin();
            }
            /**When button is clicked, start DisplaySessionActivity by sending the LatLng object in order for the activity to find the session" */
            ((ListSessionsAdapter.SessionViewHolder) holder).mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSessionClickedListener.OnSessionClicked(advertisement.getSessionId(), advertisement.getAdvertisementTimestamp());
                }
            });
            String currencyString = "?";
            if (advertisement.getCurrency()==null) {
                currencyString = "";
            } else {
                currencyString = context.getResources().getString(R.string.sek);
            }
            String priceText;
            if (advertisement.getPrice()== 0) {
                priceText = context.getResources().getString(R.string.free);
            } else {
                priceText = advertisement.getPrice() + " " + currencyString;
            }
            ((ListSessionsAdapter.SessionViewHolder) holder).setPrice(priceText);

            ((ListSessionsAdapter.SessionViewHolder) holder).setRating(session.getRating());
            if (session.getNrOfRatings()==0) {
                ((ListSessionsAdapter.SessionViewHolder) holder).displayRating(false);
            } else if ((session.getNrOfRatings()==1)) {
                ((ListSessionsAdapter.SessionViewHolder) holder).displayRating(true);
                String rating = String.format("%.1f", session.getRating());
                ((ListSessionsAdapter.SessionViewHolder) holder).setRatingAndReviewText(rating + " (" + session.getNrOfRatings() + ")");
            } else {
                ((ListSessionsAdapter.SessionViewHolder) holder).displayRating(true);
                String rating = String.format("%.1f", session.getRating());
                ((ListSessionsAdapter.SessionViewHolder) holder).setRatingAndReviewText(rating + " (" + session.getNrOfRatings() + ")");
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (advertisementIdsAndTimestampsFilteredArrayList.get(position).getAdvertisementId().equals("dateHeader")) {
            return 1;
        }
        return 0;
    }

    /**Change getItemCount to return the number of items this ListSessionsAdapter should adapt*/
    @Override
    public int getItemCount() {
        return advertisementIdsAndTimestampsFilteredArrayList.size();
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
        if (advertisementIdsAndTimestampsFilteredArrayList.size() > 0  && advertisementIdsAndTimestampsFilteredArrayList.get(0).getAdvertisementId().equals("dateHeader")) {
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
        if (advertisementIdsAndTimestampsFilteredArrayList.size()>headerPosition && advertisementIdsAndTimestampsFilteredArrayList.get(0).getAdvertisementId().equals("dateHeader")) {
            TextView headerTV = header.findViewById(R.id.listSessionsDateHeader);
            DateTime currentTime = DateTime.now();
            DateTime tomorrowTime = currentTime.plusDays(1);
            DateTime sessionTime = new DateTime(advertisementIdsAndTimestampsFilteredArrayList.get(headerPosition).getAdTimestamp());
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
                headerTV.setText(TextTimestamp.textSessionDate(advertisementIdsAndTimestampsFilteredArrayList.get(headerPosition).getAdTimestamp()));
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

        if (advertisementIdsAndTimestampsFilteredArrayList.size()>itemPosition) {
            if (advertisementIdsAndTimestampsFilteredArrayList.get(itemPosition).getAdvertisementId().equals("dateHeader")) {
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

        public void setPrice(String price){
            TextView priceTV = mView.findViewById(R.id.priceTV);
            priceTV.setText(price);
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

        public void setRating(float rating) {
            /*AppCompatRatingBar ratingBar = mView.findViewById(R.id.ratingBar);
            ratingBar.setRating(rating);*/
        }

        public void setRatingAndReviewText(String ratingAndReviewText) {
            TextView ratingAndReviewTV = mView.findViewById(R.id.ratingsAndReviewsText);
            ratingAndReviewTV.setText(ratingAndReviewText);
        }

        public void displayRating(boolean displayRating) {
            ConstraintLayout ratingContainer = mView.findViewById(R.id.reviewContainer);
            TextView newFlag = mView.findViewById(R.id.newFlag);
            if (displayRating) {
                ratingContainer.setVisibility(View.VISIBLE);
                newFlag.setVisibility(View.GONE);
            } else {
                ratingContainer.setVisibility(View.GONE);
                newFlag.setVisibility(View.VISIBLE);
            }
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

    public void notifyAdvertisementChange(String advertisementId, HashMap<String, Advertisement> advertisementHashMap, HashMap<String, Session> sessionHashMap) {
        this.advertisementHashMap = advertisementHashMap;
        this.sessionHashMap = sessionHashMap;
        this.notifyDataSetChanged();
    }

    public void notifyAdvertisementRemoved(AdvertisementIdsAndTimestamps removedAd) {
        int removedItem = this.advertisementIdsAndTimestampsFilteredArrayList.indexOf(removedAd);
        if (removedItem>=0) {
            this.advertisementIdsAndTimestampsFilteredArrayList.remove(removedItem);
            this.notifyItemRemoved(removedItem);
            //this.notifyItemRangeChanged(removedItem, getItemCount());
            //;
        }
    }
}