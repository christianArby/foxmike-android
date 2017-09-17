package com.example.chris.kungsbrostrand;


import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ListSessionsActivity extends AppCompatActivity {

    DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    ArrayList<Session> sessionsClose = new ArrayList<Session>();









    private RecyclerView mSessionList;

    private RecyclerView.Adapter adapter;

    private DatabaseReference mDatabase;

    private FusedLocationProviderClient mFusedLocationClient;

    private Location currentLocation;

    GeoFire geoFire;

    TreeMap<Integer,String> nearSessions;

    public HashMap<String,Boolean> weekdayHashMap;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sessions);




        FragmentManager fragMgr = getSupportFragmentManager();
        FragmentTransaction xact = fragMgr.beginTransaction();
        if (null == fragMgr.findFragmentByTag("weekdayFragment")) {
            xact.add(R.id.insertedFragment, WeekdayFilterFragment.newInstance(), "weekdayFragment").commit();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();


        //Weekday filter



        weekdayHashMap = new HashMap<String,Boolean>();

        Session dummySession = new Session();
        Calendar cal = Calendar.getInstance();

        SessionDate todaysSessionDate = new SessionDate(cal);

        for(int i=1; i<8; i++){
            weekdayHashMap.put(dummySession.textDay(todaysSessionDate), true);
            todaysSessionDate.day = todaysSessionDate.day +1;
        }


        //todaysSessionDate = new SessionDate(cal);

        //weekdayHashMap.put(dummySession.textDay(todaysSessionDate), true);

        mSessionList = (RecyclerView) findViewById(R.id.session_list);
        mSessionList.setHasFixedSize(true);
        mSessionList.setLayoutManager(new LinearLayoutManager(this));

        filterSessions();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void filterSessions() {

        geoFire = new GeoFire(mGeofireDbRef);
        nearSessions = new TreeMap<Integer,String>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            currentLocation = location;
                            // ...

                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 3000);

                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    //Any location key which is within 3000km from the user's location will show up here as the key parameter in this method
                                    //You can fetch the actual data for this location by creating another firebase query here

                                    String distString = getDistance(location.latitude,location.longitude);
                                    Integer dist = Integer.parseInt(distString);
                                    nearSessions.put(dist,key);
                                }

                                @Override
                                public void onKeyExited(String key) {}

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {}

                                @Override
                                public void onGeoQueryReady() {

                                    final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
                                    HashMap<String,Boolean> sessionsCloseHash;
                                    sessionsCloseHash = new HashMap<String,Boolean>();

                                    for (Integer str : nearSessions.keySet()) {
                                        sessionsCloseHash.put(nearSessions.get(str),true);
                                    }

                                    myFirebaseDatabase.getSessionsFiltered(new OnSessionsFoundListener() {
                                        @Override
                                        public void OnSessionsFound(ArrayList<Session> sessions) {
                                            sessionsClose = sessions;

                                            adapter = new sessionsAdapter(sessionsClose, ListSessionsActivity.this);

                                            mSessionList.setAdapter(adapter);
                                        }
                                    },nearSessions, weekdayHashMap);
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {

                                }
                            });
                        }
                    }
                });

    }

    public class sessionsAdapter extends RecyclerView.Adapter<sessionsAdapter.SessionViewHolder>{

        private ArrayList<Session> sessions;
        private Context context;

        public sessionsAdapter(ArrayList<Session> sessions, Context context) {
            this.sessions = sessions;
            this.context = context;
        }

        @Override
        public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_row,parent,false);
            return  new SessionViewHolder(v);
        }

        @Override
        public void onBindViewHolder(SessionViewHolder holder, int position) {
            Session session = sessions.get(position);

            final LatLng sessionLatLng = new LatLng(session.latitude,session.longitude);
            String address = getAddress(session.latitude,session.longitude)+"  |  "+getDistance(session.latitude,session.longitude)+" m";
            holder.setTitle(session.getSessionName());
            holder.setDesc(session.getSessionType());
            holder.setAddress(address);
            holder.setImage(getApplicationContext(),session.getImageUri());

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

            public void setImage(Context ctx, String image){
                ImageView session_image = (ImageView) mView.findViewById(R.id.session_image);
                Glide.with(ctx).load(image).into(session_image);
                session_image.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
            }




        }

    }

    public void joinSession(LatLng markerLatLng) {
        Intent intent = new Intent(this, JoinSessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        startActivity(intent);
    }

    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(this, Locale.getDefault());

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

    public String getDistance(double latitude, double longitude){

        Location locationA = new Location("point A");

        locationA.setLatitude(currentLocation.getLatitude());
        locationA.setLongitude(currentLocation.getLongitude());

        Location locationB = new Location("point B");

        locationB.setLatitude(latitude);
        locationB.setLongitude(longitude);

        float distance = locationA.distanceTo(locationB);

        float b = (float)Math.round(distance);

        String distanceString = Float.toString(b).replaceAll("\\.?0*$", "");

        return  distanceString;

    }
}
