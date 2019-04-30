package com.foxmike.android.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.firebase.geofire.GeoLocation;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallSessionsHorizontalAdapter;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.CustomMapClusterRenderer;
import com.foxmike.android.utils.MyItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExploreMapsFragment extends Fragment implements OnMapReadyCallback{

    public static final String TAG = ExploreMapsFragment.class.getSimpleName();

    private MapView mMapView;
    private GoogleMap mMap;
    private ClusterManager<MyItem> mClusterManager;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 13;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    public Location mLastKnownLocation;
    private int horizontalSessionHeight = 0;
    private int currentSessionInt = 0;
    private RecyclerView mSessionList;
    private LinearLayoutManager linearLayoutManager;
    private BitmapDescriptor defaultIcon;
    private BitmapDescriptor selectedIcon;
    private ArrayList<Marker> markerArray = new ArrayList<>();
    private Marker selectedMarker;
    private HashMap<String, GeoLocation> sessionLocationsMap = new HashMap<>();
    private int leftMargin;

    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private boolean nearSessionsLoaded;
    private boolean nearSessionsUsed;

    private boolean showRecyclerView;
    private boolean showRecyclerViewLoaded;
    private boolean showRecyclerViewUsed;

    private long mLastClickTime = 0;

    private Marker mCurrLocationMarker;
    private boolean moveCamera;

    private ListSmallSessionsHorizontalAdapter listSmallRecyclerViewsAdapter;
    private OnSessionClickedListener onSessionClickedListener;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FrameLayout smallMapsSessionFragmentContainer;

    private Map<MyItem, String> markers = new HashMap<>();
    private CustomMapClusterRenderer customMapClusterRenderer;
    private HashMap<String, String> sessionTypeDictionary;

    public Location getmLastKnownLocation() {
        return mLastKnownLocation;
    }

    public ExploreMapsFragment() {
        // Required empty public constructor
    }

    public static ExploreMapsFragment newInstance(HashMap<String,String> sessionTypeDictionary) {
        ExploreMapsFragment fragment = new ExploreMapsFragment();
        Bundle args = new Bundle();
        args.putSerializable("sessionTypeDictionary", sessionTypeDictionary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionTypeDictionary = (HashMap<String, String>)getArguments().getSerializable("sessionTypeDictionary");
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    mLastKnownLocation = locationList.get(locationList.size() - 1);
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                }
            }
        };

        // Construct a FusedLocationProviderClient.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        leftMargin = (int) convertDpToPx(getActivity().getApplicationContext(), 16);
        moveCamera=true;

        // Get horizontal height of small session list in order to navigation buttons position
        horizontalSessionHeight = getResources().getDimensionPixelSize(R.dimen.horizontal_session_height);


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);

        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_maps, container, false);

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);


        mMapView.getMapAsync(this);

        smallMapsSessionFragmentContainer = view.findViewById(R.id.smallMapSessionFragmentContainer);

        // Setup horizontal recyclerView
        mSessionList = view.findViewById(R.id.session_list);
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mSessionList.setLayoutManager(linearLayoutManager);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mSessionList);

        Drawable locationDrawable = getResources().getDrawable(R.mipmap.baseline_location_on_black_36);
        defaultIcon = getMarkerIconFromDrawable(locationDrawable);
        Drawable selectedLocationDrawable = locationDrawable.mutate();
        selectedLocationDrawable.setColorFilter(getResources().getColor(R.color.foxmikePrimaryColor), PorterDuff.Mode.SRC_ATOP);
        selectedIcon = getMarkerIconFromDrawable(selectedLocationDrawable);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onAsyncTaskFinished();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(15000); // 15s interval
        locationRequest.setFastestInterval(15000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Listen to scroll events
        //listenToScrollEvents();

        // Setup on marker clicked listener
        listenToMapAndMarkerClickedEvents();

        setUpClusterer();



    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            //Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation==null) {
                                return;
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void setUpClusterer() {
        // Position the map.
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(getActivity().getApplicationContext(), mMap);
        customMapClusterRenderer = new CustomMapClusterRenderer<>(getActivity().getApplicationContext(),mMap, mClusterManager);
        mClusterManager.setRenderer(customMapClusterRenderer);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                if (selectedMarker!=null) {
                    selectedMarker.setIcon(defaultIcon);
                }
                selectedMarker = customMapClusterRenderer.getMarker(myItem);
                String sessionId = markers.get(myItem);
                FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.smallMapSessionFragmentContainer, SmallMapSessionFragment.newInstance(sessionId, sessionTypeDictionary, mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).commit();
                ObjectAnimator animation = ObjectAnimator.ofFloat(smallMapsSessionFragmentContainer, "translationY", 0);
                animation.setDuration(500);
                animation.start();
                mMap.setPadding(leftMargin,0,leftMargin,horizontalSessionHeight);
                selectedMarker.setIcon(selectedIcon);
                return false;
            }
        });

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.

    }



    /*private void listenToScrollEvents() {
        mSessionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        // Set which session currently is in focus after scroll
                        currentSessionInt = (linearLayoutManager.findLastCompletelyVisibleItemPosition() - linearLayoutManager.findFirstCompletelyVisibleItemPosition())  / 2 + linearLayoutManager.findFirstVisibleItemPosition();

                        // If selectedMarker is not null it means that a previous marker was clicked, turn it into the default marker
                        if (selectedMarker!=null) {
                            selectedMarker.setIcon(defaultIcon);
                        }

                        // Set the selected marker to the marker stored in markerArray at this position
                        selectedMarker = markerArray.get(currentSessionInt);
                        // Change the marker at this position to selected since the user has scrolled to this position
                        selectedMarker.setIcon(selectedIcon);
                        markerArray.get(currentSessionInt).showInfoWindow();

                        // If the currently selected session and marker is not shown on the map after scroll, move camera to that position
                        boolean contains = mMap.getProjection()
                                .getVisibleRegion()
                                .latLngBounds
                                .contains(markerArray.get(currentSessionInt).getPosition());
                        if(!contains){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerArray.get(currentSessionInt).getPosition(), 14));
                        }

                        break;
                }
            }
        });
    }*/


    private void listenToMapAndMarkerClickedEvents() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (selectedMarker!=null) {
                    selectedMarker.setIcon(defaultIcon);
                }
                // When map is clicked, animate the recyclerview off the map (by same distance as the height of the current recyclerView
                ObjectAnimator animation = ObjectAnimator.ofFloat(smallMapsSessionFragmentContainer, "translationY", horizontalSessionHeight);
                animation.setDuration(500);
                animation.start();
                mMap.setPadding(leftMargin,0,leftMargin,0);
            }
        });
    }


    private void onAsyncTaskFinished() {

        if (nearSessionsLoaded && getView()!=null && mMap!=null && !nearSessionsUsed) {
            nearSessionsUsed = true;

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity(), R.raw.maps_style));

                if (!success) {
                    Log.e("MAPS", "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("MAPS", "Can't find style. Error: ", e);
            }
            // Clear the map of markers and clear set the array of the new array
            mMap.clear();
            mClusterManager.clearItems();
            selectedMarker = null;
            currentSessionInt = 0;
            markerArray.clear();

            if (sessionLocationsMap.size()>0) {
                // Add markers the map
                for (String key : sessionLocationsMap.keySet()) {
                    /*LatLng loc = new LatLng(sessionLocationsMap.get(key).latitude, sessionLocationsMap.get(key).longitude);
                    //Marker marker = mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(defaultMarkerBitmap)));
                    */

                    MyItem marker = new MyItem(sessionLocationsMap.get(key).latitude, sessionLocationsMap.get(key).longitude);

                    markers.put(marker, key);
                    mClusterManager.addItem(marker);

                }
            }

            mClusterManager.cluster();

        }

    }

    // Method to add markers to map. This method is called from MainPlayerActivity. Set also an
    // Onclicklistener to the map in order to display session when marker is clicked.
    public void addMarkersToMap(HashMap<String, GeoLocation> sessionLocationsMap) {
        this.sessionLocationsMap = sessionLocationsMap;
        nearSessionsLoaded = true;
        nearSessionsUsed = false;
        onAsyncTaskFinished();
    }

    public void notifySessionChange(String sessionId, HashMap<String, Session> sessionHashMap) {
        if (listSmallRecyclerViewsAdapter!=null) {
            listSmallRecyclerViewsAdapter.notifySessionChange(sessionId, sessionHashMap);
        }
    }

    public void notifySessionRemoved(String sessionId) {
        if (listSmallRecyclerViewsAdapter!=null) {
            listSmallRecyclerViewsAdapter.notifySessionRemoved(sessionId);
        }
    }

    public void goToMyLocation() {

        if (mLastKnownLocation!=null) {
            Double latitudeDouble = mLastKnownLocation.getLatitude();
            Double longitudeDouble = mLastKnownLocation.getLongitude();
            LatLng latLng = new LatLng(latitudeDouble, longitudeDouble);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            mMap.animateCamera(cameraUpdate);
        }
    }

    public void showRecylerView(boolean showRecyclerView) {

        this.showRecyclerView = showRecyclerView;
        showRecyclerViewLoaded = true;
        showRecyclerViewUsed = false;

        onAsyncTaskFinished();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        //stop location updates when Activity is no longer active
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        listSmallRecyclerViewsAdapter = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSessionList!=null) {
            mSessionList.setAdapter(null);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
    }


    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
