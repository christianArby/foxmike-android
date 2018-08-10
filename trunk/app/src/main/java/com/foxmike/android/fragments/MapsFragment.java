package com.foxmike.android.fragments;
// Checked
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallRecyclerViewsAdapter;
import com.foxmike.android.adapters.ListSmallSessionsHorizontalAdapter;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.models.Studio;
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
import java.util.HashMap;
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
    private OnLocationPickedListener onLocationPickedListener;
    private OnSessionClickedListener onSessionClickedListener;
    private TextView createSessionMapTextTV;
    private boolean changeLocation;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Button chooseLocation;
    private LatLng chosenPoint;
    private String requestType;
    private String studioId;
    private Studio studio;
    private RecyclerView mSessionList;
    private ListSmallRecyclerViewsAdapter listSmallRecyclerViewsAdapter;
    private HashMap<Marker, Integer> sessionMarkersMap = new HashMap<>();
    private ArrayList<Marker> markerArray = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private Marker selectedMarker;
    private Bitmap defaultMarkerBitmap;
    private Bitmap selectedMarkerBitmap;
    private int horizontalSessionWidth =0;
    int horizontalSessionHeight = 0;
    private int currentSessionInt = 0;
    private ArrayList<ArrayList<Session>> thisNearSessionsArrays;

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
        setRetainInstance(true);
        Bundle bundle = this.getArguments();
        changeLocation = false;

        // Get request type
        if (bundle != null) {
            requestType = bundle.getString("requestType", "nothing");
            if (requestType.equals("createSession")) {
                studioId = bundle.getString("studioId");
                studio = (Studio) bundle.getSerializable("studio");
            }
        }

        moveCamera=true;

        mAuth = FirebaseAuth.getInstance();
        mMarkerDbRef.keepSynced(true);

        // Get horizontal height of small sessions in order to set recycler view height
        horizontalSessionHeight = getResources().getDimensionPixelSize(R.dimen.horizontal_session_height);

        // Set default and selected marker
        defaultMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_marker);
        defaultMarkerBitmap = scaleBitmap(defaultMarkerBitmap, 90, 90);
        selectedMarkerBitmap = scaleBitmap(defaultMarkerBitmap, 100, 100);
        selectedMarkerBitmap = Bitmap.createBitmap(selectedMarkerBitmap, 0, 0,
                defaultMarkerBitmap.getWidth() - 1, defaultMarkerBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(ContextCompat.getColor(getActivity(), R.color.foxmikePrimaryColor), PorterDuff.Mode.SRC_IN);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(selectedMarkerBitmap);
        canvas.drawBitmap(selectedMarkerBitmap, 0, 0, p);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (myView==null) {
            myView = inflater.inflate(R.layout.fragment_maps, container, false);
            mapView = myView.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            chooseLocation = myView.findViewById(R.id.chooseLocation);
            createSessionMapTextTV = myView.findViewById(R.id.create_session_map_text);
            // OnMapReadyCallback will be triggered when map is ready
            mapView.getMapAsync(this);

            // Setup horizontal recyclerView
            mSessionList = myView.findViewById(R.id.session_list);
            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            mSessionList.setLayoutManager(linearLayoutManager);
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(mSessionList);
        }

        // If chosenPoint is null it means that the user has not chosen a location so set the view to gone (applies only when user is in trainer mode)
        if (chosenPoint==null) {
            chooseLocation.setVisibility(View.GONE);
        } else {
            chooseLocation.setVisibility(View.VISIBLE);
        }

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
                // ------------------ TRAINER MODE -------------------------------------------------------
                if (user.trainerMode) {
                    createSessionMapTextTV.setVisibility(View.VISIBLE);
                    // Set on Map clickedListeners
                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng point) {
                            chosenPoint = point;
                            if(tempMarker!=null) {
                                tempMarker.remove();
                            }
                            tempMarker =  mMap.addMarker(new MarkerOptions().position(point).icon(getMarkerIcon("#00897b")));
                            createSessionMapTextTV.setText(getAddress(point.latitude, point.longitude));
                            chooseLocation.setVisibility(View.VISIBLE);

                        }
                    });

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
                    // ------------------ PLAYER MODE -------------------------------------------------------
                } else {
                    // Listen to scroll events
                    mSessionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            switch (newState) {
                                case RecyclerView.SCROLL_STATE_IDLE:
                                    // Set which session currently is in focus after scroll
                                    currentSessionInt = (linearLayoutManager.findLastCompletelyVisibleItemPosition() - linearLayoutManager.findFirstCompletelyVisibleItemPosition())  / 2 + linearLayoutManager.findFirstVisibleItemPosition();

                                    // Set height of recyclerView based on how many sessions currently is in this position
                                    ViewGroup.LayoutParams params =mSessionList.getLayoutParams();
                                    params.height= thisNearSessionsArrays.get(currentSessionInt).size()*horizontalSessionHeight;
                                    mSessionList.setLayoutParams(params);
                                    listSmallRecyclerViewsAdapter.notifyDataSetChanged();

                                    // If selectedMarker is not null it means that a previous marker was clicked, turn it into the default marker
                                    if (selectedMarker!=null) {
                                        selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                    }

                                    // Set the selected marker to the marker stored in markerArray at this position
                                    selectedMarker = markerArray.get(currentSessionInt);
                                    // Change the marker at this position to selected since the user has scrolled to this position
                                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                    markerArray.get(currentSessionInt).showInfoWindow();

                                    // If the currently selected session and marker is not shown on the map after scroll, move camera to that position
                                    boolean contains = mMap.getProjection()
                                            .getVisibleRegion()
                                            .latLngBounds
                                            .contains(markerArray.get(currentSessionInt).getPosition());
                                    if(!contains){
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerArray.get(currentSessionInt).getPosition(), 15));
                                    }

                                    break;
                            }
                        }
                    });

                    // Setup on marker clicked listener
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            // Find out which session is in focus in recyclerView
                            //currentSessionInt = (linearLayoutManager.findLastCompletelyVisibleItemPosition() - linearLayoutManager.findFirstCompletelyVisibleItemPosition())  / 2 + linearLayoutManager.findFirstVisibleItemPosition();

                            // If the corresponding session to the clicked marker is not in focus in recyclerView, scroll to that position
                            mSessionList.smoothScrollToPosition(markerArray.indexOf(marker));
                            // When clicked on marker, show recyclerView
                            if (thisNearSessionsArrays!=null) {
                                ObjectAnimator animation = ObjectAnimator.ofFloat(mSessionList, "translationY", 0);
                                animation.setDuration(2000);
                                animation.start();
                            }

                            return false;
                        }
                    });
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            // When map is clicked, animate the recyclerview off the map (by same distance as the height of the current recyclerView
                            if (thisNearSessionsArrays!=null) {
                                ObjectAnimator animation = ObjectAnimator.ofFloat(mSessionList, "translationY", thisNearSessionsArrays.get(currentSessionInt).size()*horizontalSessionHeight);
                                animation.setDuration(2000);
                                animation.start();
                            }


                        }
                    });
                    createSessionMapTextTV.setVisibility(View.GONE);
                }
            }
        });

        // When in trainerMode, chooseLocation will be visible
        chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chosenPoint!=null) {
                    onLocationPickedListener.OnLocationPicked(chosenPoint, requestType, studioId, studio);
                }
            }
        });
    }

    // Method to add markers to map. This method is called from MainPlayerActivity. Set also an
    // Onclicklistener to the map in order to display session when marker is clicked.
    public void addMarkersToMap(ArrayList<ArrayList<Session>> nearSessionsArrays) {
        thisNearSessionsArrays = nearSessionsArrays;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Clear the map of markers and clear set the array of the new array
        mMap.clear();
        selectedMarker = null;
        currentSessionInt = 0;
        markerArray.clear();

        // Add markers the map
        for (ArrayList<Session> sessionArrayList : nearSessionsArrays) {
            Session session = sessionArrayList.get(0);
            LatLng loc = new LatLng(session.getLatitude(), session.getLongitude());
            //Marker marker = mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(defaultMarkerBitmap)));
            Marker marker = mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            markerArray.add(marker);
        }

        // Update or create the recyclerView
        if (listSmallRecyclerViewsAdapter!=null) {
            listSmallRecyclerViewsAdapter.refreshData(thisNearSessionsArrays);
            mSessionList.smoothScrollToPosition(0);
            markerArray.get(0).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            selectedMarker = markerArray.get(0);
            ViewGroup.LayoutParams params =mSessionList.getLayoutParams();
            params.height= nearSessionsArrays.get(currentSessionInt).size()*horizontalSessionHeight;
            mSessionList.setLayoutParams(params);

        } else {
            listSmallRecyclerViewsAdapter = new ListSmallRecyclerViewsAdapter(thisNearSessionsArrays, getActivity(), onSessionClickedListener);
            if (mSessionList!=null) {
                mSessionList.setAdapter(listSmallRecyclerViewsAdapter);
                markerArray.get(0).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                selectedMarker = markerArray.get(0);
                ViewGroup.LayoutParams params =mSessionList.getLayoutParams();
                params.height= nearSessionsArrays.get(currentSessionInt).size()*horizontalSessionHeight;
                mSessionList.setLayoutParams(params);
            }
            listSmallRecyclerViewsAdapter.notifyDataSetChanged();
        }
    }

    // Method to set marker color by string
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
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
        if (context instanceof OnLocationPickedListener) {
            onLocationPickedListener = (OnLocationPickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLocationPickedListener");
        }
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onLocationPickedListener = null;
        onSessionClickedListener = null;
    }


    public interface OnLocationPickedListener {
        void OnLocationPicked(LatLng latLng, String requestType, String studioId, Studio studio);
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

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
}
