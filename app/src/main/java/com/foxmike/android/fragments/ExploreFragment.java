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
import com.foxmike.android.R;
import com.foxmike.android.adapters.BottomNavigationAdapter;
import com.foxmike.android.interfaces.OnNearSessionsAndAdvertisementsFoundListener;
import com.foxmike.android.interfaces.OnSessionsAndAdvertisementsFilteredListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.utils.WrapContentViewPager;
import com.google.android.gms.maps.model.LatLng;
import com.rd.PageIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private ArrayList<Session> sessionListArrayList = new ArrayList<>();
    private HashMap<String, Advertisement> advertisementHashMap = new HashMap<>();
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

    public ExploreFragment() {
        // Required empty public constructor
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
        setupListAndMapWithSessions();

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
                MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getItem(1);
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

        myFirebaseDatabase.getNearSessions(getActivity(), distanceRadius, new OnNearSessionsAndAdvertisementsFoundListener() {
            @Override
            public void OnNearSessionsFound(ArrayList<Session> nearSessions, HashMap<String, Advertisement> nearAdvertisements, Location location) {

                locationFound = true;
                locationAndViewUsed = false;
                onAsyncTaskFinished();

                sessionListArrayList.clear();
                sessionListArrayList = nearSessions;
                advertisementHashMap.clear();
                advertisementHashMap = nearAdvertisements;
                locationClosetoSessions = location;

                myFirebaseDatabase.filterSessionAndAdvertisements(nearSessions, advertisementHashMap, firstWeekdayHashMap, secondWeekdayHashMap, sortType, minPrice, maxPrice, new OnSessionsAndAdvertisementsFilteredListener() {
                    @Override
                    public void OnSessionsAndAdvertisementsFiltered(HashMap<String, Session> sessions, ArrayList<Advertisement> advertisements) {
                        ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getItem(0);
                        listSessionsFragment.updateSessionListView(sessions, advertisements, locationClosetoSessions);
                        listSessionsFragment.stopSwipeRefreshingSymbol();

                        MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getItem(1);
                        mapsFragment.addMarkersToMap(sessions);

                    }
                });

            }

            @Override
            public void OnLocationNotFound() {
                locationFound = false;
                locationAndViewUsed = false;
                onAsyncTaskFinished();

            }
        });
    }

    private void onAsyncTaskFinished() {
        if (getView()!=null && locationLoaded && locationAndViewUsed) {
            locationAndViewUsed = true;
            Toast.makeText(getContext(), R.string.location_not_found, Toast.LENGTH_LONG).show();
            ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getItem(0);
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

        MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getItem(1);


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

        myFirebaseDatabase.filterSessionAndAdvertisements(sessionListArrayList, advertisementHashMap, firstWeekdayHashMap, secondWeekdayHashMap, sortType, minPrice, maxPrice, new OnSessionsAndAdvertisementsFilteredListener() {
            @Override
            public void OnSessionsAndAdvertisementsFiltered(HashMap<String, Session> sessions, ArrayList<Advertisement> advertisements) {
                MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getItem(1);
                mapsFragment.addMarkersToMap(sessions);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getItem(0);
                listSessionsFragment.updateSessionListView(sessions, advertisements,locationClosetoSessions);

            }
        });

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
        myFirebaseDatabase.filterSessionAndAdvertisements(sessionListArrayList, advertisementHashMap, firstWeekdayHashMap, secondWeekdayHashMap, sortType, minPrice, maxPrice, new OnSessionsAndAdvertisementsFilteredListener() {
            @Override
            public void OnSessionsAndAdvertisementsFiltered(HashMap<String, Session> sessions, ArrayList<Advertisement> advertisements) {
                MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getItem(1);
                mapsFragment.addMarkersToMap(sessions);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getItem(0);
                listSessionsFragment.updateSessionListView(sessions, advertisements,locationClosetoSessions);
                listSessionsFragment.stopSwipeRefreshingSymbol();
            }
        });

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

        myFirebaseDatabase.filterSessionAndAdvertisements(sessionListArrayList, advertisementHashMap, firstWeekdayHashMap, secondWeekdayHashMap, sortType, minPrice, maxPrice, new OnSessionsAndAdvertisementsFilteredListener() {
            @Override
            public void OnSessionsAndAdvertisementsFiltered(HashMap<String, Session> sessions, ArrayList<Advertisement> advertisements) {
                MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getItem(1);
                mapsFragment.addMarkersToMap(sessions);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getItem(0);
                listSessionsFragment.updateSessionListView(sessions, advertisements,locationClosetoSessions);
                listSessionsFragment.stopSwipeRefreshingSymbol();
            }
        });
    }

    public void OnMaxPriceChanged(int maxPrice, String currencyCountry) {

        this.maxPrice = maxPrice;

        if (maxPrice!=PRICES_INTEGERS_SE.get("Max")) {
            showFilteredItem("maxPrice", getString(R.string.max_price_colon) + PRICES_STRINGS_SE.get(maxPrice));
        } else {
            removeFilteredItem("maxPrice");
        }

        myFirebaseDatabase.filterSessionAndAdvertisements(sessionListArrayList, advertisementHashMap, firstWeekdayHashMap, secondWeekdayHashMap, sortType, minPrice, maxPrice, new OnSessionsAndAdvertisementsFilteredListener() {
            @Override
            public void OnSessionsAndAdvertisementsFiltered(HashMap<String, Session> sessions, ArrayList<Advertisement> advertisements) {
                MapsFragment mapsFragment = (MapsFragment) exploreFragmentAdapter.getItem(1);
                mapsFragment.addMarkersToMap(sessions);

                ListSessionsFragment listSessionsFragment = (ListSessionsFragment) exploreFragmentAdapter.getItem(0);
                listSessionsFragment.updateSessionListView(sessions, advertisements,locationClosetoSessions);
                listSessionsFragment.stopSwipeRefreshingSymbol();
            }
        });
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

}