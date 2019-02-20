package com.foxmike.android.fragments;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.foxmike.android.R;
import com.foxmike.android.adapters.BottomNavigationAdapter;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.AdvertisementIdsAndTimestamps;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.utils.WrapContentViewPager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rd.PageIndicatorView;

import org.joda.time.DateTime;
import org.joda.time.Duration;

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
    private SortAndFilterFragment sortAndFilterFragment;
    private View view;
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
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofire");
    private Location currentLocation;
    private HashMap<String,Integer> sessionDistances = new HashMap<>();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<DatabaseReference, ValueEventListener> sessionListeners = new HashMap<>();
    private HashMap<DatabaseReference, ValueEventListener> advertisementListeners = new HashMap<>();
    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private HashMap<String, Advertisement> advertisementHashMap = new HashMap<>();
    private HashMap<String, String> advertisementSessionHashMap = new HashMap<>();

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

        MapsFragment mapsFragment = MapsFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
        mapsFragment.setArguments(bundle);
        exploreFragmentAdapter.addFragments(mapsFragment);

        /** Setup List and Map with sessions*/
        sortType = "date";
        distanceRadius = DISTANCE_INTEGERS_SE.get("Max");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_explore, container, false);

        exploreFragmentViewPager = view.findViewById(R.id.exploreFragmentViewPager);
        exploreFragmentViewPager.setPagingEnabled(false);
        exploreFragmentViewPager.setAdapter(exploreFragmentAdapter);

        WrapContentViewPager weekdayViewpager = view.findViewById(R.id.weekdayPager);
        PageIndicatorView pageIndicatorView = view.findViewById(R.id.pageIndicatorView);

        // filter buttons
        filteredItem1 = view.findViewById(R.id.filteredItem1);
        filteredItem2 = view.findViewById(R.id.filteredItem2);
        filteredItem3 = view.findViewById(R.id.filteredItem3);

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
                        distanceRadius = DISTANCE_INTEGERS_SE.get("Max");
                        onFilterChangedListener.OnDistanceFilterChanged(distanceRadius);
                    }
                }
            });
        }

        // Setup weekdaypager
        weekdayViewpager.setAdapter(new WeekdayViewpagerAdapter(fragmentManager));
        pageIndicatorView.setViewPager(weekdayViewpager);

        mapOrListBtn = view.findViewById(R.id.map_or_list_button);
        mapOrListBtn.setImageDrawable(getResources().getDrawable(R.mipmap.ic_location_on_black_24dp));
        sortAndFilterFAB = view.findViewById(R.id.sort_button);
        myLocationBtn = view.findViewById(R.id.my_location_button);
        myLocationBtn.setVisibility(View.GONE);

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
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (sortAndFilterFragment!=null) {
                    transaction.remove(sortAndFilterFragment);
                }
                sortAndFilterFragment = SortAndFilterFragment.newInstance(sortType, distanceRadius, minPrice, maxPrice);
                sortAndFilterFragment.show(transaction,"sortAndFilterFragment");
            }
        });

        // Setup my location button
        myLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
                mapsFragment.goToMyLocation();
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationAndViewUsed = false;
        onAsyncTaskFinished();
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    currentLocation = location;
                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), distanceRadius);
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
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

                            ArrayList<Task<?>> sessionTasks = new ArrayList<>();
                            sessionTasks.clear();
                            ArrayList<Task<?>> advertisementTasks = new ArrayList<>();
                            advertisementTasks.clear();

                            Long currentTimestamp = System.currentTimeMillis();
                            DateTime currentTime = new DateTime(currentTimestamp);

                            // Download all the near sessions
                            for (String sessionId : sessionDistances.keySet()) {

                                DatabaseReference sessionRef = dbRef.child("sessions").child(sessionId);
                                if (!sessionListeners.containsKey(sessionRef)) {
                                    TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
                                    Task dbTask = dbSource.getTask();
                                    ValueEventListener sessionListener = sessionRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            dbSource.trySetResult(dataSnapshot);
                                            if (dataSnapshot.getValue()!=null) {
                                                sessionHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Session.class));
                                                MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
                                                mapsFragment.notifySessionChange(dataSnapshot.getKey(), sessionHashMap);

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
                                                    for (String removedAd: removedSession.getAdvertisements().keySet()) {
                                                        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);
                                                        listSessionsFragment.notifyAdvertisementRemoved(new AdvertisementIdsAndTimestamps(removedAd, removedSession.getAdvertisements().get(removedAd)));
                                                        advertisementHashMap.remove(removedAd);
                                                    }
                                                    MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
                                                    mapsFragment.notifySessionRemoved(dataSnapshot.getKey());
                                                    sessionHashMap.remove(dataSnapshot.getKey());
                                                }
                                            }

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            dbSource.setException(databaseError.toException());
                                        }
                                    });
                                    sessionTasks.add(dbTask);
                                    sessionListeners.put(sessionRef, sessionListener);
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

                                        for (Session session: sessionHashMap.values()) {
                                            for (String advertisementKey: session.getAdvertisements().keySet()) {
                                                DateTime advertisementTime = new DateTime(session.getAdvertisements().get(advertisementKey));
                                                Duration durationCurrentToAdvertisment = new Duration(currentTime, advertisementTime);
                                                if (advertisementTime.isAfter(currentTime) && durationCurrentToAdvertisment.getStandardDays()<15) {
                                                    DatabaseReference advRef = dbRef.child("advertisements").child(advertisementKey);
                                                    if (!advertisementListeners.containsKey(advRef)) {
                                                        TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
                                                        Task dbTask = dbSource.getTask();
                                                        ValueEventListener advertisementListener = advRef.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);
                                                                dbSource.trySetResult(dataSnapshot);
                                                                if (dataSnapshot.getValue()==null) {
                                                                    listSessionsFragment.notifyAdvertisementRemoved(new AdvertisementIdsAndTimestamps(dataSnapshot.getKey(), session.getAdvertisements().get(dataSnapshot.getKey())));
                                                                } else {
                                                                    Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                                                                    advertisementHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Advertisement.class));
                                                                    advertisementSessionHashMap.put(advertisement.getAdvertisementId(), advertisement.getSessionId());
                                                                    listSessionsFragment.notifyAdvertisementChange(dataSnapshot.getKey(), advertisementHashMap, sessionHashMap);
                                                                }

                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                dbSource.setException(databaseError.toException());
                                                            }
                                                        });
                                                        advertisementTasks.add(dbTask);
                                                        advertisementListeners.put(advRef, advertisementListener);
                                                    }
                                                }
                                            }
                                        }

                                        if (advertisementTasks.isEmpty()) {
                                            TaskCompletionSource<String> dummySource = new TaskCompletionSource<>();
                                            Task dummyTask = dummySource.getTask();
                                            advertisementTasks.add(dummyTask);
                                            dummySource.setResult("done");
                                        }


                                        Tasks.whenAll(advertisementTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    /*for (Advertisement ad :advertisementHashMap.values()) {
                                                        advertisementIdsAndTimestamps.add(new AdvertisementIdsAndTimestamps(ad.getAdvertisementId(), ad.getAdvertisementTimestamp()));
                                                    }

                                                    Collections.sort(advertisementIdsAndTimestamps);*/
                                                    locationFound = true;
                                                    locationAndViewUsed = false;
                                                    onAsyncTaskFinished();

                                                    filterSessionAndAdvertisements();
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                            if (sessionDistances.size() < 1) {
                                filterSessionAndAdvertisements();
                            }
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {
                        }
                    });
                } else {
                    locationFound = false;
                    locationAndViewUsed = false;
                    onAsyncTaskFinished();
                }

            }
        });
    }

    public void filterSessionAndAdvertisements() {
        // sessionArray will be an array of the near sessions filtered
        ArrayList<String> sessionIdsFiltered = new ArrayList<>();
        ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList = new ArrayList<>();
        // save the current time in a timestamp to compare with the advertisement timestamps
        Long currentTimestamp = System.currentTimeMillis();
        // Filter sessions not part of weekdays
        for (Session nearSession : sessionHashMap.values()) {
            // create a boolean to keep track if this session has been added to the sessionArray or not
            boolean sessionAdded = false;
            if (nearSession.getAdvertisements()!=null) {
                // loop through all the advertisement timestamps found under session/adIds
                for (String advertisementKey: nearSession.getAdvertisements().keySet()) {
                    // If part of weekday filter
                    long advertisementTimestamp = nearSession.getAdvertisements().get(advertisementKey);
                    if (firstWeekdayHashMap.containsKey(TextTimestamp.textSDF(advertisementTimestamp))) {
                        if (firstWeekdayHashMap.get(TextTimestamp.textSDF(advertisementTimestamp))) {
                            // if time has not passed
                            if (advertisementTimestamp > currentTimestamp) {
                                if (advertisementHashMap.get(advertisementKey).getPrice()>=minPrice && advertisementHashMap.get(advertisementKey).getPrice()<=maxPrice) {
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
                    // same for secondWeek of the filter (I have one hashmap for each week)
                    if (secondWeekdayHashMap.containsKey(TextTimestamp.textSDF(advertisementTimestamp))) {
                        if (secondWeekdayHashMap.get(TextTimestamp.textSDF(advertisementTimestamp))) {
                            if (advertisementTimestamp > currentTimestamp) {
                                if (advertisementHashMap.get(advertisementKey).getPrice()>=minPrice && advertisementHashMap.get(advertisementKey).getPrice()<=maxPrice) {
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

        MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);
        mapsFragment.addMarkersToMap(sessionIdsFiltered, sessionHashMap);

    }

    private void onAsyncTaskFinished() {
        if (getView()!=null && locationLoaded && locationAndViewUsed) {
            locationAndViewUsed = true;
            Toast.makeText(getContext(), R.string.location_not_found, Toast.LENGTH_LONG).show();
            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getRegisteredFragment(0);
            listSessionsFragment.emptyListView();
            listSessionsFragment.stopSwipeRefreshingSymbol();
        }
    }

    private void switchMapOrListUI(boolean mapIsVisible) {

        int width = view.getRight();
        int height = view.getBottom();
        float fabDiameter = convertDpToPx(getActivity(), 56);

        mapOrListBtnStartX = width/2 - fabDiameter/2;
        mapOrListBtnStartY = height -  convertDpToPx(getActivity(), 20) - fabDiameter;
        float Xcontrol2 = width - convertDpToPx(getActivity(),72);
        float Ycontrol2 = sortAndFilterFAB.getY() + convertDpToPx(getActivity(), 144);

        MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getRegisteredFragment(1);


        if (mapIsVisible) {
            mapsFragment.showRecylerView(true);
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
            mapsFragment.showRecylerView(false);
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
        if (filterDistance!= DISTANCE_INTEGERS_SE.get("Max")) {
            showFilteredItem("distance", getString(R.string.distance_colon) + DISTANCE_STRINGS_SE.get(distanceRadius));
        } else {
            removeFilteredItem("distance");
        }
        setupListAndMapWithSessions();
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

    public void OnMaxPriceChanged(int maxPrice, String currencyCountry) {

        this.maxPrice = maxPrice;

        if (maxPrice!=PRICES_INTEGERS_SE.get("Max")) {
            showFilteredItem("maxPrice", getString(R.string.max_price_colon) + PRICES_STRINGS_SE.get(maxPrice));
        } else {
            removeFilteredItem("maxPrice");
        }

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
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
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
