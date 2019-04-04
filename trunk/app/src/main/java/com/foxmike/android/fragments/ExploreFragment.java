package com.foxmike.android.fragments;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.foxmike.android.R;
import com.foxmike.android.adapters.BottomNavigationAdapter;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionAdvertisements;
import com.foxmike.android.utils.AdvertisementIdsAndTimestamps;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.utils.WrapContentViewPager;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rd.PageIndicatorView;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import static com.foxmike.android.utils.Distance.DISTANCE_INTEGERS_SE;
import static com.foxmike.android.utils.Distance.DISTANCE_STRINGS_SE;
import static com.foxmike.android.utils.Price.PRICES_INTEGERS_SE;
import static com.foxmike.android.utils.Price.PRICES_STRINGS_SE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment{

    public static final String TAG = ExploreFragment.class.getSimpleName();

    private MyFirebaseDatabase myFirebaseDatabase;
    public HashMap<String,Boolean> firstWeekdayHashMap;
    public HashMap<String,Boolean> secondWeekdayHashMap;
    private AHBottomNavigationViewPager exploreFragmentViewPager;
    private BottomNavigationAdapter exploreFragmentAdapter;
    private int distanceRadius;
    private String sortType;
    private Location locationClosetoSessions;
    private FragmentManager fragmentManager;
    private FloatingActionButton mapOrListBtn;
    private FloatingActionButton sortAndFilterFAB;
    private FloatingActionButton myLocationBtn;
    private View mainView;
    private float mapOrListBtnStartX;
    private float mapOrListBtnStartY;
    private Boolean started = false;
    private int minPrice = 0;
    private int maxPrice = PRICES_INTEGERS_SE.get("Max");
    private Boolean locationFound = false;
    private Boolean locationLoaded = false;
    private Boolean locationAndViewUsed = false;
    private TextView filteredItem1;
    private TextView filteredItem2;
    private TextView filteredItem3;
    private ArrayList<TextView> filteredItems = new ArrayList<>();
    private SortAndFilterFragment.OnFilterChangedListener onFilterChangedListener;
    private GeoFire geoFire;
    private DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    private Location currentLocation;
    private HashMap<String,Integer> sessionDistances = new HashMap<>();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<DatabaseReference, ValueEventListener> sessionListeners = new HashMap<>();
    private HashMap<DatabaseReference, ValueEventListener> advertisementListeners = new HashMap<>();
    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private HashMap<String, Advertisement> advertisementHashMap = new HashMap<>();
    private HashMap<String, String> advertisementSessionHashMap = new HashMap<>();
    private ToggleButton allDatesBtn;
    private int minHour = 0;
    private int minMinute = 0;
    private int maxHour = 23;
    private int maxMinute = 45;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GeoQuery geoQuery;
    private GeoQueryEventListener geoQueryEventListener;
    private HashMap<String, SessionAdvertisements> sessionAdsHashMap;
    private HashMap<String, Boolean> sessionTypeChosen = new HashMap<>();

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        setupListAndMapWithSessions();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myFirebaseDatabase = new MyFirebaseDatabase();

        /** Setup weekdayHashmaps*/
        firstWeekdayHashMap = new HashMap<String,Boolean>();
        secondWeekdayHashMap = new HashMap<String,Boolean>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar cal = Calendar.getInstance();
        for(int i=1; i<8; i++){
            String stringDate = sdf.format(cal.getTime());
            firstWeekdayHashMap.put(stringDate, true);
            cal.add(Calendar.DATE,1);
        }
        for(int i=1; i<8; i++){
            String stringDate = sdf.format(cal.getTime());
            secondWeekdayHashMap.put(stringDate, true);
            cal.add(Calendar.DATE,1);
        }

        fragmentManager = getChildFragmentManager();
        exploreFragmentAdapter = new BottomNavigationAdapter(fragmentManager);

        ListSessionsFragment listSessionsFragment = ListSessionsFragment.newInstance();
        exploreFragmentAdapter.addFragments(listSessionsFragment);

        ExploreMapsFragment exploreMapsFragment = ExploreMapsFragment.newInstance();
        exploreFragmentAdapter.addFragments(exploreMapsFragment);

        /** Setup List and Map with sessions*/
        sortType = "date";
        distanceRadius = DISTANCE_INTEGERS_SE.get("1000 km");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_explore, container, false);
        allDatesBtn = mainView.findViewById(R.id.allDatesBtn);

        allDatesBtn.setText(R.string.all);
        allDatesBtn.setTextOn(getResources().getString(R.string.all));
        allDatesBtn.setTextOff(getResources().getString(R.string.all));
        allDatesBtn.setChecked(true);

        allDatesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnWeekdayButtonClicked(0,0,new HashMap<>());
                allDatesBtn.setChecked(true);
            }
        });

        exploreFragmentViewPager = mainView.findViewById(R.id.exploreFragmentViewPager);
        exploreFragmentViewPager.setPagingEnabled(false);
        exploreFragmentViewPager.setAdapter(exploreFragmentAdapter);

        WrapContentViewPager weekdayViewpager = mainView.findViewById(R.id.weekdayPager);
        PageIndicatorView pageIndicatorView = mainView.findViewById(R.id.pageIndicatorView);

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
                        distanceRadius = DISTANCE_INTEGERS_SE.get("1000 km");
                        onFilterChangedListener.OnDistanceFilterChanged(distanceRadius);
                    }
                }
            });
        }

        // Setup weekdaypager
        weekdayViewpager.setAdapter(new WeekdayViewpagerAdapter(fragmentManager));
        pageIndicatorView.setViewPager(weekdayViewpager);

        mapOrListBtn = mainView.findViewById(R.id.map_or_list_button);
        mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
        sortAndFilterFAB = mainView.findViewById(R.id.sort_button);
        myLocationBtn = mainView.findViewById(R.id.my_location_button);
        myLocationBtn.hide();

        /** Setup mapOrList FAB*/
        mapOrListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exploreFragmentViewPager.getCurrentItem()==0) {
                    exploreFragmentViewPager.setCurrentItem(1, false);
                    switchMapOrListUI(true);
                } else {
                    exploreFragmentViewPager.setCurrentItem(0, false);
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
                sortAndFilterFragment = SortAndFilterFragment.newInstance(sortType, distanceRadius, minPrice, maxPrice, minHour, minMinute, maxHour, maxMinute, distanceRadius, sessionTypeChosen);
                sortAndFilterFragment.show(transaction,SortAndFilterFragment.TAG);
            }
        });

        // Setup my location button
        myLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
                exploreMapsFragment.goToMyLocation();
            }
        });

        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListAndMapWithSessions();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationAndViewUsed = false;
        onAsyncTaskFinished();
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

    private void setupListAndMapWithSessions() {
        myFirebaseDatabase= new MyFirebaseDatabase();
        // TODO if new filtersessions int is smaller than previous this function should only filter and not download

        FusedLocationProviderClient mFusedLocationClient;
        geoFire = new GeoFire(mGeofireDbRef);
        if (getActivity()==null) {
            locationFound = false;
            locationAndViewUsed = false;
            onAsyncTaskFinished();
            return;
        }
        getLocationPermission();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            currentLocation = task.getResult();
                            geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), distanceRadius);
                            geoQueryEventListener = new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    //Any location key which is within distanceRadius from the user's location will show up here as the key parameter in this method
                                    //You can fetch the actual data for this location by creating another firebase query here
                                    String distString = getDistance(location.latitude, location.longitude, currentLocation);
                                    Integer dist = Integer.parseInt(distString);
                                    sessionDistances.put(key, dist);
                                }

                                @Override
                                public void onKeyExited(String key) {
                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {
                                }

                                @Override
                                public void onGeoQueryReady() {

                                    Long currentTimestamp = System.currentTimeMillis();
                                    Long twoWeekTimestamp = new DateTime(currentTimestamp).plusWeeks(2).getMillis();

                                    sessionAdsHashMap = new HashMap<>();
                                    sessionAdsHashMap.clear();

                                    ArrayList<Task<?>> sessionAdvertisementTasks = new ArrayList<>();
                                    sessionAdvertisementTasks.clear();

                                    // Loop sessionIds and
                                    for (String sessionId : sessionDistances.keySet()) {
                                        TaskCompletionSource<DataSnapshot> sessionAdsSource = new TaskCompletionSource<>();
                                        Task sessionAdsTask = sessionAdsSource.getTask();
                                        sessionAdvertisementTasks.add(sessionAdsTask);
                                        // Find out if sessions has ads in near future
                                        Query query = FirebaseDatabase.getInstance().getReference().child("sessionAdvertisements").child(sessionId).orderByValue().startAt(currentTimestamp).endAt(twoWeekTimestamp);
                                        FirebaseDatabaseViewModel sessionAdvertisementsViewModel = ViewModelProviders.of(ExploreFragment.this).get(FirebaseDatabaseViewModel.class);
                                        LiveData<DataSnapshot> sessionAdvertisementsLiveData = sessionAdvertisementsViewModel.getDataSnapshotLiveData(query);
                                        sessionAdvertisementsLiveData.observe(ExploreFragment.this, new Observer<DataSnapshot>() {
                                            @Override
                                            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                                                SessionAdvertisements sessionAdvertisements = new SessionAdvertisements();
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    sessionAdvertisements.put(snapshot.getKey(), (Long) snapshot.getValue());
                                                }
                                                sessionAdsHashMap.put(dataSnapshot.getKey(), sessionAdvertisements);
                                                sessionAdsSource.trySetResult(dataSnapshot);
                                                if (dataSnapshot.getValue()==null) {
                                                    // no advertisements
                                                    return;
                                                }
                                            }
                                        });
                                    }

                                    if (sessionAdvertisementTasks.isEmpty()) {
                                        TaskCompletionSource<String> dummySource = new TaskCompletionSource<>();
                                        Task dummyTask = dummySource.getTask();
                                        sessionAdvertisementTasks.add(dummyTask);
                                        dummySource.setResult("done");
                                    }

                                    Tasks.whenAll(sessionAdvertisementTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            // All session keys within user area is now saved in sessionDistances
                                            ArrayList<Task<?>> sessionAndAdvertisementTasks = new ArrayList<>();
                                            sessionAndAdvertisementTasks.clear();

                                            for (String sessionId : sessionAdsHashMap.keySet()) {

                                                TaskCompletionSource<DataSnapshot> sessionSource = new TaskCompletionSource<>();
                                                Task sessionTask = sessionSource.getTask();
                                                sessionAndAdvertisementTasks.add(sessionTask);
                                                DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference().child("sessions").child(sessionId);
                                                FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(ExploreFragment.this).get(FirebaseDatabaseViewModel.class);
                                                LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(sessionRef);
                                                firebaseDatabaseLiveData.observe(ExploreFragment.this, new Observer<DataSnapshot>() {
                                                    @Override
                                                    public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.getValue()!=null) {
                                                            sessionHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Session.class));
                                                            ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
                                                            exploreMapsFragment.notifySessionChange(dataSnapshot.getKey(), sessionHashMap);

                                                            for (String advertisementId: advertisementSessionHashMap.keySet()) {
                                                                if (advertisementSessionHashMap.get(advertisementId).equals(dataSnapshot.getKey())) {
                                                                    ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);
                                                                    listSessionsFragment.notifyAdvertisementChange(advertisementId, advertisementHashMap, sessionHashMap);
                                                                }
                                                            }
                                                        } else {
                                                            // SESSION REMOVED
                                                            if (sessionHashMap.get(dataSnapshot.getKey())!=null) {
                                                                Session removedSession = sessionHashMap.get(dataSnapshot.getKey());
                                                                for (String removedAd: sessionAdsHashMap.get(removedSession.getSessionId()).keySet()) {
                                                                    ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);
                                                                    listSessionsFragment.notifyAdvertisementRemoved(new AdvertisementIdsAndTimestamps(removedAd, sessionAdsHashMap.get(removedSession.getSessionId()).get(removedAd)));
                                                                }
                                                                ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
                                                                exploreMapsFragment.notifySessionRemoved(dataSnapshot.getKey());
                                                                sessionHashMap.remove(dataSnapshot.getKey());
                                                            }
                                                        }
                                                        sessionSource.trySetResult(dataSnapshot);
                                                    }
                                                });

                                                if (sessionAdsHashMap.get(sessionId)!=null) {
                                                    for (String advertisementId : sessionAdsHashMap.get(sessionId).keySet()) {
                                                        DatabaseReference advRef = dbRef.child("advertisements").child(advertisementId);
                                                        TaskCompletionSource<DataSnapshot> adSource = new TaskCompletionSource<>();
                                                        Task adTask = adSource.getTask();
                                                        sessionAndAdvertisementTasks.add(adTask);

                                                        FirebaseDatabaseViewModel advertisementViewModel = ViewModelProviders.of(ExploreFragment.this).get(FirebaseDatabaseViewModel.class);
                                                        LiveData<DataSnapshot> advertisementeLiveData = advertisementViewModel.getDataSnapshotLiveData(advRef);
                                                        advertisementeLiveData.observe(ExploreFragment.this, new Observer<DataSnapshot>() {
                                                            @Override
                                                            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                                                                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);

                                                                if (dataSnapshot.getValue()==null) {
                                                                    listSessionsFragment.notifyAdvertisementRemoved(new AdvertisementIdsAndTimestamps(dataSnapshot.getKey(), sessionAdsHashMap.get(sessionId).get(advertisementId)));
                                                                } else {
                                                                    Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                                                                    if (advertisement.getStatus().equals("cancelled")) {
                                                                        if (advertisementHashMap.containsKey(advertisement.getAdvertisementId())) {
                                                                            advertisementHashMap.remove(advertisement.getAdvertisementId());
                                                                        }
                                                                        if (advertisementSessionHashMap.containsKey(advertisement.getAdvertisementId())) {
                                                                            advertisementSessionHashMap.remove(advertisement.getAdvertisementId());
                                                                        }
                                                                        listSessionsFragment.notifyAdvertisementRemoved(new AdvertisementIdsAndTimestamps(advertisement.getAdvertisementId(), advertisement.getAdvertisementTimestamp()));
                                                                    } else {
                                                                        advertisementHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Advertisement.class));
                                                                        advertisementSessionHashMap.put(advertisement.getAdvertisementId(), advertisement.getSessionId());
                                                                        listSessionsFragment.notifyAdvertisementChange(dataSnapshot.getKey(), advertisementHashMap, sessionHashMap);
                                                                    }
                                                                }
                                                                adSource.trySetResult(dataSnapshot);

                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            if (sessionAndAdvertisementTasks.isEmpty()) {
                                                TaskCompletionSource<String> dummySource = new TaskCompletionSource<>();
                                                Task dummyTask = dummySource.getTask();
                                                sessionAndAdvertisementTasks.add(dummyTask);
                                                dummySource.setResult("done");
                                            }

                                            Tasks.whenAll(sessionAndAdvertisementTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        locationFound = true;
                                                        locationAndViewUsed = false;
                                                        onAsyncTaskFinished();

                                                        filterSessionAndAdvertisements();

                                                    }
                                                }
                                            });
                                        }
                                    });



                                    if (sessionDistances.size() < 1) {
                                        filterSessionAndAdvertisements();
                                    }
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                }
                            };

                            geoQuery.addGeoQueryEventListener(geoQueryEventListener);

                        } else {
                            // no location
                            locationFound = false;
                            locationAndViewUsed = false;
                            onAsyncTaskFinished();
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            // no location
            locationFound = false;
            locationAndViewUsed = false;
            onAsyncTaskFinished();
        }
    }

    public void filterSessionAndAdvertisements() {
        // sessionArray will be an array of the near sessions filtered
        ArrayList<String> sessionIdsFiltered = new ArrayList<>();
        ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList = new ArrayList<>();
        // save the current time in a timestamp to compare with the advertisement timestamps
        Long currentTimestamp = System.currentTimeMillis();
        // Filter sessions not part of weekdays
        for (Session nearSession : sessionHashMap.values()) {

            boolean show = false;

            // User has made an active choice
            if (sessionTypeChosen.containsValue(true)) {
                if (!sessionTypeChosen.containsKey(nearSession.getSessionType())) {
                    show = false;
                } else if (sessionTypeChosen.get(nearSession.getSessionType())) {
                    show = true;
                }
            } else {
                // User has NOT made an active choice
                show = true;
            }

            if (show) {
                // create a boolean to keep track if this session has been added to the sessionArray or not
                boolean sessionAdded = false;
                if (sessionAdsHashMap.get(nearSession.getSessionId())!=null) {
                    if ((sessionDistances.get(nearSession.getSessionId())/1000)<=distanceRadius) {
                        // loop through all the advertisement timestamps found under session/adIds
                        for (String advertisementKey: sessionAdsHashMap.get(nearSession.getSessionId()).keySet()) {
                            // If part of weekday filter
                            long advertisementTimestamp = sessionAdsHashMap.get(nearSession.getSessionId()).get(advertisementKey);
                            if (firstWeekdayHashMap.containsKey(TextTimestamp.textSDF(advertisementTimestamp))) {
                                if (firstWeekdayHashMap.get(TextTimestamp.textSDF(advertisementTimestamp))) {
                                    // if time has not passed
                                    if (advertisementTimestamp > currentTimestamp) {
                                        if (advertisementHashMap.containsKey(advertisementKey)) {
                                            if (advertisementHashMap.get(advertisementKey).getPrice()>=minPrice && advertisementHashMap.get(advertisementKey).getPrice()<=maxPrice) {
                                                DateTime adTime = new DateTime(advertisementHashMap.get(advertisementKey).getAdvertisementTimestamp());
                                                DateTime minTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), minHour, minMinute);
                                                DateTime maxTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), maxHour, maxMinute);
                                                if ((adTime.isAfter(minTime) || adTime.isEqual(minTime)) && (adTime.isBefore(maxTime) || adTime.isEqual(maxTime))) {
                                                    advertisementIdsAndTimestampsFilteredArrayList.add(new AdvertisementIdsAndTimestamps(advertisementKey, advertisementTimestamp));
                                                    // if this session hasn't already been saved to sessionArray save it
                                                    if (!sessionAdded) {
                                                        sessionIdsFiltered.add(nearSession.getSessionId());
                                                        sessionAdded = true;
                                                    }
                                                }

                                            }

                                        }

                                    }
                                }
                            }
                            // same for secondWeek of the filter (I have one hashmap for each week)
                            if (secondWeekdayHashMap.containsKey(TextTimestamp.textSDF(advertisementTimestamp))) {
                                if (secondWeekdayHashMap.get(TextTimestamp.textSDF(advertisementTimestamp))) {
                                    if (advertisementTimestamp > currentTimestamp) {
                                        if (advertisementHashMap.containsKey(advertisementKey)) {
                                            if (advertisementHashMap.get(advertisementKey).getPrice()>=minPrice && advertisementHashMap.get(advertisementKey).getPrice()<=maxPrice) {
                                                DateTime adTime = new DateTime(advertisementHashMap.get(advertisementKey).getAdvertisementTimestamp());
                                                DateTime minTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), minHour, minMinute);
                                                DateTime maxTime = new DateTime(adTime.getYear(), adTime.getMonthOfYear(), adTime.getDayOfMonth(), maxHour, maxMinute);
                                                if ((adTime.isAfter(minTime) || adTime.isEqual(minTime)) && (adTime.isBefore(maxTime) || adTime.isEqual(maxTime))) {
                                                    advertisementIdsAndTimestampsFilteredArrayList.add(new AdvertisementIdsAndTimestamps(advertisementKey, advertisementTimestamp));
                                                    // if this session hasn't already been saved to sessionArray save it
                                                    if (!sessionAdded) {
                                                        sessionIdsFiltered.add(nearSession.getSessionId());
                                                        sessionAdded = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // If array should be sorted by date sort it by date
        if (sortType.equals("date")) {
            Collections.sort(advertisementIdsAndTimestampsFilteredArrayList);
            TextTimestamp prevTextTimestamp = new TextTimestamp();
            // Throw in a ad dummy containing the dateheader for every new day so that these can be used in the list
            int i = 0;
            while (i < advertisementIdsAndTimestampsFilteredArrayList.size()) {
                if (!prevTextTimestamp.textSDF().equals(TextTimestamp.textSDF(advertisementIdsAndTimestampsFilteredArrayList.get(i).getAdTimestamp()))) {
                    AdvertisementIdsAndTimestamps dummyAdvertisementIdAndTimestamp = new AdvertisementIdsAndTimestamps("dateHeader", advertisementIdsAndTimestampsFilteredArrayList.get(i).getAdTimestamp());
                    advertisementIdsAndTimestampsFilteredArrayList.add(i, dummyAdvertisementIdAndTimestamp);
                    prevTextTimestamp = new TextTimestamp(advertisementIdsAndTimestampsFilteredArrayList.get(i).getAdTimestamp());
                }
                i++;
            }
        }

        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);
        listSessionsFragment.updateSessionListView(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);
        listSessionsFragment.stopSwipeRefreshingSymbol();

        ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
        exploreMapsFragment.addMarkersToMap(sessionIdsFiltered, sessionHashMap, sessionAdsHashMap);

    }

    private void onAsyncTaskFinished() {
        if (getView()!=null && locationLoaded && locationAndViewUsed) {
            locationAndViewUsed = true;
            Toast.makeText(getActivity().getApplicationContext(), R.string.location_not_found, Toast.LENGTH_LONG).show();
            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);
            listSessionsFragment.emptyListView();
            listSessionsFragment.stopSwipeRefreshingSymbol();
        }
    }

    private void switchMapOrListUI(boolean mapIsVisible) {

        int width = mainView.getRight();
        int height = mainView.getBottom();
        float fabDiameter = convertDpToPx(getActivity().getApplicationContext(), 56);

        if (width>0 && height>0 && fabDiameter>0) {
            mapOrListBtnStartX = width/2 - fabDiameter/2;
            mapOrListBtnStartY = height -  convertDpToPx(getActivity().getApplicationContext(), 20) - fabDiameter;
            float Xcontrol2 = width - convertDpToPx(getActivity().getApplicationContext(),72);
            float Ycontrol2 = sortAndFilterFAB.getY() + convertDpToPx(getActivity().getApplicationContext(), 144);

            ExploreMapsFragment exploreMapsFragment = (ExploreMapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);


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

    /** INTERFACE to change weekday hashmaps based on inputs */
    public void OnWeekdayChanged(int week, String weekdayKey, Boolean weekdayBoolean, Activity activity) {
        if (week==1) {
            firstWeekdayHashMap.put(weekdayKey,weekdayBoolean);
        }
        if (week==2) {
            secondWeekdayHashMap.put(weekdayKey,weekdayBoolean);
        }
    }
    /** INTERFACE to refilter sessions in List and Map based on weekday hashmaps */
    public void OnWeekdayButtonClicked(int week, int button, HashMap<Integer, Boolean> toggleHashMap) {
        HashMap<Integer, Boolean> toggleMap1;
        HashMap<Integer, Boolean> toggleMap2;
        final FragmentManager fragmentManager = getChildFragmentManager();
        WeekdayFilterFragment weekdayFilterFragment = (WeekdayFilterFragment) fragmentManager.findFragmentByTag(makeFragmentName(R.id.weekdayPager,0));
        WeekdayFilterFragment weekdayFilterFragmentB = (WeekdayFilterFragment) fragmentManager.findFragmentByTag(makeFragmentName(R.id.weekdayPager,1));

        toggleMap1 = weekdayFilterFragment.getToggleMap1();
        toggleMap2 = weekdayFilterFragmentB.getToggleMap2();

        weekdayFilterFragment.changeToggleMap(week,button,toggleMap1,toggleMap2);
        weekdayFilterFragmentB.changeToggleMap(week,button,toggleMap1,toggleMap2);

        toggleMap1 = weekdayFilterFragment.getAndUpdateToggleMap1();
        weekdayFilterFragmentB.setToggleMap1(toggleMap1);
        toggleMap2 = weekdayFilterFragmentB.getAndUpdateToggleMap2();
        weekdayFilterFragment.setToggleMap2(toggleMap2);

        filterSessionAndAdvertisements();

    }
    /** INTERFACE triggered when list is scrolled REFRESHED, downloads all sessions based on input distance radius*/
    public void OnRefreshSessions() {
        setupListAndMapWithSessions();
    }

    /** INTERFACE triggered when list is scrolled setting behaviour of buttons */
    public void OnListSessionsScroll(int dy) {
        if (dy > 0 && !started) {
            started = true;
            sortAndFilterFAB.hide();
            mapOrListBtn.animate()
                    .translationY(400)
                    .withLayer()
                    .start();

        } else if (dy < 0 && started) {
            started = false;
            sortAndFilterFAB.hide();
            sortAndFilterFAB.show();
            mapOrListBtn
                    .animate()
                    .translationY(0)
                    .withLayer()
                    .start();
        }

    }
    /** INTERFACE triggered when sort buttons are clicked, SORTS sessions*/
    public void OnChangeSortType(String sortType) {
        this.sortType = sortType;
        filterSessionAndAdvertisements();

    }
    /** INTERFACE triggered when filter buttons are clicked, FILTERS sessions*/
    public void OnFilterByDistance(int filterDistance) {
        distanceRadius = filterDistance;
        if (filterDistance!= DISTANCE_INTEGERS_SE.get("1000 km")) {
            showFilteredItem("distance", getString(R.string.distance_colon) + DISTANCE_STRINGS_SE.get(distanceRadius));
        } else {
            removeFilteredItem("distance");
        }
        filterSessionAndAdvertisements();
    }

    public void OnMinPriceChanged(int minPrice, String currencyCountry) {

        this.minPrice = minPrice;

        if (minPrice!=0) {
            showFilteredItem("minPrice", getString(R.string.min_price_colon) + PRICES_STRINGS_SE.get(minPrice));
        } else {
            removeFilteredItem("minPrice");
        }

        filterSessionAndAdvertisements();
    }

    public void OnTimeRangeChanged(int minHour, int minMinute, int maxHour, int maxMinute) {
        this.minHour = minHour;
        this.minMinute = minMinute;
        this.maxHour = maxHour;
        this.maxMinute = maxMinute;

        filterSessionAndAdvertisements();
    }

    public void OnMaxPriceChanged(int maxPrice, String currencyCountry) {

        this.maxPrice = maxPrice;

        if (maxPrice!=PRICES_INTEGERS_SE.get("Max")) {
            showFilteredItem("maxPrice", getString(R.string.max_price_colon) + PRICES_STRINGS_SE.get(maxPrice));
        } else {
            removeFilteredItem("maxPrice");
        }

        filterSessionAndAdvertisements();
    }

    public void OnSessionTypeChanged(HashMap<String, Boolean> sessionTypeChosen) {

        this.sessionTypeChosen = sessionTypeChosen;

        filterSessionAndAdvertisements();
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

    private String getDistance(double latitude, double longitude, Location currentLocation){

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

    public void OnLocationPicked(LatLng latLng, String requestType) {

    }

    // Sets up weekday pager
    class WeekdayViewpagerAdapter extends FragmentPagerAdapter {
        public WeekdayViewpagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = WeekdayFilterFragment.newInstance(1);
            }
            if (position == 1) {
                fragment = WeekdayFilterFragment.newInstance(2);
            }
            return fragment;
        }
        @Override
        public int getCount() {
            return 2;
        }
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
        allDatesBtn = null;
        if (exploreFragmentViewPager!=null) {
            exploreFragmentViewPager.setAdapter(null);
        }
        removeListeners();
        if (geoQuery!=null && geoQueryEventListener!=null) {
            geoQuery.removeAllListeners();
        }

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
        for (DatabaseReference advertisementListenerRef: advertisementListeners.keySet()) {
            if (advertisementListeners.get(advertisementListenerRef)!=null) {
                advertisementListenerRef.removeEventListener(advertisementListeners.get(advertisementListenerRef));
            }
        }
    }
}
