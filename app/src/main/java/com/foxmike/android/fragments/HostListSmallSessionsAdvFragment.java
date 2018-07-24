package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class HostListSmallSessionsAdvFragment extends Fragment {

    private OnSessionBranchClickedListener onSessionBranchClickedListener;
    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsAdapter listSmallSessionsAdapter;
    private HashMap<String,Boolean> sessionIds = new HashMap<>();
    private ArrayList<SessionBranch> sessionsAdv = new ArrayList<>();
    private int studioCounter = 0;
    private boolean sessionsLoaded;
    private boolean sessionsAndViewUsed;
    private TextView noContent;


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
        View view =  inflater.inflate(R.layout.fragment_host_list_small_sessions_adv, container, false);
        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listSmallSessionsAdapter = new ListSmallSessionsAdapter(sessionsAdv, onSessionBranchClickedListener, getContext());
        smallSessionsListRV.setAdapter(listSmallSessionsAdapter);
        noContent = view.findViewById(R.id.noContent);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sortAndLoadSessions(sessionsAdv);
    }

    // Function which downloads sessions hosted by current user and saves the sessions which are advertised in the arraylist sessionsAdv,
    // Criteria for advertised is that the boolean advertised is true and that the session date
    public void initData() {

        final DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("studios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // No studios
                } else {
                    final HashMap<String,Boolean> studios = (HashMap<String,Boolean>) dataSnapshot.getValue();
                    studioCounter = 0;
                    for (String studioId: studios.keySet()) {
                        rootDbRef.child("studiosTEST").child(studioId).child("sessions").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                studioCounter++;
                                if (dataSnapshot.getValue()==null) {
                                    // No sessions
                                } else {
                                    HashMap<String, Long> sessionsMap = (HashMap<String, Long>) dataSnapshot.getValue();
                                    Long currentTimestamp = System.currentTimeMillis();
                                    for (String sessionId: sessionsMap.keySet()) {
                                        if (sessionsMap.get(sessionId)>currentTimestamp) {
                                            sessionIds.put(sessionId, true);
                                        }
                                    }
                                    if (studioCounter==studios.size()) {
                                        MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
                                        myFirebaseDatabase.getSessionBranches(sessionIds, new OnSessionBranchesFoundListener() {
                                            @Override
                                            public void OnSessionBranchesFound(ArrayList<SessionBranch> sessionBranches) {
                                                sessionsAdv = sessionBranches;
                                                sessionsLoaded = true;

                                                if (sessionsAdv.size()>0) {
                                                    noContent.setVisibility(View.GONE);
                                                } else {
                                                    noContent.setVisibility(View.VISIBLE);
                                                }

                                                sortAndLoadSessions(sessionBranches);
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sortAndLoadSessions(ArrayList<SessionBranch> sessionBranches) {

        if (getView()!=null && sessionsLoaded && !sessionsAndViewUsed) {
            sessionsAndViewUsed=true;
            sessionsAdv = sessionBranches;
            Collections.sort(sessionsAdv);

            // Input a dummysession with a section header for sessions further away than two weeks
            final Calendar cal = Calendar.getInstance();
            Date todaysDate = cal.getTime();
            cal.add(Calendar.DATE,14);
            Date twoWeeksDate = cal.getTime();
            int n = 0;
            Boolean keepLooking = true;
            while (n < sessionsAdv.size() && keepLooking) {
                if (sessionsAdv.get(n).getSession().supplyDate().after(twoWeeksDate) && keepLooking) {
                    Session dummySession = new Session();
                    dummySession.setImageUrl("sectionHeader");
                    dummySession.setSessionName(getResources().getString(R.string.upcoming_advertisements));
                    SessionBranch dummySessionBranch = new SessionBranch("irrelevant", dummySession);
                    sessionsAdv.add(n, dummySessionBranch);
                    keepLooking=false;
                }
                n++;
            }
            if (listSmallSessionsAdapter!=null) {
                listSmallSessionsAdapter.updateData(sessionsAdv);
            }
        }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        sessionsAndViewUsed = false;
    }
}
