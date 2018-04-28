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
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyFirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class HostListSmallSessionsAdvFragment extends Fragment {

    private OnSessionBranchClickedListener onSessionBranchClickedListener;
    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsAdapter listSmallSessionsAdapter;
    private ArrayList<SessionBranch> sessionsAdv = new ArrayList<>();


    public HostListSmallSessionsAdvFragment() {
        // Required empty public constructor
    }

    public static HostListSmallSessionsAdvFragment newInstance() {
        HostListSmallSessionsAdvFragment fragment = new HostListSmallSessionsAdvFragment();
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
        View view =  inflater.inflate(R.layout.fragment_host_list_small_sessions_booked, container, false);
        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listSmallSessionsAdapter = new ListSmallSessionsAdapter(sessionsAdv, onSessionBranchClickedListener, getContext());
        smallSessionsListRV.setAdapter(listSmallSessionsAdapter);
        return view;
    }

    // Function which downloads sessions hosted by current user and saves the sessions which are advertised in the arraylist sessionsAdv,
    // Criteria for advertised is that the boolean advertised is true and that the session date
    public void initData() {
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Get the currents user's information from the database */
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                if (user.sessionsHosting.size()!=0){
                    // Get which sessions the current user is hosting in a arraylist from the database
                    myFirebaseDatabase.getSessionBranches(user.sessionsHosting,new OnSessionBranchesFoundListener() {
                        @Override
                        public void OnSessionBranchesFound(ArrayList<SessionBranch> sessionBranches) {
                            final Calendar cal = Calendar.getInstance();
                            Date todaysDate = cal.getTime();
                            cal.add(Calendar.DATE,14);
                            Date twoWeeksDate = cal.getTime();
                            // Loop hosted sessions and see which are advertised, critera for advertised is that the boolean advertised is true and that the session date
                            // is after today's date

                            for (SessionBranch sessionBranch: sessionBranches) {

                                if (sessionBranch.getSession().isAdvertised() && sessionBranch.getSession().supplyDate().after(todaysDate)) {
                                    sessionsAdv.add(sessionBranch);
                                }
                            }
                            // Sort the list on date
                            Collections.sort(sessionsAdv);

                            // Input a dummysession with a section header for sessions further away than two weeks
                            int n = 0;
                            Boolean keepLooking = true;
                            while (n < sessionsAdv.size() && keepLooking) {
                                if (sessionsAdv.get(n).getSession().supplyDate().after(twoWeeksDate) && keepLooking) {
                                    Session dummySession = new Session();
                                    dummySession.setImageUrl("sectionHeader");
                                    dummySession.setSessionName(getString(R.string.upcoming_listings));
                                    SessionBranch dummySessionBranch = new SessionBranch("irrelevant", dummySession);
                                    sessionsAdv.add(n, dummySessionBranch);
                                    keepLooking=false;
                                }
                                n++;
                            }
                            if (listSmallSessionsAdapter!=null) {
                                listSmallSessionsAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
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
}