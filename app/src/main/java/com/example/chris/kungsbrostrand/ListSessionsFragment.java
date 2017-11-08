package com.example.chris.kungsbrostrand;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
        mSessionList.setHasFixedSize(true);
        mSessionList.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        sessionsAdapter = new sessionsAdapter(sessions, getActivity(), currentLocation);
        if (mSessionList!=null) {
            mSessionList.setAdapter(sessionsAdapter);
        }
    }
}