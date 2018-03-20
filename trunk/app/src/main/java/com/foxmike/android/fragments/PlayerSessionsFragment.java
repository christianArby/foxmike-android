package com.foxmike.android.fragments;
// Checked
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foxmike.android.R;
import com.foxmike.android.adapters.SmallSessionsPagerAdapter;
import com.foxmike.android.interfaces.OnSessionBranchesFoundListener;
import com.foxmike.android.interfaces.OnSessionsFoundListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
/**
 * This fragment lists all the sessions the current user has attended
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
        playerSessionsPager = (ViewPager) view.findViewById(R.id.player_sessions_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.player_sessions_tabs);
        loadPages(false);
        return view;
    }
    // This method sets up the tab layout with a viewpager which loads fragments of ListSmallSessionsFragment
    // If boolean update i set to true the method will tell the adapter to create new fragments and toss the old ones
    public void loadPages(final boolean update) {
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Get the currents user's information from the database */
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not attending any sessions create two blank pages */
                if (user.sessionsAttending.size()==0){
                    ArrayList<SessionBranch> sessionsBooked = new ArrayList<SessionBranch>();
                    ArrayList<SessionBranch> sessionBookedInPast = new ArrayList<SessionBranch>();
                    if (!update) {
                        playerSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), sessionsBooked, sessionBookedInPast,"BOKADE", "TIDIGARE");
                        playerSessionsPager.setAdapter(playerSessionsPagerAdapter);
                        tabLayout.setupWithViewPager(playerSessionsPager);
                    } else {
                        playerSessionsPagerAdapter.updateData(sessionsBooked, sessionBookedInPast);
                        playerSessionsPagerAdapter.notifyDataSetChanged();
                    }
                }
                myFirebaseDatabase.getSessionBranches(user.sessionsAttending, new OnSessionBranchesFoundListener() {
                    @Override
                    public void OnSessionBranchesFound(ArrayList<SessionBranch> sessionsBranches) {
                        ArrayList<SessionBranch> sessionsBooked = new ArrayList<SessionBranch>();
                        ArrayList<SessionBranch> sessionBookedInPast = new ArrayList<SessionBranch>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        final Calendar cal = Calendar.getInstance();
                        Date todaysDate = cal.getTime();

                        for (SessionBranch sessionBranch: sessionsBranches) {
                            if (sessionBranch.getSession().getSessionDate().getDateOfSession().after(todaysDate)) {
                                sessionsBooked.add(sessionBranch);
                            } else {
                                sessionBookedInPast.add(sessionBranch);
                            }
                        }
                        Collections.sort(sessionsBooked);
                        Collections.sort(sessionBookedInPast);
                        if (!update) {
                            playerSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), sessionsBooked, sessionBookedInPast,"BOKADE", "TIDIGARE");
                            playerSessionsPager.setAdapter(playerSessionsPagerAdapter);
                            tabLayout.setupWithViewPager(playerSessionsPager);
                        } else {
                            playerSessionsPagerAdapter.updateData(sessionsBooked, sessionBookedInPast);
                            playerSessionsPagerAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }
}
