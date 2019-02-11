package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.sessionsAdapter;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.utils.AdvertisementIdsAndTimestamps;
import com.foxmike.android.utils.HeaderItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This fragment creates a list of sessions based on an arraylist of session objects given as arguments. It also
 * uses an location object in order to sort the sessions on distance from user
 */
public class  ListSessionsFragment extends Fragment {

    private RecyclerView mSessionList;
    private com.foxmike.android.adapters.sessionsAdapter sessionsAdapter;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;
    private OnRefreshSessionsListener onRefreshSessionsListener;
    private SwipeRefreshLayout listSessionsSwipeRefreshLayout;
    private OnListSessionsScrollListener onListSessionsScrollListener;
    private TextView noSessionsFound;
    private ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList;
    private HashMap<String, Advertisement> advertisementHashMap = new HashMap<>();
    private HashMap<String, Session> sessionHashMap = new HashMap<>();
    private boolean sessionsLoaded;
    private boolean locationLoaded;
    private boolean sessionsAndLocationUsed;

    private boolean swipeRefreshStatus;
    private boolean swipeRefreshStatusLoaded;
    private boolean swipeRefreshStatusUsed;
    private TextView noContent;
    private FrameLayout progressBar;

    private View mainView;

    public ListSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSessionsFragment newInstance() {
        ListSessionsFragment fragment = new ListSessionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    /** Inflate the layout for this fragment (which is a RecyclerView) when creating view for fragment*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_list_sessions, container, false);
        mSessionList = mainView.findViewById(R.id.session_list);
        listSessionsSwipeRefreshLayout = mainView.findViewById(R.id.session_list_swipe_layout);
        noContent = mainView.findViewById(R.id.noContent);
        noSessionsFound = mainView.findViewById(R.id.noSessionsFound);
        //mSessionList.setHasFixedSize(true); TODO What does this mean
        mSessionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = mainView.findViewById(R.id.progressBar);
        // Tell the parent activity when the list is scrolled (in order to hide FAB buttons)
        mSessionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                onListSessionsScrollListener.OnListSessionsScroll(dy);
            }
        });
        // Tell the parent activity when the list is swiped to refresh (in order to refresh data from database)
        listSessionsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshSessionsListener.OnRefreshSessions();
            }
        });

        return mainView;
    }

    /** Use sessionsAdapter to generate view mSessionList*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        onAsyncTaskFinished();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void onAsyncTaskFinished() {
        if (sessionsLoaded && locationLoaded && !sessionsAndLocationUsed && this.mainView!=null ) {
            sessionsAndLocationUsed = true;

            progressBar.setVisibility(View.GONE);

            if (advertisementIdsAndTimestampsFilteredArrayList.size()>0) {
                noContent.setVisibility(View.GONE);
            } else {
                noContent.setVisibility(View.VISIBLE);
            }

            if (sessionsAdapter!=null) {
                noSessionsFound.setVisibility(View.GONE);
                sessionsAdapter.refreshData(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, currentLocation);
            } else {
                noSessionsFound.setVisibility(View.GONE);
                sessionsAdapter = new sessionsAdapter(advertisementIdsAndTimestampsFilteredArrayList, advertisementHashMap, sessionHashMap, getActivity(), currentLocation, onSessionClickedListener);
                if (mSessionList!=null) {
                    HeaderItemDecoration headerItemDecoration = new HeaderItemDecoration(mSessionList, (HeaderItemDecoration.StickyHeaderInterface) sessionsAdapter);
                    mSessionList.addItemDecoration(headerItemDecoration);
                    mSessionList.setAdapter(sessionsAdapter);
                }
                sessionsAdapter.notifyDataSetChanged();
            }
        }

        if (swipeRefreshStatusLoaded && mainView!=null && !swipeRefreshStatusUsed) {
            swipeRefreshStatusUsed = true;
            listSessionsSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // Function to refresh data in sessionsAdapter
    public void updateSessionListView(ArrayList<AdvertisementIdsAndTimestamps> advertisementIdsAndTimestampsFilteredArrayList, HashMap<String, Advertisement> advertisementHashMap, HashMap<String, Session> sessionHashMap, Location currentLocation) {
        this.advertisementIdsAndTimestampsFilteredArrayList = advertisementIdsAndTimestampsFilteredArrayList;
        this.advertisementHashMap = advertisementHashMap;
        this.sessionHashMap = sessionHashMap;
        this.currentLocation = currentLocation;
        sessionsLoaded = true;
        locationLoaded = true;
        sessionsAndLocationUsed = false;

        onAsyncTaskFinished();
    }

    public void notifyAdvertisementChange(String advertisementId, HashMap<String, Advertisement> advertisementHashMap, HashMap<String, Session> sessionHashMap) {
        if (sessionsAdapter!=null) {
            this.advertisementHashMap = advertisementHashMap;
            this.sessionHashMap = sessionHashMap;
            sessionsAdapter.notifyAdvertisementChange(advertisementId, advertisementHashMap, sessionHashMap);
        }
    }

    public void emptyListView() {
        noSessionsFound.setVisibility(View.VISIBLE);
    }

    public void stopSwipeRefreshingSymbol() {
        swipeRefreshStatusUsed = false;
        swipeRefreshStatus = false;
        swipeRefreshStatusLoaded = true;
        onAsyncTaskFinished();
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        onRefreshSessionsListener = null;
        onListSessionsScrollListener = null;
    }

    public interface OnRefreshSessionsListener {
        void OnRefreshSessions();
    }

    public interface OnListSessionsScrollListener {
        void OnListSessionsScroll(int dy);
    }
}