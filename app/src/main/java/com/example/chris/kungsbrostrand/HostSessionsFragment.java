package com.example.chris.kungsbrostrand;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 *
 */
public class HostSessionsFragment extends Fragment {

    private LinearLayout list1;
    private LinearLayout list2;
    private LinearLayout list3;

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
        /* Get the view fragment_user_profile */
        final View view = inflater.inflate(R.layout.fragment_host_sessions, container, false);

        list1 = view.findViewById(R.id.list1);
        View list1HeadingView = inflater.inflate(R.layout.your_sessions_heading,list1,false);
        TextView list1Heading = list1HeadingView.findViewById(R.id.yourSessionsHeadingTV);
        list1Heading.setText("Sessions advertised within 2 weeeks");
        list1.addView(list1HeadingView);

        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        /* Get the currents user's information from the database */
        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not hosting any sessions set that the sessionsHosting content has beeen found*/
                if (user.sessionsHosting.size()==0){

                }
                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {

                        ArrayList<Session> sessionsAdvWithin2weeks = new ArrayList<Session>();
                        ArrayList<Session> sessionsAdvNotWithin2weeks = new ArrayList<Session>();
                        ArrayList<Session> sessionsNotAdv = new ArrayList<Session>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        final Calendar cal = Calendar.getInstance();
                        Date todaysDate = cal.getTime();
                        cal.add(Calendar.DATE,14);
                        Date twoWeeksDate = cal.getTime();
                        int n=0;
                        int n1=0;
                        int n2=0;
                        for (Session session: sessions) {
                            try {
                                Date sessionDate = sdf.parse(session.textSDF(session.getSessionDate()));

                                if (session.isAdvertised()) {
                                    if (sessionDate.after(todaysDate) && sessionDate.before(twoWeeksDate)) {
                                        sessionsAdvWithin2weeks.add(n, session);
                                        n++;
                                    } else {
                                        sessionsAdvNotWithin2weeks.add(n1, session);
                                        n1++;
                                    }
                                } else {
                                    sessionsNotAdv.add(n2, session);
                                }

                            } catch (ParseException e) {
                                //handle exception
                            }
                        }



                        SessionRow sessionRow1 = new SessionRow();
                        sessionRow1.populateList(sessionsAdvWithin2weeks, getActivity(),list1);

                        View list2HeadingView = inflater.inflate(R.layout.your_sessions_heading,list1,false);
                        TextView list2Heading = list2HeadingView.findViewById(R.id.yourSessionsHeadingTV);
                        list2Heading.setText("Sessions advertised Not within 2 weeeks");
                        list1.addView(list2HeadingView);

                        SessionRow sessionRow2 = new SessionRow();
                        sessionRow2.populateList(sessionsAdvNotWithin2weeks, getActivity(),list1);

                        View list3HeadingView = inflater.inflate(R.layout.your_sessions_heading,list1,false);
                        TextView list3Heading = list3HeadingView.findViewById(R.id.yourSessionsHeadingTV);
                        list3Heading.setText("Sessions not advertised");
                        list1.addView(list3HeadingView);

                        SessionRow sessionRow3 = new SessionRow();
                        sessionRow3.populateList(sessionsNotAdv, getActivity(),list1);

                    }
                },user.sessionsHosting);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
