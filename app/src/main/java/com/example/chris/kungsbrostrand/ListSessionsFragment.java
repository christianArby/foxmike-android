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

import java.util.ArrayList;

public class ListSessionsFragment extends Fragment {

    private RecyclerView mSessionList;
    private RecyclerView.Adapter sessionsAdapter;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;
    private OnRefreshSessionsListener onRefreshSessionsListener;
    private SwipeRefreshLayout listSessionsSwipeRefreshLayout;

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


        listSessionsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshSessionsListener.OnRefreshSessions();
            }
        });


        return view;
    }

    /** Use sessionsAdapter to generate view mSessionList*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSessionList.setAdapter(sessionsAdapter);
    }

    /** Generate view in RecyclerView with sessionsAdapter*/
    public void generateSessionListView(ArrayList<Session> sessions, Location location) {
        currentLocation =location;
        sessionsAdapter = new sessionsAdapter(sessions, getActivity(), currentLocation, onSessionClickedListener);
        if (mSessionList!=null) {
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        onRefreshSessionsListener = null;
    }

    public interface OnRefreshSessionsListener {
        void OnRefreshSessions();
    }
}