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

        playerSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), false, getString(R.string.booked_text), getString(R.string.booked_history_text));
        playerSessionsPager.setAdapter(playerSessionsPagerAdapter);
        tabLayout.setupWithViewPager(playerSessionsPager);

        return view;
    }

    // Function which load the tab layout and viewpager
    /*public void loadPages(final boolean update) {
        // If this function was initiated through an update update the fragments/pages otherwise build them from scratch
        if (!update) {
            playerSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), false, getString(R.string.booked_text), getString(R.string.booked_history_text));
            playerSessionsPager.setAdapter(playerSessionsPagerAdapter);
            tabLayout.setupWithViewPager(playerSessionsPager);
        } else {
            playerSessionsPagerAdapter.notifyDataSetChanged();
        }
    }*/
}
