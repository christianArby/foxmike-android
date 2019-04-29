package com.foxmike.android.fragments;
// Checked

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSessionsAdapter;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.AdvertisementIdsAndTimestamps;
import com.foxmike.android.utils.HeaderItemDecoration;
import com.foxmike.android.utils.PaginationScrollListener;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.foxmike.android.utils.Distance.DISTANCE_INTEGERS_SE;
import static com.foxmike.android.utils.Price.PRICES_INTEGERS_SE;
import static com.foxmike.android.utils.StaticResources.maxDefaultHour;
import static com.foxmike.android.utils.StaticResources.maxDefaultMinute;
import static com.foxmike.android.utils.StaticResources.minDefaultHour;
import static com.foxmike.android.utils.StaticResources.minDefaultMinute;

/**
 * This fragment creates a list of sessions based on an arraylist of session objects given as arguments. It also
 * uses an location object in order to sort the sessions on distance from user
 */
public class  ListSessionsFragment extends Fragment {

    public static final String TAG = ListSessionsFragment.class.getSimpleName();

    private RecyclerView mSessionList;
    private ListSessionsAdapter sessionsAdapter;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;
    private OnRefreshSessionsListener onRefreshSessionsListener;
    private SwipeRefreshLayout listSessionsSwipeRefreshLayout;
    private OnListSessionsScrollListener onListSessionsScrollListener;
    //private TextView noSessionsFound;
    private boolean sessionsLoaded;
    private boolean locationLoaded;
    private boolean sessionsAndLocationUsed;
    private boolean swipeRefreshStatusLoaded;
    private boolean swipeRefreshStatusUsed;
    //private TextView noContent;
    //private DotProgressBar progressBar;
    private boolean loading = false;

    private View mainView;

    private long lastKeyEnteredTime = 0;
    private boolean firstKey = true;
    private ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList  = new ArrayList<>();
    private ArrayList<String> sessionIdsFiltered  = new ArrayList<>();
    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private HashMap<String, Advertisement> advertisementHashMap = new HashMap<>();
    private int loadedthisDay = 0;
    private int itemsToLoadPerDayEachTime = 20;
    private int totalAdsToLoadThisDay;

    private HashMap<GeoQuery, GeoQueryEventListener> geofireListeners = new HashMap<>();
    private boolean allLoaded;
    private int weekday;
    //private TextView dateHeaderTV;

    private int minPrice = PRICES_INTEGERS_SE.get("Min");
    private int maxPrice = PRICES_INTEGERS_SE.get("Max");
    private int minHour = minDefaultHour;
    private int minMinute = minDefaultMinute;
    private int maxHour = maxDefaultHour;
    private int maxMinute = maxDefaultMinute;
    private int distanceRadius = DISTANCE_INTEGERS_SE.get("1000 km");
    private HashMap<String, Boolean> sessionTypeChosen = new HashMap<>();
    ArrayList<String> geoFireNodesKeys = new ArrayList<>();
    private DotProgressBar firsLoadProgressBar;

    private boolean geoFireLoaded = false;
    private boolean firstLoad = true;



    //private TextView noContentWithLink;
    private OnNextDayWithSessionsClickedListener onNextDayWithSessionsClickedListener;
    private ArrayList<String> geoFireNodesKeysFromThisDay = new ArrayList<>();
    private boolean listenersAdded;



    // Index from which pagination should start (0 is 1st page in our case)
    private static final int PAGE_START = 1;

    // Indicates if footer ProgressBar is shown (i.e. next page is loading)
    private boolean isLoading = false;

    // If current page is the last page (Pagination will stop after this page load)
    private boolean isLastPage = false;

    // total no. of pages to load. Initial load is page 0, after which 2 more pages will load.
    private int TOTAL_PAGES;
    // indicates the current page which Pagination is fetching.
    private int currentPage = PAGE_START;
    private LinearLayoutManager linearLayoutManager;
    private OnDimCurrentDayListener onDimCurrentDayListener;
    private boolean currentDayIsDimmed;
    private OnFragmentAttachedNeedNewDataListener onFragmentAttachedNeedNewDataListener;

    public ListSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSessionsFragment newInstance(int weekday) {
        ListSessionsFragment fragment = new ListSessionsFragment();
        Bundle args = new Bundle();
        args.putInt("weekday", weekday);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.weekday = getArguments().getInt("weekday");
        }
    }

    /** Inflate the layout for this fragment (which is a RecyclerView) when creating view for fragment*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_list_sessions, container, false);
        mSessionList = mainView.findViewById(R.id.session_list);
        listSessionsSwipeRefreshLayout = mainView.findViewById(R.id.session_list_swipe_layout);
        //noContent = mainView.findViewById(R.id.noContent);
        //noContentWithLink = mainView.findViewById(R.id.noContentWithLink);
        //noSessionsFound = mainView.findViewById(R.id.noLocation);
        mSessionList.setHasFixedSize(true);
        mSessionList.setItemViewCacheSize(20);
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mSessionList.setLayoutManager(linearLayoutManager);
        mSessionList.setItemAnimator(new DefaultItemAnimator());
        sessionsAdapter = new ListSessionsAdapter(getActivity().getApplicationContext(), onSessionClickedListener, this.weekday);
        HeaderItemDecoration headerItemDecoration = new HeaderItemDecoration(mSessionList, (HeaderItemDecoration.StickyHeaderInterface) sessionsAdapter);
        mSessionList.addItemDecoration(headerItemDecoration);
        mSessionList.setAdapter(sessionsAdapter);
        /*progressBar = mainView.findViewById(R.id.dotProgressBar);
        dateHeaderTV = mainView.findViewById(R.id.listSessionsDateHeader);*/

        Long todayTimestamp = System.currentTimeMillis();
        Long dayTimestamp = new DateTime(todayTimestamp).plusDays(weekday).getMillis();
        String dateHeader = TextTimestamp.textSessionDate(dayTimestamp);
        firsLoadProgressBar = mainView.findViewById(R.id.firstLoadProgressBar);
        //dateHeaderTV.setText(dateHeader);

        // Tell the parent activity when the list is scrolled (in order to hide FAB buttons)

        mSessionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                onListSessionsScrollListener.OnListSessionsScroll(newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (sessionsAdapter.getFirstItemNextDay()!=-1) {
                    if (linearLayoutManager.findFirstVisibleItemPosition()>sessionsAdapter.getFirstItemNextDay()) {
                        if (!currentDayIsDimmed) {
                            onDimCurrentDayListener.OnDimCurrentDay(true);
                            currentDayIsDimmed = true;
                        }
                    } else {
                        if (currentDayIsDimmed) {
                            onDimCurrentDayListener.OnDimCurrentDay(false);
                            currentDayIsDimmed = false;
                        }
                    }
                }
            }
        });

        listSessionsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshSessionsListener.OnRefreshSessions();

            }
        });

        mSessionList.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                //Increment page index to load the next one
                currentPage += 1;
                totalAdsToLoadThisDay = loadedthisDay + itemsToLoadPerDayEachTime;
                loadPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });


        /*noContentWithLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextDayWithSessionsClickedListener.OnNextDayWithSessionsClicked(weekday);
            }
        });*/

        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onFragmentAttachedNeedNewDataListener.OnFragmentAttachedNeedNewData(this.weekday);
    }


    /** Use sessionsAdapter to generate view mSessionList*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //onAsyncTaskFinished();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (GeoQuery geoQuery: geofireListeners.keySet()) {
            if (geofireListeners.get(geoQuery)!=null) {
                geoQuery.removeAllListeners();
            }
        }
        listenersAdded = false;
        loading = false;
    }

    public void geoFireNodesUpdated(ArrayList<String> geoFireNodesKeys, Location currentLocation, Integer day) {

        this.currentLocation = currentLocation;
        this.geoFireNodesKeys = geoFireNodesKeys;

        geoFireLoaded = true;
        Long todayTimestamp = System.currentTimeMillis();

        Long thisDayTimestamp = 0L;
        if (day == 0) {
            thisDayTimestamp = new DateTime(todayTimestamp).plusDays(day).getMillis();
        } else {
            DateTime thisDayDateTime = new DateTime(todayTimestamp).plusDays(day);
            thisDayTimestamp = new DateTime(thisDayDateTime.getYear(), thisDayDateTime.getMonthOfYear(), thisDayDateTime.getDayOfMonth(), 0, 1).getMillis();
        }


        geoFireNodesKeysFromThisDay.clear();

        for (String nodeKey: geoFireNodesKeys) {
            Long timestamp = Long.parseLong(CharBuffer.wrap(nodeKey, 0, 13).toString());
            if (timestamp>thisDayTimestamp) {
                geoFireNodesKeysFromThisDay.add(nodeKey);
            }
        }

        // TODO CHECK THIS WITH ISADDED
        if (sessionsAdapter!=null) {
            sessionsAdapter.clear();
        }

        checkIfToLoadList();
    }

    private void checkIfToLoadList() {

        if (isAdded() && geoFireNodesKeysFromThisDay.size()>0 && sessionsAdapter.getItemCount()==0) {
            loadedthisDay = 0;
            currentPage = PAGE_START;
            isLastPage = false;
            isLoading = false;
            totalAdsToLoadThisDay = loadedthisDay + itemsToLoadPerDayEachTime;
            TOTAL_PAGES = (int) Math.ceil((double) geoFireNodesKeysFromThisDay.size()/(double)itemsToLoadPerDayEachTime);
            firstLoad = true;
            loadPage();
        }

        if (isAdded() && geoFireLoaded && geoFireNodesKeysFromThisDay.size()==0 && sessionsAdapter.getItemCount()==0) {
            firsLoadProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadPage() {
        ArrayList<Task<?>> sessionAdvertisementFirstLoadTasks  = new ArrayList<>();
        sessionAdvertisementFirstLoadTasks.clear();
        while (loadedthisDay <totalAdsToLoadThisDay && geoFireNodesKeysFromThisDay.size()>loadedthisDay) {
            advertisementIdsAndTimestampsFilteredArrayList.clear();

            String adId = CharBuffer.wrap(geoFireNodesKeysFromThisDay.get(loadedthisDay), 13, 33).toString();
            String sessionId = CharBuffer.wrap(geoFireNodesKeysFromThisDay.get(loadedthisDay), 33, 53).toString();

            TaskCompletionSource<Boolean> sessionSource = new TaskCompletionSource<>();
            Task sessionTask = sessionSource.getTask();
            sessionAdvertisementFirstLoadTasks.add(sessionTask);
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


            TaskCompletionSource<Boolean> adSource = new TaskCompletionSource<>();
            Task adTask = adSource.getTask();
            sessionAdvertisementFirstLoadTasks.add(adTask);
            DatabaseReference advertisementRef = FirebaseDatabase.getInstance().getReference().child("advertisements").child(adId);
            FirebaseDatabaseViewModel advertisementViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
            LiveData<DataSnapshot> advertisementLiveData = advertisementViewModel.getDataSnapshotLiveData(advertisementRef);
            advertisementLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()!=null) {
                        Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                        if (!advertisement.getStatus().equals("cancelled")) {
                            advertisementHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Advertisement.class));
                            advertisementIdsAndTimestampsFilteredArrayList.add(new AdvertisementIdsAndTimestamps(dataSnapshot.getKey(), dataSnapshot.getValue(Advertisement.class).getAdvertisementTimestamp()));
                        }
                    }
                    adSource.trySetResult(true);
                }
            });

            loadedthisDay++;
        }

        if (sessionAdvertisementFirstLoadTasks.isEmpty()) {
            TaskCompletionSource<String> dummySource = new TaskCompletionSource<>();
            Task dummyTask = dummySource.getTask();
            sessionAdvertisementFirstLoadTasks.add(dummyTask);
            dummySource.setResult("done");
        }

        Tasks.whenAll(sessionAdvertisementFirstLoadTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Collections.sort(advertisementIdsAndTimestampsFilteredArrayList);

                    if (currentPage==PAGE_START && PAGE_START==TOTAL_PAGES) {
                        // IF ONLY ONE PAGE
                        isLoading = false;
                        stopSwipeRefreshingSymbol();
                        firsLoadProgressBar.setVisibility(View.GONE);
                        sessionsAdapter.addAll(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);
                        isLastPage = true;
                    } else {
                        // IF MORE THAN ONE PAGE AND THIS IS FIRST PAGE
                        if (firstLoad) {
                            stopSwipeRefreshingSymbol();
                            firsLoadProgressBar.setVisibility(View.GONE);
                            sessionsAdapter.addAll(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);

                            if (currentPage <= TOTAL_PAGES) sessionsAdapter.addLoadingFooter();
                            else isLastPage = true;
                        } else {
                            // ALL NEXT PAGES
                            sessionsAdapter.removeLoadingFooter();  // 2
                            isLoading = false;   // 3
                            sessionsAdapter.addAll(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);   // 4
                            if (currentPage != TOTAL_PAGES) sessionsAdapter.addLoadingFooter();  // 5
                            else isLastPage = true;
                        }
                    }
                    firstLoad = false;
                }
            }
        });

    }

    public void notifyAdvertisementChange(String advertisementId, HashMap<String, Advertisement> advertisementHashMap, HashMap<String, Session> sessionHashMap) {
        if (sessionsAdapter !=null) {
            this.advertisementHashMap = advertisementHashMap;
            this.sessionHashMap = sessionHashMap;
            sessionsAdapter.notifyAdvertisementChange(advertisementId, advertisementHashMap, sessionHashMap);
        }
    }

    public void notifyAdvertisementRemoved(AdvertisementIdsAndTimestamps removedAd) {
        if (sessionsAdapter !=null) {
            sessionsAdapter.notifyAdvertisementRemoved(removedAd);
        }
    }

    public void emptyListView() {
        //noSessionsFound.setVisibility(View.VISIBLE);
    }

    public void stopSwipeRefreshingSymbol() {
        swipeRefreshStatusUsed = false;
        swipeRefreshStatusLoaded = true;
        //onAsyncTaskFinished();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
        if (context instanceof OnRefreshSessionsListener) {
            onRefreshSessionsListener = (OnRefreshSessionsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRefreshSessionsListener");
        }
        if (context instanceof OnListSessionsScrollListener) {
            onListSessionsScrollListener = (OnListSessionsScrollListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListSessionsScrollListener");
        }
        if (context instanceof OnNextDayWithSessionsClickedListener) {
            onNextDayWithSessionsClickedListener = (OnNextDayWithSessionsClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNextDayWithSessionsClickedListener");
        }
        if (context instanceof OnDimCurrentDayListener) {
            onDimCurrentDayListener = (OnDimCurrentDayListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDimCurrentDayListener");
        }
        if (context instanceof OnFragmentAttachedNeedNewDataListener) {
            onFragmentAttachedNeedNewDataListener = (OnFragmentAttachedNeedNewDataListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentAttachedNeedNewDataListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        onRefreshSessionsListener = null;
        onListSessionsScrollListener = null;
        onNextDayWithSessionsClickedListener = null;
        onDimCurrentDayListener = null;
        onFragmentAttachedNeedNewDataListener = null;
    }

    public interface OnRefreshSessionsListener {
        void OnRefreshSessions();
    }

    public interface OnListSessionsScrollListener {
        void OnListSessionsScroll(int state);
    }

    public interface OnFilterReadyListener {
        void OnFilterReady();
    }

    public interface OnNextDayWithSessionsClickedListener {
        void OnNextDayWithSessionsClicked(Integer currentDay);
    }

    public interface OnDimCurrentDayListener {
        void OnDimCurrentDay(boolean dim);
    }
    public interface OnFragmentAttachedNeedNewDataListener {
        void OnFragmentAttachedNeedNewData(int week);
    }
}