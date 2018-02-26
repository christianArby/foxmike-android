package com.example.chris.kungsbrostrand;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 *
 */
public class PlayerSessionsFragment extends Fragment {

    private ViewPager playerSessionsPager;
    private SmallSessionsPagerAdapter playerSessionsPagerAdapter;
    private TabLayout tabLayout;

    public PlayerSessionsFragment() {
        // Required empty public constructor
    }

    public static PlayerSessionsFragment newInstance() {
        PlayerSessionsFragment fragment = new PlayerSessionsFragment();
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
        final View view = inflater.inflate(R.layout.fragment_player_sessions, container, false);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        /* Get the currents user's information from the database */
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not hosting any sessions set that the sessionsHosting content has beeen found*/
                if (user.sessionsAttending.size()==0){

                }
                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {

                        ArrayList<Session> sessionsBooked = new ArrayList<Session>();
                        ArrayList<Session> sessionBookedInPast = new ArrayList<Session>();
                        //HashMap<Integer,String> sessionsAdvSectionHeaders = new HashMap<>();
                        //HashMap<Integer,String> sessionsNotAdvSectionHeaders = new HashMap<>();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        final Calendar cal = Calendar.getInstance();
                        Date todaysDate = cal.getTime();

                        for (Session session: sessions) {
                            if (session.getSessionDate().getDateOfSession().after(todaysDate)) {
                                sessionsBooked.add(session);
                            } else {
                                sessionBookedInPast.add(session);
                            }
                        }

                        Collections.sort(sessionsBooked);
                        Collections.sort(sessionBookedInPast);

                        playerSessionsPager = (ViewPager) view.findViewById(R.id.player_sessions_pager);

                        playerSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), sessionsBooked, sessionBookedInPast,"BOKADE", "TIDIGARE");

                        playerSessionsPager.setAdapter(playerSessionsPagerAdapter);

                        tabLayout = (TabLayout) view.findViewById(R.id.player_sessions_tabs);

                        tabLayout.setupWithViewPager(playerSessionsPager);

                    }
                },user.sessionsAttending);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
