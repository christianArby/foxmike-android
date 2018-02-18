package com.example.chris.kungsbrostrand;


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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class ListSessionsFragment extends Fragment {

    private RecyclerView mSessionList;
    private sessionsAdapter sessionsAdapter;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;
    private OnRefreshSessionsListener onRefreshSessionsListener;
    private SwipeRefreshLayout listSessionsSwipeRefreshLayout;
    private OnListSessionsScrollListener onListSessionsScrollListener;
    private OnListSessionsSortListener onListSessionsSortListener;

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
        View view = inflater.inflate(R.layout.fragment_list_sessions, container, false);
        mSessionList = view.findViewById(R.id.session_list);
        listSessionsSwipeRefreshLayout = view.findViewById(R.id.session_list_swipe_layout);
        mSessionList.setHasFixedSize(true);
        mSessionList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSessionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                onListSessionsScrollListener.OnListSessionsScroll(dy);
            }
        });


        listSessionsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshSessionsListener.OnRefreshSessions();
            }
        });

        final Spinner sortOnSpinner = (Spinner) view.findViewById(R.id.sortOnSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_list_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sortOnSpinner.setAdapter(adapter);

        sortOnSpinner.setPrompt("Title");

        sortOnSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int sortType = i;
                onListSessionsSortListener.OnListSessionsSort(sortType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                onListSessionsSortListener.OnListSessionsSort(0);

            }
        });

        return view;
    }

    /** Use sessionsAdapter to generate view mSessionList*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //mSessionList.setAdapter(sessionsAdapter);

        //HeaderItemDecoration headerItemDecoration = new HeaderItemDecoration(mSessionList, (HeaderItemDecoration.StickyHeaderInterface) sessionsAdapter);
        //mSessionList.addItemDecoration(headerItemDecoration);
        //mSessionList.setAdapter(sessionsAdapter);


    }

    public void updateSessionListView(ArrayList<Session> sessions, Location location) {

        sessionsAdapter.refreshData(sessions,location);

    }


    /** Generate view in RecyclerView with sessionsAdapter*/
    public void generateSessionListView(ArrayList<Session> sessions, Location location) {

        currentLocation =location;

        sessionsAdapter = new sessionsAdapter(sessions, getActivity(), currentLocation, onSessionClickedListener);
        if (mSessionList!=null) {
            //mSessionList.setAdapter(sessionsAdapter);

            HeaderItemDecoration headerItemDecoration = new HeaderItemDecoration(mSessionList, (HeaderItemDecoration.StickyHeaderInterface) sessionsAdapter);
            mSessionList.addItemDecoration(headerItemDecoration);
            mSessionList.setAdapter(sessionsAdapter);

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
        if (context instanceof OnListSessionsSortListener) {
            onListSessionsSortListener = (OnListSessionsSortListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListSessionsSortListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        onRefreshSessionsListener = null;
        onListSessionsScrollListener = null;
        onListSessionsSortListener = null;
    }

    public interface OnRefreshSessionsListener {
        void OnRefreshSessions();
    }

    public interface OnListSessionsScrollListener {
        void OnListSessionsScroll(int dy);
    }

    public interface OnListSessionsSortListener {
        void OnListSessionsSort(int sortType);
    }
}