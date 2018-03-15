package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionsFoundListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.models.Session;
import com.foxmike.android.adapters.SmallSessionsPagerAdapter;
import com.foxmike.android.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class HostSessionsFragment extends Fragment {

    private OnCreateSessionClickedListener onCreateSessionClickedListener;
    private FloatingActionButton createSessionBtn;
    private ViewPager hostSessionsPager;
    private SmallSessionsPagerAdapter hostSessionsPagerAdapter;
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
        hostSessionsPager = (ViewPager) view.findViewById(R.id.host_sessions_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.host_sessions_tabs);
        // Setup create session button
        createSessionBtn = view.findViewById(R.id.add_session_btn);
        createSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateSessionClickedListener.OnCreateSessionClicked();
            }
        });
        // Function which load the tab layout and viewpager
        loadPages(false);

        return view;
    }
    // Function which load the tab layout and viewpager
    public void loadPages(final boolean update) {

        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Get the currents user's information from the database */
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                if (user.sessionsHosting.size()!=0){
                    // Get which sessions the current user is hosting in a arraylist from the database
                    myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                        @Override
                        public void OnSessionsFound(ArrayList<Session> sessions) {
                            ArrayList<Session> sessionsAdv = new ArrayList<Session>();
                            ArrayList<Session> sessionsNotAdv = new ArrayList<Session>();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            final Calendar cal = Calendar.getInstance();
                            Date todaysDate = cal.getTime();
                            cal.add(Calendar.DATE,14);
                            Date twoWeeksDate = cal.getTime();
                            // Loop hosted sessions and see which are advertised, critera for advertised is that the boolean advertised is true and that the session date
                            // is after today's date
                            for (Session session: sessions) {
                                if (session.isAdvertised() && session.getSessionDate().getDateOfSession().after(todaysDate)) {
                                    sessionsAdv.add(session);
                                } else {
                                    sessionsNotAdv.add(session);
                                }
                            }
                            // Sort the two lists on date
                            Collections.sort(sessionsAdv);
                            Collections.sort(sessionsNotAdv);
                            // Input a dummysession with a section header for sessions further away than two weeks
                            int n = 0;
                            Boolean keepLooking = true;
                            while (n < sessionsAdv.size() && keepLooking) {
                                if (sessionsAdv.get(n).getSessionDate().getDateOfSession().after(twoWeeksDate) && keepLooking) {
                                    Session dummySession = new Session();
                                    dummySession.setImageUrl("sectionHeader");
                                    dummySession.setSessionName(getString(R.string.upcoming_listings));
                                    sessionsAdv.add(n, dummySession);
                                    keepLooking=false;
                                }
                                n++;
                            }
                            // If this function was initiated through an update update the fragments/pages otherwise build them from scratch
                            if (!update) {
                                hostSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), sessionsAdv, sessionsNotAdv,"Annonserade", "Avannonserade");
                                hostSessionsPager.setAdapter(hostSessionsPagerAdapter);
                                tabLayout.setupWithViewPager(hostSessionsPager);
                            } else {
                                hostSessionsPagerAdapter.updateData(sessionsAdv,sessionsNotAdv);
                                hostSessionsPagerAdapter.notifyDataSetChanged();
                            }
                        }
                    },user.sessionsHosting);

                    /* Else if user is not hosting any sessions create blank pages */
                } else {
                    ArrayList<Session> sessionsAdv = new ArrayList<Session>();
                    ArrayList<Session> sessionsNotAdv = new ArrayList<Session>();
                    if (!update) {
                        hostSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), sessionsAdv, sessionsNotAdv,"Annonserade", "Avannonserade");
                        hostSessionsPager.setAdapter(hostSessionsPagerAdapter);
                        tabLayout.setupWithViewPager(hostSessionsPager);
                    } else {
                        hostSessionsPagerAdapter.updateData(sessionsAdv,sessionsNotAdv);
                        hostSessionsPagerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        onCreateSessionClickedListener = null;
    }
    public interface OnCreateSessionClickedListener {
        void OnCreateSessionClicked();
    }
}