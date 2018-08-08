package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallSessionsAdapter;
import com.foxmike.android.interfaces.OnSessionBranchClickedListener;
import com.foxmike.android.interfaces.OnSessionBranchesFoundListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyFirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class PlayerListSmallSessionsHistoryFragment extends Fragment {

    private OnSessionBranchClickedListener onSessionBranchClickedListener;
    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsAdapter listSmallSessionsAdapter;
    private ArrayList<SessionBranch> sessionsHistory = new ArrayList<>();
    private TextView noContent;

    public PlayerListSmallSessionsHistoryFragment() {
        // Required empty public constructor
    }

    public static PlayerListSmallSessionsHistoryFragment newInstance() {
        PlayerListSmallSessionsHistoryFragment fragment = new PlayerListSmallSessionsHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and setup recylerview and adapter
        View view = inflater.inflate(R.layout.fragment_player_list_small_sessions_history, container, false);
        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listSmallSessionsAdapter = new ListSmallSessionsAdapter(sessionsHistory, onSessionBranchClickedListener, "displaySession",getContext());
        smallSessionsListRV.setAdapter(listSmallSessionsAdapter);
        noContent = view.findViewById(R.id.noContent);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionBranchClickedListener) {
            onSessionBranchClickedListener = (OnSessionBranchClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionBranchClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionBranchClickedListener = null;
    }
    // Function which downloads sessions attended by current user and saves the sessions which are in the past in the arraylist sessionsHistory
    public void initData() {
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        // Get the currents user's information from the database *//*
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {

                myFirebaseDatabase.getSessionBranches(user.sessionsAttending, new OnSessionBranchesFoundListener() {
                    @Override
                    public void OnSessionBranchesFound(ArrayList<SessionBranch> sessionsBranches) {
                        final Calendar cal = Calendar.getInstance();
                        Date todaysDate = cal.getTime();

                        for (SessionBranch sessionBranch: sessionsBranches) {
                            if (!sessionBranch.getSession().supplyDate().after(todaysDate)) {
                                sessionsHistory.add(sessionBranch);
                            }
                        }
                        Collections.sort(sessionsHistory);

                        if (sessionsHistory.size()>0) {
                            noContent.setVisibility(View.GONE);
                        } else {
                            noContent.setVisibility(View.VISIBLE);
                        }

                        if (listSmallSessionsAdapter!=null) {
                            listSmallSessionsAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

}
