package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This fragment displays a google map with all sessions passed as arguments shown as markers on map
 * In case the bundle includes an int changeLocation which is 1 it will not display markers, it will
 * just show the map and a text and wait til the user clicks on the map and then CreateOrEditSession will
 * start from parent activity
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap;
    private MapView mapView;
    GoogleApiClient mGoogleApiClient;
    Marker mCurrLocationMarker;
    static final int PICK_SESSION_REQUEST = 1;
    private final DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("sessions");
    private LatLng clickedPosition;
    private LatLng markerLatLng;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private boolean moveCamera;
    private View myView;
    private OnSessionClickedListener onSessionClickedListener;
    private OnCreateStudioListener onCreateStudioListener;
    private OnSessionLocationChangedListener onSessionLocationChangedListener;
    private TextView createSessionMapTextTV;
    private boolean changeLocation;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Button chooseLocation;
    private LatLng chosenPoint;

    private Marker tempMarker;

    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        changeLocation = false;
        if (bundle != null) {
            if (bundle.getBoolean("changeLocation")) {
                changeLocation = true;
            }
        }
        setRetainInstance(true);
        moveCamera=true;
        mAuth = FirebaseAuth.getInstance();
        mMarkerDbRef.keepSynced(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (myView==null) {
            myView = inflater.inflate(R.layout.fragment_maps, container, false);
            mapView = myView.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
        }

        chooseLocation = myView.findViewById(R.id.chooseLocation);
        if (chosenPoint==null) {
            chooseLocation.setVisibility(View.GONE);
        } else {
            chooseLocation.setVisibility(View.VISIBLE);
        }

        createSessionMapTextTV = myView.findViewById(R.id.create_session_map_text);
        return myView;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        //  Get information of current user from database in order to determine if the user is in
        //  "trainer mode" or not. If in trainer mode, set an onclicklistener to the map so that the
        //  user can create sessions (start CreateOrEditSessionActivity by click), show also explanation text "Click on map to create session"
        MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(User user) {
                if (user.trainerMode) {
                    createSessionMapTextTV.setVisibility(View.VISIBLE);
                    //when map is clicked, open CreateOrEditSessionActivity
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng point) {

                            chosenPoint = point;

                            if(tempMarker!=null) {
                                tempMarker.remove();
                            }

                            tempMarker =  mMap.addMarker(new MarkerOptions().position(point).icon(getMarkerIcon("#00897b")));

                            createSessionMapTextTV.setText(getAddress(point.latitude, point.longitude));

                            chooseLocation.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    createSessionMapTextTV.setVisibility(View.GONE);
                }
            }
        });

        chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chosenPoint!=null) {
                    if (changeLocation) {
                        onSessionLocationChangedListener.OnSessionLocationChanged(chosenPoint);
                        getActivity().onBackPressed();
                    } else {
                        addSession(chosenPoint);
                    }
                }
            }
        });
    }

    // Method to add markers to map. This method is called from MainPlayerActivity. Set also an
    // Onclicklistener to the map in order to display session when marker is clicked.
    public void addMarkersToMap(ArrayList<Session> sessions) {

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.clear();
        for (Session session: sessions) {
            LatLng loc = new LatLng(session.getLatitude(), session.getLongitude());
            mMap.addMarker(new MarkerOptions().position(loc).title(session.getSessionType()).icon(getMarkerIcon("#00897b")).snippet(session.supplyTextTimeStamp().textTime()));
        }

        // mMap.addMarker(new MarkerOptions().position(loc).title(session.getSessionType()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_on_black_24dp)).snippet(session.supplyTextTimeStamp().textTime()));

        // when marker is clicked find latitude value in child in realtime database
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                markerLatLng = marker.getPosition();
                displaySession(markerLatLng);
                return false;
            }
        });
    }

    // method definition
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    // Method to start CreateOrEditSessionActivity
    private void addSession(LatLng clickedPosition) {
        onCreateStudioListener.OnCreateStudio(clickedPosition);
    }

    // Method that adds a marker to the map when map is clicked and CreateOrEditSessionActivity
    // has been started.
    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_SESSION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mMap.addMarker(new MarkerOptions().position(clickedPosition));
            }
        }
    }*/

    // Method to call the interface OnSessionClickedListener so that MainPlayerActivity knows that
    // a session has been clicked and display session fragment should be created and switched to.
    private void displaySession(LatLng markerLatLng) {
        onSessionClickedListener.OnSessionClicked(markerLatLng.latitude, markerLatLng.longitude);
    }

    //google  ONLY LONG AND LAT SET BELOW THIS
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(1*1000);    // TODO Check this
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        Double latitudeDouble = mLastLocation.getLatitude();
        Double longitudeDouble = mLastLocation.getLongitude();

        if(moveCamera){
            LatLng latLng = new LatLng(latitudeDouble, longitudeDouble);
            mLocationRequest.setInterval(10 * 1000);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            moveCamera=false;
        }
    }

    public void goToMyLocation() {

        if (mLastLocation!=null) {
            Double latitudeDouble = mLastLocation.getLatitude();
            Double longitudeDouble = mLastLocation.getLongitude();
            LatLng latLng = new LatLng(latitudeDouble, longitudeDouble);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            mMap.animateCamera(cameraUpdate);
        }
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
        if (context instanceof OnCreateStudioListener) {
            onCreateStudioListener = (OnCreateStudioListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateStudioListener");
        }
        if (context instanceof OnSessionLocationChangedListener) {
            onSessionLocationChangedListener = (OnSessionLocationChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionLocationChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        onCreateStudioListener = null;
        onSessionLocationChangedListener = null;
    }

    public interface OnCreateStudioListener {
        void OnCreateStudio(LatLng latLng);
    }

    public interface OnSessionLocationChangedListener {
        void OnSessionLocationChanged(LatLng latLng);
    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

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
