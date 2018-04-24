package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class PlayerListSmallSessionsBookedFragment extends Fragment {

    private OnSessionBranchClickedListener onSessionBranchClickedListener;
    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsAdapter listSmallSessionsAdapter;
    private ArrayList<SessionBranch> sessionsBooked = new ArrayList<>();

    public PlayerListSmallSessionsBookedFragment() {
        // Required empty public constructor
    }

    public static PlayerListSmallSessionsBookedFragment newInstance() {
        PlayerListSmallSessionsBookedFragment fragment = new PlayerListSmallSessionsBookedFragment();
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
        View view = inflater.inflate(R.layout.fragment_player_list_small_sessions_booked, container, false);
        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listSmallSessionsAdapter = new ListSmallSessionsAdapter(sessionsBooked, onSessionBranchClickedListener, getContext());
        smallSessionsListRV.setAdapter(listSmallSessionsAdapter);
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

    // Function which downloads sessions attended by current user and saves the sessions which are coming up in the arraylist sessionsBooked
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
                            if (sessionBranch.getSession().supplyDate().after(todaysDate)) {
                                sessionsBooked.add(sessionBranch);
                            }
                        }
                        Collections.sort(sessionsBooked);

                        if (listSmallSessionsAdapter!=null) {
                            listSmallSessionsAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

}
