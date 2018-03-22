package com.foxmike.android.adapters;
//Checked
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.foxmike.android.fragments.HostListSmallSessionsAdvFragment;
import com.foxmike.android.fragments.HostListSmallSessionsNotAdvFragment;
import com.foxmike.android.fragments.ListSmallSessionsFragment;
import com.foxmike.android.fragments.PlayerListSmallSessionsBookedFragment;
import com.foxmike.android.fragments.PlayerListSmallSessionsHistoryFragment;
import com.foxmike.android.fragments.WeekdayFilterFragment;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;

import java.util.ArrayList;

/**
 * This adapter takes two arraylists of sessions and creates two fragments of ListSmallSessionsFragment and populates a viewpager with these two fragments
 */

public class SmallSessionsPagerAdapter extends FragmentStatePagerAdapter {

    private boolean trainerMode;
    private String headerOne;
    private String headerTwo;

    public SmallSessionsPagerAdapter(FragmentManager fm, boolean trainerMode, String headerOne, String headerTwo) {
        super(fm);
        this.trainerMode = trainerMode;
        this.headerOne = headerOne;
        this.headerTwo = headerTwo;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (trainerMode) {
            if (position == 0) {
                fragment = HostListSmallSessionsAdvFragment.newInstance();
            }
            if (position == 1) {
                fragment = HostListSmallSessionsNotAdvFragment.newInstance();
            }
        } else {
            if (position == 0) {
                fragment = PlayerListSmallSessionsBookedFragment.newInstance();
            }
            if (position == 1) {
                fragment = PlayerListSmallSessionsHistoryFragment.newInstance();
            }

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return headerOne;
            case 1:
                return headerTwo;
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return POSITION_NONE;
    }

}