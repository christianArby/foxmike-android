package com.foxmike.android.fragments;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ExplorerNavigationAdapter;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.TextTimestamp;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static android.view.View.INVISIBLE;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.foxmike.android.utils.Distance.DISTANCE_INTEGERS_SE;
import static com.foxmike.android.utils.Distance.DISTANCE_STRINGS_SE;
import static com.foxmike.android.utils.Price.PRICES_INTEGERS_SE;
import static com.foxmike.android.utils.Price.PRICES_STRINGS_SE;
import static com.foxmike.android.utils.StaticResources.maxDefaultHour;
import static com.foxmike.android.utils.StaticResources.maxDefaultMinute;
import static com.foxmike.android.utils.StaticResources.minDefaultHour;
import static com.foxmike.android.utils.StaticResources.minDefaultMinute;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment{

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationClient;

    public static final String TAG = ExploreFragment.class.getSimpleName();

    public HashMap<String,Boolean> firstWeekdayHashMap;
    public HashMap<String,Boolean> secondWeekdayHashMap;
    private ViewPager exploreFragmentViewPager;
    private ExplorerNavigationAdapter exploreFragmentAdapter;

    private String sortType;
    private FragmentManager fragmentManager;
    private FloatingActionButton mapOrListBtn;
    private FloatingActionButton sortAndFilterFAB;
    private FloatingActionButton myLocationBtn;
    private View mainView;
    private float mapOrListBtnStartX;
    private float mapOrListBtnStartY;
    private Boolean started = false;

    private TextView filteredItem1;
    private TextView filteredItem2;
    private TextView filteredItem3;
    private ArrayList<TextView> filteredItems = new ArrayList<>();
    private SortAndFilterFragment.OnFilterChangedListener onFilterChangedListener;
    private HashMap<DatabaseReference, ValueEventListener> sessionListeners = new HashMap<>();
    private HashMap<DatabaseReference, ValueEventListener> advertisementListeners = new HashMap<>();
    private HashMap<GeoQuery, GeoQueryEventListener> geofireListeners = new HashMap<>();
    private TabLayout tabLayout;
    private FrameLayout mapContainer;
    private boolean listIsVisible = true;

    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9;
    /*private GeoQuery geoQuery;
    private GeoQueryEventListener geoQueryEventListener;*/

    private LocationCallback locationCallback;
    public Location mLastKnownLocation;
    private LocationRequest locationRequest;
    private boolean weekdayFragmentsLoaded;

    private HashMap<String, Boolean> sessionTypeChosen = new HashMap<>();
    private int minPrice = PRICES_INTEGERS_SE.get("Min");
    private int maxPrice = PRICES_INTEGERS_SE.get("Max");
    private int minHour = minDefaultHour;
    private int minMinute = minDefaultMinute;
    private int maxHour = maxDefaultHour;
    private int maxMinute = maxDefaultMinute;
    private final int DEFAULT_DISTANCE = DISTANCE_INTEGERS_SE.get("6 mil");
    private int currentDefaultDistanceRadius = DISTANCE_INTEGERS_SE.get("6 mil");
    private int currentDistanceRadius = DISTANCE_INTEGERS_SE.get("6 mil");
    private ArrayList<String> sessionIdsFiltered;
    private TreeMap<String, GeoLocation> geoFireNodes = new TreeMap<>();
    private TreeMap<String, GeoLocation> geoFireNodesFiltered = new TreeMap<>();
    private long lastKeyEnteredTime;
    private boolean firstKey;
    private int currentDay;
    private HashMap<String, Session> sessionHashMap;
    private boolean geoFireNodesLoaded;
    private TreeMap<String, Long> advertismentsPerDayMap;
    private boolean locationFound;
    private boolean advertisementsPerDayLoaded;
    private int iteratedDay;
    private HashMap<String, String> sessionTypeDictionary;
    private boolean defaultColors;
    private boolean geoFireNodesUsed;
    private boolean fragmentsLoaded;
    private boolean geoFireNodesAndFragmentsUsed;
    private ArrayList<String> geoFireNodesKeys = new ArrayList<>();
    private long mLastGeoFireQueryUpdated = 0;
    private CountDownTimer countDownTimerLocation;

    public HashMap<String, Boolean> getSessionTypeChosen() {
        return sessionTypeChosen;
    }


    public int getMinPrice() {
        return minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public int getMinHour() {
        return minHour;
    }

    public int getMinMinute() {
        return minMinute;
    }

    public int getMaxHour() {
        return maxHour;
    }

    public int getMaxMinute() {
        return maxMinute;
    }

    public ExploreFragment() {
        // Required empty public constructor
    }

    public TreeMap<String, Long> getAdvertismentsPerDayMap() {
        return advertismentsPerDayMap;
    }


    public static ExploreFragment newInstance(HashMap<String,String> sessionTypeDictionary) {

        Bundle args = new Bundle();
        args.putSerializable("sessionTypeDictionary", sessionTypeDictionary);
        ExploreFragment fragment = new ExploreFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void locationPermissionChanged(boolean granted) {

        if (granted) {
            mLocationPermissionGranted = true;
            getLocation();
            updateLocationUI();
            getDeviceLocation();
        } else {
            mLocationPermissionGranted = false;
        }
        if (isAdded()) {
            updateFragmentsWithLocationPermission();
        }

    }

    private void updateFragmentsWithLocationPermission() {
        if (mLocationPermissionGranted) {
            if (exploreFragmentAdapter!=null) {
                for (int x = 0; x < 14; x++) {
                    ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(x);
                    if (listSessionsFragment!=null) {
                        listSessionsFragment.noLocationPermissionVisible(false);
                        listSessionsFragment.noLocationVisible(false);
                    }
                }
            }
            if (fragmentManager!=null) {
                ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) fragmentManager.findFragmentByTag("ExploreMapsFragment");
                if (exploreMapsFragment==null) {
                    return;
                }
                exploreMapsFragment.locationPermissionChanged(true);
            }

        } else {
            if (exploreFragmentAdapter!=null) {
                for (int x = 0; x < 14; x++) {
                    ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(x);
                    if (listSessionsFragment!=null) {
                        listSessionsFragment.noLocationPermissionVisible(true);
                        listSessionsFragment.noLocationVisible(false);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            sessionTypeDictionary = (HashMap<String, String>)getArguments().getSerializable("sessionTypeDictionary");
        }

        // Construct a FusedLocationProviderClient.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        locationRequest = new LocationRequest();
        locationRequest.setInterval(15000); // 15s interval
        locationRequest.setFastestInterval(15000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Prompt the user for permission.
        getLocationPermission();



        //ExploreMapsFragment exploreMapsFragment = ExploreMapsFragment.newInstance();
        //exploreFragmentAdapter.addFragments(exploreMapsFragment);

        /** Setup List and Map with sessions*/
        sortType = "date";
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
            locationPermissionChanged(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getLocation() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    mLastKnownLocation = locationList.get(locationList.size() - 1);
                    locationFound = true;
                    checkIfGeoFireNodesAreLoaded();
                    // ANTINGEN HÄR '''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
                    // UPDATE THINGS THAT NEED LAST LOCATION
                }
            }
        };
    }

    private void getDeviceLocation() {
        // getDeviceLocation
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
                            locationFound = true;
                            checkIfGeoFireNodesAreLoaded();
                            // ELLER HÄR '''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
                        } else {
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            //Log.e("Exception: %s", e.getMessage());
        }
    }

    private void checkIfGeoFireNodesAreLoaded() {
        if (!geoFireNodesLoaded && locationFound) {
            loadGeoFireNodes();
        }
    }

    public void dimCurrentDay(boolean dim) {
        if (dim) {
            tabLayout.setTabTextColors(getResources().getColor(R.color.primaryTextColor), getResources().getColor(R.color.primaryTextColor));
            defaultColors = false;
        } else {
            tabLayout.setTabTextColors(getResources().getColor(R.color.primaryTextColor), getResources().getColor(R.color.foxmikePrimaryColor));
            defaultColors = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DateTime today = new DateTime(System.currentTimeMillis());
        if (exploreFragmentAdapter!=null) {
            if (exploreFragmentAdapter.getFirstDay().getDayOfYear()!=today.getDayOfYear()) {
                exploreFragmentAdapter = null;
                exploreFragmentAdapter = new ExplorerNavigationAdapter(fragmentManager, getResources().getString(R.string.today_text));
                for (int x = 0; x < 14; x++) {
                    ListSessionsFragment listSessionsFragment = ListSessionsFragment.newInstance(x, sessionTypeDictionary);
                    exploreFragmentAdapter.addFragments(listSessionsFragment);
                }
                geoFireNodesLoaded = false;
                checkIfGeoFireNodesAreLoaded();
                exploreFragmentViewPager.setAdapter(exploreFragmentAdapter);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_explore, container, false);

        exploreFragmentViewPager = mainView.findViewById(R.id.exploreFragmentViewPager);
        tabLayout = (TabLayout) mainView.findViewById(R.id.explorer_tabs);
        tabLayout.setupWithViewPager(exploreFragmentViewPager);
        tabLayout.setTabTextColors(getResources().getColor(R.color.primaryTextColor), getResources().getColor(R.color.foxmikePrimaryColor));

        mapContainer = mainView.findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.INVISIBLE);


        fragmentManager = getChildFragmentManager();
        exploreFragmentAdapter = new ExplorerNavigationAdapter(fragmentManager, getResources().getString(R.string.today_text));
        for (int x = 0; x < 14; x++) {
            ListSessionsFragment listSessionsFragment = ListSessionsFragment.newInstance(x, sessionTypeDictionary);
            exploreFragmentAdapter.addFragments(listSessionsFragment);
        }

        exploreFragmentViewPager.setAdapter(exploreFragmentAdapter);
        exploreFragmentViewPager.setOffscreenPageLimit(4);

        if (fragmentManager.findFragmentByTag("ExploreMapsFragment")==null) {
            fragmentManager.beginTransaction().add(R.id.mapContainer, ExploreMapsFragment.newInstance(sessionTypeDictionary), "ExploreMapsFragment").commitNow();
        }

        geoFireNodesAndFragmentsUsed = false;
        fragmentsLoaded = true;
        updateFragmentsWithLocationPermission();
        checkIfToLoadFragmentsWithData();

        exploreFragmentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                if (!defaultColors) {
                    tabLayout.setTabTextColors(getResources().getColor(R.color.primaryTextColor), getResources().getColor(R.color.foxmikePrimaryColor));
                    defaultColors = true;
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {


            }
        });

        // filter buttons
        filteredItem1 = mainView.findViewById(R.id.filteredItem1);
        filteredItem2 = mainView.findViewById(R.id.filteredItem2);
        filteredItem3 = mainView.findViewById(R.id.filteredItem3);

        filteredItems.add(filteredItem1);
        filteredItem1.setHint("");
        filteredItems.add(filteredItem2);
        filteredItem2.setHint("");
        filteredItems.add(filteredItem3);
        filteredItem3.setHint("");

        for (TextView filterItem: filteredItems) {
            filterItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String filterType = filterItem.getHint().toString();
                    if (filterType.equals("minPrice")) {
                        minPrice = 0;
                        onFilterChangedListener.OnMinPriceChanged(minPrice);
                    }
                    if (filterType.equals("maxPrice")) {
                        maxPrice = PRICES_INTEGERS_SE.get("Max");
                        onFilterChangedListener.OnMaxPriceChanged(maxPrice);
                    }
                    if (filterType.equals("distance")) {
                        onFilterChangedListener.OnDistanceFilterChanged(currentDefaultDistanceRadius);
                    }
                }
            });
        }

        mapOrListBtn = mainView.findViewById(R.id.map_or_list_button);
        mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
        sortAndFilterFAB = mainView.findViewById(R.id.sort_button);
        myLocationBtn = mainView.findViewById(R.id.my_location_button);
        myLocationBtn.hide();

        /** Setup mapOrList FAB*/
        mapOrListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapContainer.getVisibility()==INVISIBLE) {
                    mapContainer.setVisibility(View.VISIBLE);
                    listIsVisible = false;
                    switchMapOrListUI(true);
                } else {
                    mapContainer.setVisibility(View.INVISIBLE);
                    listIsVisible = true;
                    switchMapOrListUI(false);
                }
            }
        });

        /** Setup sortAndFilter FAB*/
        sortAndFilterFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SortAndFilterFragment sortAndFilterFragment = (SortAndFilterFragment) fragmentManager.findFragmentByTag(SortAndFilterFragment.TAG);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (sortAndFilterFragment!=null) {
                    transaction.remove(sortAndFilterFragment);
                }
                sortAndFilterFragment = SortAndFilterFragment.newInstance(sortType, currentDistanceRadius, minPrice, maxPrice, minHour, minMinute, maxHour, maxMinute, currentDistanceRadius, sessionTypeChosen, sessionTypeDictionary);
                sortAndFilterFragment.show(transaction,SortAndFilterFragment.TAG);
            }
        });

        // Setup my location button
        myLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) fragmentManager.findFragmentByTag("ExploreMapsFragment");
                if (exploreMapsFragment==null) {
                    return;
                }
                exploreMapsFragment.goToMyLocation();
            }
        });

        countDownTimerLocation = new CountDownTimer(3000,3000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (!locationFound) {
                    if (exploreFragmentAdapter!=null) {
                        for (int x = 0; x < 14; x++) {
                            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(x);
                            if (listSessionsFragment!=null) {
                                listSessionsFragment.noLocationVisible(true);
                                listSessionsFragment.firstLoadVisible(false);
                            }
                        }
                    }
                }

            }
        };
        countDownTimerLocation.start();

        return mainView;
    }

    private void checkIfToLoadFragmentsWithData() {

        if (fragmentsLoaded && geoFireNodesLoaded && !geoFireNodesAndFragmentsUsed) {
            geoFireNodesAndFragmentsUsed = true;

            geoFireNodesUsed = true;
            filterGeoFireNodesAndPopulateListAndMap(false, 0);

        }

    }

    private void switchMapOrListUI(boolean mapIsVisible) {

        int width = mainView.getMeasuredWidth();
        int height = mainView.getMeasuredHeight();
        float fabDiameter = convertDpToPx(getActivity().getApplicationContext(), 56);

        if (width>0 && height>0 && fabDiameter>0) {
            mapOrListBtnStartX = width/2 - fabDiameter/2;
            mapOrListBtnStartY = height -  convertDpToPx(getActivity().getApplicationContext(), 20) - fabDiameter;
            float Xcontrol2 = width - convertDpToPx(getActivity().getApplicationContext(),72);
            float Ycontrol2 = sortAndFilterFAB.getY() + convertDpToPx(getActivity().getApplicationContext(), 144);

            ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) fragmentManager.findFragmentByTag("ExploreMapsFragment");

            if (exploreMapsFragment==null) {
                return;
            }


            if (mapIsVisible) {
                exploreMapsFragment.showRecylerView(true);
                mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_list_black_24dp));
                myLocationBtn.show();

                Path path = new Path();
                path.moveTo(mapOrListBtnStartX, mapOrListBtnStartY);
                path.quadTo(mapOrListBtnStartX, Ycontrol2, Xcontrol2, Ycontrol2);
                ObjectAnimator objectAnimator1 = new ObjectAnimator().ofFloat(mapOrListBtn, "x", "y", path);
                objectAnimator1.setDuration(500);
                objectAnimator1.setInterpolator(new LinearOutSlowInInterpolator());
                objectAnimator1.start();
            } else {
                exploreMapsFragment.showRecylerView(false);
                mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
                myLocationBtn.hide();

                Path path = new Path();
                path.moveTo(Xcontrol2, Ycontrol2);
                path.quadTo(Ycontrol2, mapOrListBtnStartY, mapOrListBtnStartX, mapOrListBtnStartY);
                ObjectAnimator objectAnimator1 = new ObjectAnimator().ofFloat(mapOrListBtn, "x", "y", path);
                objectAnimator1.setDuration(500);
                objectAnimator1.setInterpolator(new FastOutLinearInInterpolator());
                objectAnimator1.start();
            }
        }
    }

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /** INTERFACE triggered when list is scrolled REFRESHED, downloads all sessions based on input distance radius*/
    public void OnRefreshSessions(int weekday) {

        DateTime today = new DateTime(System.currentTimeMillis());
        if (exploreFragmentAdapter!=null) {
            if (exploreFragmentAdapter.getFirstDay().getDayOfYear()!=today.getDayOfYear()) {
                exploreFragmentAdapter = null;
                exploreFragmentAdapter = new ExplorerNavigationAdapter(fragmentManager, getResources().getString(R.string.today_text));
                for (int x = 0; x < 14; x++) {
                    ListSessionsFragment listSessionsFragment = ListSessionsFragment.newInstance(x, sessionTypeDictionary);
                    exploreFragmentAdapter.addFragments(listSessionsFragment);
                }
                geoFireNodesLoaded = false;
                checkIfGeoFireNodesAreLoaded();
                exploreFragmentViewPager.setAdapter(exploreFragmentAdapter);
                return;
            }
        }

        if (!mLocationPermissionGranted) {
            getLocationPermission();
            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(weekday);
            if (listSessionsFragment!=null) {
                listSessionsFragment.stopSwipeRefreshingSymbol();
            }
            return;
        }

        if (SystemClock.elapsedRealtime() - mLastGeoFireQueryUpdated < 300000) {
            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(weekday);
            if (listSessionsFragment!=null) {
                listSessionsFragment.stopSwipeRefreshingSymbol();
            }
            return;
        }
        geoFireNodesLoaded = false;
        checkIfGeoFireNodesAreLoaded();
    }

    /** INTERFACE triggered when list is scrolled setting behaviour of buttons */
    public void OnListSessionsScroll(int state) {
        if (state==SCROLL_STATE_IDLE) {
            sortAndFilterFAB.hide();
            sortAndFilterFAB.show();
            mapOrListBtn
                    .animate()
                    .translationY(0)
                    .withLayer()
                    .start();
        } else {
            sortAndFilterFAB.hide();
            mapOrListBtn.animate()
                    .translationY(400)
                    .withLayer()
                    .start();

        }
    }
    /** INTERFACE triggered when filter buttons are clicked, FILTERS sessions*/
    public void OnFilterByDistance(int filterDistance) {
        if (filterDistance> currentDefaultDistanceRadius) {
            currentDefaultDistanceRadius = filterDistance;
            currentDistanceRadius = currentDefaultDistanceRadius;
            loadGeoFireNodes();
            return;
        }
        currentDistanceRadius = filterDistance;
        if (filterDistance!= currentDefaultDistanceRadius && filterDistance!=DEFAULT_DISTANCE) {
            showFilteredItem("distance", getString(R.string.distance_colon) + DISTANCE_STRINGS_SE.get(currentDistanceRadius));
        } else {
            removeFilteredItem("distance");
        }

        filterGeoFireNodesAndPopulateListAndMap(false, 0);

    }

    public void OnMinPriceChanged(int minPrice, String currencyCountry) {

        this.minPrice = minPrice;

        if (minPrice!=0) {
            showFilteredItem("minPrice", getString(R.string.min_price_colon) + PRICES_STRINGS_SE.get(minPrice));
        } else {
            removeFilteredItem("minPrice");
        }

        filterGeoFireNodesAndPopulateListAndMap(false, 0);


    }

    public void OnTimeRangeChanged(int minHour, int minMinute, int maxHour, int maxMinute) {
        this.minHour = minHour;
        this.minMinute = minMinute;
        this.maxHour = maxHour;
        this.maxMinute = maxMinute;

        filterGeoFireNodesAndPopulateListAndMap(false, 0);

    }

    public void OnMaxPriceChanged(int maxPrice, String currencyCountry) {

        this.maxPrice = maxPrice;

        if (maxPrice!=PRICES_INTEGERS_SE.get("Max")) {
            showFilteredItem("maxPrice", getString(R.string.max_price_colon) + PRICES_STRINGS_SE.get(maxPrice));
        } else {
            removeFilteredItem("maxPrice");
        }

        filterGeoFireNodesAndPopulateListAndMap(false, 0);


    }

    public void OnSessionTypeChanged(HashMap<String, Boolean> sessionTypeChosen) {

        this.sessionTypeChosen = sessionTypeChosen;
        filterGeoFireNodesAndPopulateListAndMap(false, 0);


    }

    private void showFilteredItem(String filterType, String filterText) {
        boolean itemSet = false;
        for (TextView filteredItem: filteredItems) {
            if (filteredItem.getHint().equals(filterType)) {
                filteredItem.setText(filterText);
                filteredItem.setHint(filterType);
                filteredItem.setVisibility(View.VISIBLE);
                itemSet = true;
            }
        }
        if (!itemSet) {
            for (TextView filteredItem: filteredItems) {
                if (!itemSet) {
                    if (filteredItem.getHint().equals("")) {
                        filteredItem.setText(filterText);
                        filteredItem.setHint(filterType);
                        filteredItem.setVisibility(View.VISIBLE);
                        itemSet = true;
                    }
                }
            }
        }
    }

    private void removeFilteredItem(String filterType) {
        for (TextView filteredItem: filteredItems) {
            if (filteredItem.getHint().equals(filterType)) {
                filteredItem.setHint("");
                filteredItem.setText("");
                filteredItem.setVisibility(View.GONE);
            }
        }
    }

    private float getDistance(double latitude, double longitude, Location currentLocation){

        Location locationA = new Location("point A");
        locationA.setLatitude(currentLocation.getLatitude());
        locationA.setLongitude(currentLocation.getLongitude());
        Location locationB = new Location("point B");
        locationB.setLatitude(latitude);
        locationB.setLongitude(longitude);
        return  locationA.distanceTo(locationB);
    }

    public void OnLocationPicked(LatLng latLng, String requestType) {

    }

    // Makes a fragemnt name to fragments created by pager
    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof SortAndFilterFragment.OnFilterChangedListener) {
            onFilterChangedListener = (SortAndFilterFragment.OnFilterChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFilterChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFilterChangedListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exploreFragmentViewPager!=null) {
            exploreFragmentViewPager.setAdapter(null);
        }
        removeListeners();
        countDownTimerLocation.cancel();
        countDownTimerLocation = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
        if (exploreFragmentAdapter!=null) {
            exploreFragmentAdapter = null;
        }
    }

    public void removeListeners() {
        for (DatabaseReference sessionListenerRef: sessionListeners.keySet()) {
            if (sessionListeners.get(sessionListenerRef)!=null) {
                sessionListenerRef.removeEventListener(sessionListeners.get(sessionListenerRef));
            }
        }
        for (GeoQuery geoQuery: geofireListeners.keySet()) {
            if (geofireListeners.get(geoQuery)!=null) {
                geoQuery.removeAllListeners();
            }
        }
    }

    private void loadGeoFireNodes() {

        for (int x = 0; x < 14; x++) {
            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(x);
            if (listSessionsFragment!=null) {
                listSessionsFragment.firstLoadVisible(true);
                listSessionsFragment.noLocationPermissionVisible(false);
                listSessionsFragment.noLocationVisible(false);
            }
        }

        geoFireNodesLoaded = true;

        mLastGeoFireQueryUpdated = SystemClock.elapsedRealtime();

        geoFireNodes.clear();
        lastKeyEnteredTime = 0;
        firstKey = true;
        ArrayList<Task<?>> geoQueryTasks  = new ArrayList<>();
        geoQueryTasks.clear();

        Long todayTimestamp = System.currentTimeMillis();
        for (int weekday = 0; weekday < 14; weekday++) {
            TaskCompletionSource<Boolean> geoQuerySource = new TaskCompletionSource<>();
            Task geoQueryTask = geoQuerySource.getTask();
            geoQueryTasks.add(geoQueryTask);
            currentDay = weekday;

            Long dayTimestamp = new DateTime(todayTimestamp).plusDays(weekday).getMillis();
            String geoFireDateNode = TextTimestamp.textSDF(dayTimestamp);
            DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geoFire").child(geoFireDateNode);
            GeoFire geoFire = new GeoFire(mGeofireDbRef);
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), currentDefaultDistanceRadius);
            GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    geoFireNodes.put(key, location);
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    geoQuerySource.trySetResult(true);
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            };
            if (!geofireListeners.containsKey(geoQuery)) {
                geoQuery.addGeoQueryEventListener(geoQueryEventListener);
                geofireListeners.put(geoQuery, geoQueryEventListener);
            }

        }

        Tasks.whenAll(geoQueryTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (geoFireNodes.size()==0) {
                    if (currentDefaultDistanceRadius !=DISTANCE_INTEGERS_SE.get("100 mil")) {
                        currentDefaultDistanceRadius = DISTANCE_INTEGERS_SE.get("100 mil");
                        currentDistanceRadius = currentDefaultDistanceRadius;
                        loadGeoFireNodes();
                    }
                }

                if (task.isSuccessful()) {
                    geoFireNodesAndFragmentsUsed = false;
                    checkIfToLoadFragmentsWithData();
                }
            }
        });

    }

    public void updateWeekFragmentWithData(int weekday) {
        if (geoFireNodesLoaded) {
            filterGeoFireNodesAndPopulateListAndMap(true, weekday);
        }
    }

    private void filterGeoFireNodesAndPopulateListAndMap(boolean updateSingleFragment, int weekday) {
        geoFireNodesFiltered.clear();
        if (geoFireNodes.size()>0) {
            for (String geoFireNodeKey: geoFireNodes.keySet()) {
                boolean show = true;

                // User has made an active choice
                if (sessionTypeChosen.containsValue(true)) {
                    if (!sessionTypeChosen.containsKey(CharBuffer.wrap(geoFireNodeKey, 53, 56).toString())) {
                        show = false;
                    } else {
                        if (!sessionTypeChosen.get(CharBuffer.wrap(geoFireNodeKey, 53, 56).toString())) {
                            show = false;
                        }
                    }
                }

                if (minPrice!=PRICES_INTEGERS_SE.get("Min") ||  maxPrice!=PRICES_INTEGERS_SE.get("Max")) {
                    int price = Integer.parseInt(CharBuffer.wrap(geoFireNodeKey, 59, 63).toString());
                    if (price < minPrice || price > maxPrice) {
                        show = false;
                    }
                }

                if (minHour!=minDefaultHour || minMinute!=minDefaultMinute || maxHour!=maxDefaultHour || maxMinute!= maxDefaultMinute) {
                    DateTime adTime = new DateTime(Long.parseLong(CharBuffer.wrap(geoFireNodeKey, 0, 13).toString()));
                    DateTime minTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), minHour, minMinute);
                    DateTime maxTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), maxHour, maxMinute);
                    if (adTime.isBefore(minTime) || adTime.isAfter(maxTime)) {
                        show = false;
                    }
                }

                if (currentDistanceRadius!= currentDefaultDistanceRadius) {
                    float distanceRadiusFloat = (float) currentDistanceRadius;
                    if (getDistance(geoFireNodes.get(geoFireNodeKey).latitude, geoFireNodes.get(geoFireNodeKey).longitude, mLastKnownLocation) > distanceRadiusFloat*1000) {
                        show = false;
                    }
                }

                if (show) {
                    geoFireNodesFiltered.put(geoFireNodeKey, geoFireNodes.get(geoFireNodeKey));
                }
            }
        }


        // FILTERING DONE

        geoFireNodesKeys.clear();
        geoFireNodesKeys = new ArrayList<>(geoFireNodesFiltered.keySet());
        Collections.sort(geoFireNodesKeys);

        if (updateSingleFragment) {
            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(weekday);
            if (listSessionsFragment!=null) {
                listSessionsFragment.geoFireNodesUpdated(geoFireNodesKeys, mLastKnownLocation, weekday);
            }
        } else {
            for (int x = 0; x < 14; x++) {
                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(x);
                if (listSessionsFragment!=null) {
                    listSessionsFragment.geoFireNodesUpdated(geoFireNodesKeys, mLastKnownLocation, x);
                }
            }
        }



        HashMap<String, GeoLocation> sessionLocations = new HashMap<>();

        for (String geoFireNodeKey: geoFireNodesKeys) {
            String sessionId = CharBuffer.wrap(geoFireNodeKey, 33, 53).toString();
            sessionLocations.put(sessionId, geoFireNodesFiltered.get(geoFireNodeKey));
        }

        ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) fragmentManager.findFragmentByTag("ExploreMapsFragment");
        if (exploreMapsFragment!=null) {
            exploreMapsFragment.addMarkersToMap(sessionLocations);
        }

        return;

        // if filter
        /*if (distanceRadius!=DISTANCE_INTEGERS_SE.get("1000 km") || minPrice!=PRICES_INTEGERS_SE.get("Min") || maxPrice!=PRICES_INTEGERS_SE.get("Max") || minHour!=minDefaultHour || minMinute!=minDefaultMinute || maxHour!=maxDefaultHour || maxMinute!= maxDefaultMinute || sessionTypeChosen.containsValue(true)) {
            // FILTER
            for (String key: geoFireNodes.keySet()) {
                boolean show = true;

                // User has made an active choice
                if (sessionTypeChosen.containsValue(true)) {
                    if (!sessionTypeChosen.containsKey(sessionHashMap.get(advertisement.getSessionId()).getSessionType())) {
                        show = false;
                    }
                }

                if (distanceRadius!=DISTANCE_INTEGERS_SE.get("1000 km")) {
                    float distanceRadiusFloat = (float) distanceRadius;
                    if (getDistance(sessionHashMap.get(advertisement.getSessionId()).getLatitude(), sessionHashMap.get(advertisement.getSessionId()).getLongitude(), currentLocation) > distanceRadiusFloat) {
                        show = false;
                    }
                }
                if (minPrice!=PRICES_INTEGERS_SE.get("Min") ||  maxPrice!=PRICES_INTEGERS_SE.get("Max")) {
                    if (advertisement.getPrice() < minPrice || advertisement.getPrice() > maxPrice) {
                        show = false;
                    }
                }
                if (minHour!=minDefaultHour || minMinute!=minDefaultMinute || maxHour!=maxDefaultHour || maxMinute!= maxDefaultMinute) {
                    DateTime adTime = new DateTime(advertisement.getAdvertisementTimestamp());
                    DateTime minTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), minHour, minMinute);
                    DateTime maxTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), maxHour, maxMinute);
                    if (adTime.isBefore(minTime) || adTime.isAfter(maxTime)) {
                        show = false;
                    }
                }
                if (show) {
                    advertisementIdsAndTimestampsFilteredArrayList.add(new AdvertisementIdsAndTimestamps(advertisement.getAdvertisementId(), advertisement.getAdvertisementTimestamp()));
                }

                geoFireNodesFiltered.put(key, geoFireNodes.get(key));
            }
            Log.d("FOXMIKE_LOG", "Filtering ended");
            onFilterReadyListener.OnFilterReady();
        } else {

            Log.d("FOXMIKE_LOG", "Filtering ended");
            onFilterReadyListener.OnFilterReady();
        };*/

    }



    /*private void addListeners() {

        int loadedthisDay = 0;
        ArrayList<Task<?>> sessionTasks  = new ArrayList<>();
        sessionTasks.clear();
        sessionHashMap = new HashMap<>();
        while (loadedthisDay < sessionReferences.size()) {
            loadedthisDay++;

            String timestamp = CharBuffer.wrap(sessionReferences.get(loadedthisDay), 0, 13).toString();
            Long adTimestamp = Long.parseLong(timestamp);
            Long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp>adTimestamp) {
                break;
            }
            String sessionId = CharBuffer.wrap(sessionReferences.get(loadedthisDay), 36, sessionReferences.get(loadedthisDay).length()).toString();
            if (!sessionHashMap.containsKey(sessionId)) {
                TaskCompletionSource<Boolean> sessionSource = new TaskCompletionSource<>();
                Task sessionTask = sessionSource.getTask();
                sessionTasks.add(sessionTask);
                DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference().child("sessions").child(sessionId);
                FirebaseDatabaseViewModel sessionViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
                LiveData<DataSnapshot> sessionLiveData = sessionViewModel.getDataSnapshotLiveData(sessionRef);
                sessionLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
                    @Override
                    public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()!=null) {
                            sessionHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Session.class));
                        }
                        // -------------
                        sessionSource.trySetResult(true);
                    }
                });

            }
        }

        if (sessionTasks.isEmpty()) {
            TaskCompletionSource<String> dummySource = new TaskCompletionSource<>();
            Task dummyTask = dummySource.getTask();
            sessionTasks.add(dummyTask);
            dummySource.setResult("done");
        }

        Tasks.whenAll(sessionTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("FOXMIKE_LOG", "All data loaded");

                    filter(new OnSessionFilterReadyListener() {
                        @Override
                        public void OnSessionFilterReady() {

                        }
                    });
                }
            }
        });
    }

    private void filter(OnSessionFilterReadyListener onSessionFilterReadyListener) {
        sessionIdsFiltered.clear();
        Log.d("FOXMIKE_LOG", "Filtering sessions started");
        if (sessionHashMap.size()==0) {
            Log.d("FOXMIKE_LOG", "Filtering sessions ended");
            onSessionFilterReadyListener.OnSessionFilterReady();
            return;
        }
        if (distanceRadius!=DISTANCE_INTEGERS_SE.get("1000 km") || sessionTypeChosen.containsValue(true)) {
            // FILTER
            for (Session session: sessionHashMap.values()) {
                boolean show = true;

                // User has made an active choice
                if (sessionTypeChosen.containsValue(true)) {
                    if (!sessionTypeChosen.containsKey(session.getSessionType())) {
                        show = false;
                    }
                }

                if (distanceRadius!=DISTANCE_INTEGERS_SE.get("1000 km")) {
                    float distanceRadiusFloat = (float) distanceRadius;
                    if (getDistance(session.getLatitude(), session.getLongitude(), mLastKnownLocation) > distanceRadiusFloat) {
                        show = false;
                    }
                }
                if (show) {
                    sessionIdsFiltered.add(session.getSessionId());
                }

            }
            Log.d("FOXMIKE_LOG", "Session filtering ended");
            onSessionFilterReadyListener.OnSessionFilterReady();
        } else {
            for (Session session: sessionHashMap.values()) {
                sessionIdsFiltered.add(session.getSessionId());
            }
            Log.d("FOXMIKE_LOG", "Filtering ended");
            onSessionFilterReadyListener.OnSessionFilterReady();
        };

    }*/

    public interface OnSessionFilterReadyListener {
        void OnSessionFilterReady();
    }

    public interface OnFilterReadyListener {
        void OnFilterReady();
    }
}
