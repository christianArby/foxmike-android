package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.Iterator;

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
    private View mainView;
    private ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList  = new ArrayList<>();
    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private HashMap<String, Advertisement> advertisementHashMap = new HashMap<>();
    private int loadedthisDay = 0;
    private int itemsToLoadPerDayEachTime = 20;
    private int totalAdsToLoadThisDay;
    private HashMap<String, String> sessionTypeDictionary;
    private TextView noContent;

    private HashMap<GeoQuery, GeoQueryEventListener> geofireListeners = new HashMap<>();
    private int weekday;
    ArrayList<String> geoFireNodesKeys = new ArrayList<>();
    private DotProgressBar firsLoadProgressBar;

    private boolean geoFireLoaded = false;
    private boolean firstLoad = true;

    //private TextView noContentWithLink;
    private ArrayList<String> geoFireNodesKeysFromThisDay = new ArrayList<>();

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
    private boolean geoFireUsed;
    private Long lastLoadingStart;
    private TextView noLocationPermission;
    private TextView noLocation;

    public ListSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSessionsFragment newInstance(int weekday, HashMap<String,String> sessionTypeDictionary) {
        ListSessionsFragment fragment = new ListSessionsFragment();
        Bundle args = new Bundle();
        args.putInt("weekday", weekday);
        args.putSerializable("sessionTypeDictionary", sessionTypeDictionary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.weekday = getArguments().getInt("weekday");
            sessionTypeDictionary = (HashMap<String, String>)getArguments().getSerializable("sessionTypeDictionary");
        }
    }

    /** Inflate the layout for this fragment (which is a RecyclerView) when creating view for fragment*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_list_sessions, container, false);
        mSessionList = mainView.findViewById(R.id.session_list);
        listSessionsSwipeRefreshLayout = mainView.findViewById(R.id.session_list_swipe_layout);
        mSessionList.setHasFixedSize(true);
        mSessionList.setItemViewCacheSize(20);
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mSessionList.setLayoutManager(linearLayoutManager);
        mSessionList.setItemAnimator(new DefaultItemAnimator());
        sessionsAdapter = new ListSessionsAdapter(getActivity().getApplicationContext(), onSessionClickedListener, this.weekday, sessionTypeDictionary);
        HeaderItemDecoration headerItemDecoration = new HeaderItemDecoration(mSessionList, (HeaderItemDecoration.StickyHeaderInterface) sessionsAdapter);
        mSessionList.addItemDecoration(headerItemDecoration);
        mSessionList.setAdapter(sessionsAdapter);
        noContent = mainView.findViewById(R.id.noContent);
        noLocationPermission = mainView.findViewById(R.id.noLocationPermission);
        noLocation = mainView.findViewById(R.id.noLocation);
        noLocation.setVisibility(View.GONE);

        Long todayTimestamp = System.currentTimeMillis();
        Long dayTimestamp = new DateTime(todayTimestamp).plusDays(weekday).getMillis();
        String dateHeader = TextTimestamp.textSessionDate(dayTimestamp);
        firsLoadProgressBar = mainView.findViewById(R.id.firstLoadProgressBar);

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
                onRefreshSessionsListener.OnRefreshSessions(weekday);
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

        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onFragmentAttachedNeedNewDataListener.OnFragmentAttachedNeedNewData(this.weekday);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (GeoQuery geoQuery: geofireListeners.keySet()) {
            if (geofireListeners.get(geoQuery)!=null) {
                geoQuery.removeAllListeners();
            }
        }
    }

    public void geoFireNodesUpdated(ArrayList<String> geoFireNodesKeys, Location currentLocation, Integer day) {

        listSessionsSwipeRefreshLayout.setRefreshing(false);

        this.currentLocation = currentLocation;
        this.geoFireNodesKeys = geoFireNodesKeys;
        this.weekday = day;

        geoFireLoaded = true;
        geoFireUsed = false;

        checkIfToLoadListFromScratch();
    }

    public void firstLoadVisible (boolean show) {
        if (show) {
            firsLoadProgressBar.setVisibility(View.VISIBLE);
        } else {
            firsLoadProgressBar.setVisibility(View.GONE);
        }
    }

    public void noLocationPermissionVisible(boolean show) {
        if (show) {
            noLocationPermission.setVisibility(View.VISIBLE);
        } else {
            noLocationPermission.setVisibility(View.GONE);
        }
    }

    public void noLocationVisible(boolean show) {
        if (show) {
            noLocation.setVisibility(View.VISIBLE);
        } else {
            noLocation.setVisibility(View.GONE);
        }
    }

    private void checkIfToLoadListFromScratch() {

        if (isAdded() && !isLoading && !geoFireUsed) {
            noContent.setVisibility(View.GONE);
            ArrayList<String> geoFireNodesKeysUsing = geoFireNodesKeys;
            isLoading = true;
            lastLoadingStart = SystemClock.elapsedRealtime();
            geoFireUsed = true;
            firsLoadProgressBar.setVisibility(View.VISIBLE);

            /*CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if (SystemClock.elapsedRealtime() - lastLoadingStart > 4900 && isLoading) {
                        isLoading = false;
                        geoFireUsed = false;
                        checkIfToLoadListFromScratch();
                    }
                }
            }.start();*/

            Long todayTimestamp = System.currentTimeMillis();
            Long thisDayTimestamp = 0L;
            if (this.weekday == 0) {
                thisDayTimestamp = new DateTime(todayTimestamp).plusDays(this.weekday).minusMinutes(75).getMillis();
            } else {
                DateTime thisDayDateTime = new DateTime(todayTimestamp).plusDays(this.weekday);
                thisDayTimestamp = new DateTime(thisDayDateTime.getYear(), thisDayDateTime.getMonthOfYear(), thisDayDateTime.getDayOfMonth(), 0, 1).getMillis();
            }
            geoFireNodesKeysFromThisDay.clear();
            for (String nodeKey: geoFireNodesKeysUsing) {
                Long timestamp = Long.parseLong(CharBuffer.wrap(nodeKey, 0, 13).toString());
                if (timestamp>thisDayTimestamp) {
                    geoFireNodesKeysFromThisDay.add(nodeKey);
                }
            }
            // TODO CHECK THIS WITH ISADDED
            if (sessionsAdapter!=null) {
                sessionsAdapter.clear();
            }

            if (geoFireNodesKeysFromThisDay.size() >0) {
                loadedthisDay = 0;
                currentPage = PAGE_START;
                isLastPage = false;
                totalAdsToLoadThisDay = loadedthisDay + itemsToLoadPerDayEachTime;
                TOTAL_PAGES = (int) Math.ceil((double) geoFireNodesKeysFromThisDay.size()/(double)itemsToLoadPerDayEachTime);
                firstLoad = true;
                loadPage();
            } else {
                isLoading = false;
                firsLoadProgressBar.setVisibility(View.GONE);
            }

        }

        /*if (isAdded() && geoFireNodesKeysFromThisDay.size()>0 && sessionsAdapter.getItemCount()==0) {
            loadedthisDay = 0;
            currentPage = PAGE_START;
            isLastPage = false;
            isLoading = false;
            totalAdsToLoadThisDay = loadedthisDay + itemsToLoadPerDayEachTime;
            TOTAL_PAGES = (int) Math.ceil((double) geoFireNodesKeysFromThisDay.size()/(double)itemsToLoadPerDayEachTime);
            firstLoad = true;
            isLoading = true;
            onListSessionsLoadingListener.OnListSessionsLoading(isLoading);
            loadPage();
        }*/

        if (isAdded() && !isLoading && geoFireLoaded && geoFireNodesKeysFromThisDay.size()==0 && sessionsAdapter.getItemCount()==0) {
            noContent.setVisibility(View.VISIBLE);
            firsLoadProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadPage() {
        ArrayList<Task<?>> sessionAdvertisementFirstLoadTasks  = new ArrayList<>();
        sessionAdvertisementFirstLoadTasks.clear();
        advertisementIdsAndTimestampsFilteredArrayList.clear();
        while (loadedthisDay < totalAdsToLoadThisDay && geoFireNodesKeysFromThisDay.size() > loadedthisDay) {

            Long timestamp = Long.parseLong(CharBuffer.wrap(geoFireNodesKeysFromThisDay.get(loadedthisDay), 0, 13).toString());
            String adId = CharBuffer.wrap(geoFireNodesKeysFromThisDay.get(loadedthisDay), 13, 33).toString();
            String sessionId = CharBuffer.wrap(geoFireNodesKeysFromThisDay.get(loadedthisDay), 33, 53).toString();

            advertisementIdsAndTimestampsFilteredArrayList.add(new AdvertisementIdsAndTimestamps(adId, timestamp));

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
                        if (advertisement.getStatus().equals("cancelled")) {
                            notifyAdvertisementRemoved(new AdvertisementIdsAndTimestamps(advertisement.getAdvertisementId(), advertisement.getAdvertisementTimestamp()));
                        }
                        advertisementHashMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Advertisement.class));
                    } else {
                        if (advertisementHashMap.containsKey(dataSnapshot.getKey())) {
                            notifyAdvertisementRemoved(new AdvertisementIdsAndTimestamps(dataSnapshot.getKey(), advertisementHashMap.get(dataSnapshot.getKey()).getAdvertisementTimestamp()));
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

                    // Remove cancelled sessions
                    Iterator<AdvertisementIdsAndTimestamps> i = advertisementIdsAndTimestampsFilteredArrayList.iterator();
                    while (i.hasNext()) {
                        AdvertisementIdsAndTimestamps ad = i.next();
                        if (advertisementHashMap.get(ad.getAdvertisementId()).getStatus().equals("cancelled")) {
                            i.remove();
                        }
                    }

                    if (currentPage==PAGE_START && PAGE_START==TOTAL_PAGES) {
                        // IF ONLY ONE PAGE
                        stopSwipeRefreshingSymbol();
                        firsLoadProgressBar.setVisibility(View.GONE);
                        sessionsAdapter.addAll(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);
                        isLastPage = true;
                        isLoading = false;
                        checkIfToLoadListFromScratch();
                    } else {
                        // IF MORE THAN ONE PAGE AND THIS IS FIRST PAGE
                        if (firstLoad) {
                            stopSwipeRefreshingSymbol();

                            firsLoadProgressBar.setVisibility(View.GONE);
                            sessionsAdapter.addAll(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);
                            isLoading = false;
                            checkIfToLoadListFromScratch();

                            if (currentPage <= TOTAL_PAGES) sessionsAdapter.addLoadingFooter();
                            else isLastPage = true;
                        } else {
                            // ALL NEXT PAGES
                            if (sessionsAdapter.getItemCount()>0) {
                                sessionsAdapter.removeLoadingFooter();  // 2
                                sessionsAdapter.addAll(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);
                                isLoading = false;
                                firsLoadProgressBar.setVisibility(View.GONE);
                                checkIfToLoadListFromScratch();// 4
                                if (currentPage != TOTAL_PAGES) sessionsAdapter.addLoadingFooter();  // 5
                                else isLastPage = true;
                            }

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

    public void stopSwipeRefreshingSymbol() {
        listSessionsSwipeRefreshLayout.setRefreshing(false);
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
        onDimCurrentDayListener = null;
        onFragmentAttachedNeedNewDataListener = null;
    }

    public interface OnRefreshSessionsListener {
        void OnRefreshSessions(int weekday);
    }

    public interface OnListSessionsScrollListener {
        void OnListSessionsScroll(int state);
    }

    public interface OnDimCurrentDayListener {
        void OnDimCurrentDay(boolean dim);
    }
    public interface OnFragmentAttachedNeedNewDataListener {
        void OnFragmentAttachedNeedNewData(int week);
    }
}