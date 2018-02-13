package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HostSessionsFragment extends Fragment {

    private OnCreateSessionClickedListener onCreateSessionClickedListener;
    private FloatingActionButton createSessionBtn;
    private ViewPager hostSessionsPager;
    private HostSessionsPagerAdapter hostSessionsPagerAdapter;
    private TabLayout tabLayout;

    public HostSessionsFragment() {
        // Required empty public constructor
    }

    public static HostSessionsFragment newInstance() {
        HostSessionsFragment fragment = new HostSessionsFragment();
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

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Get the view fragment_user_account */
        final View view = inflater.inflate(R.layout.fragment_host_sessions, container, false);

        createSessionBtn = view.findViewById(R.id.add_session_btn);
        createSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateSessionClickedListener.OnCreateSessionClicked();
            }
        });

        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        /* Get the currents user's information from the database */
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not hosting any sessions set that the sessionsHosting content has beeen found*/
                if (user.sessionsHosting.size()==0){

                }
                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {

                        ArrayList<Session> sessionsAdv = new ArrayList<Session>();
                        ArrayList<Session> sessionsNotAdv = new ArrayList<Session>();
                        HashMap<Integer,String> sessionsAdvSectionHeaders = new HashMap<>();
                        HashMap<Integer,String> sessionsNotAdvSectionHeaders = new HashMap<>();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        final Calendar cal = Calendar.getInstance();
                        Date todaysDate = cal.getTime();
                        cal.add(Calendar.DATE,14);
                        Date twoWeeksDate = cal.getTime();

                        for (Session session: sessions) {
                            if (session.isAdvertised() && session.getSessionDate().getDateOfSession().after(todaysDate)) {
                                sessionsAdv.add(session);
                            } else {
                                sessionsNotAdv.add(session);
                            }
                        }

                        Collections.sort(sessionsAdv);
                        Collections.sort(sessionsNotAdv);

                        int n = 0;
                        Boolean keepLooking = true;
                        List<Integer> rows = new ArrayList<Integer>();

                        for (Session session: sessionsAdv) {

                            if (session.getSessionDate().getDateOfSession().after(twoWeeksDate) && keepLooking) {
                                rows.add(n);
                                sessionsAdvSectionHeaders.put(n, "Kommande annonseringar");
                                keepLooking=false;
                            }
                            n++;
                        }

                        for (Integer row: rows) {
                            Session dummySession = new Session();
                            sessionsAdv.add(row, dummySession);
                        }



                        /*int n2 = 0;
                        Boolean keepLooking2 = true;
                        List<Integer> rows2 = new ArrayList<Integer>();
                        for (Session session: sessionsNotAdv) {

                            if (session.getSessionDate().getDateOfSession().after(twoWeeksDate) && keepLooking2) {
                                rows2.add(n2);
                                sessionsNotAdvSectionHeaders.put(n2, "Kommande annonseringar");
                                keepLooking2=false;
                            }
                            n2++;

                        }

                        for (Integer row: rows2) {
                            Session dummySession = new Session();
                            sessionsNotAdv.add(row, dummySession);
                        }*/



                        hostSessionsPager = (ViewPager) view.findViewById(R.id.host_sessions_pager);

                        hostSessionsPagerAdapter = new HostSessionsPagerAdapter(getChildFragmentManager(), sessionsAdv, sessionsAdvSectionHeaders, sessionsNotAdv, sessionsNotAdvSectionHeaders);

                        hostSessionsPager.setAdapter(hostSessionsPagerAdapter);

                        tabLayout = (TabLayout) view.findViewById(R.id.host_sessions_tabs);

                        tabLayout.setupWithViewPager(hostSessionsPager);

                    }
                },user.sessionsHosting);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }

        if (context instanceof OnCreateSessionClickedListener) {
            onCreateSessionClickedListener = (OnCreateSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onCreateSessionClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        onCreateSessionClickedListener = null;
    }*/

    public interface OnCreateSessionClickedListener {
        void OnCreateSessionClicked();
    }

}
