package com.foxmike.android.adapters;
//Checked
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.foxmike.android.fragments.HostListSmallAdvertisementsFragment;
import com.foxmike.android.fragments.HostListSmallSessionsFragment;
import com.foxmike.android.fragments.PlayerListSmallAdvertisementsBookedFragment;
import com.foxmike.android.fragments.PlayerListSmallAdvertisementsHistoryFragment;

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
                fragment = HostListSmallSessionsFragment.newInstance();
            }
            if (position == 1) {
                fragment = HostListSmallAdvertisementsFragment.newInstance();
            }
        } else {
            if (position == 0) {
                fragment = PlayerListSmallAdvertisementsBookedFragment.newInstance();
            }
            if (position == 1) {
                fragment = PlayerListSmallAdvertisementsHistoryFragment.newInstance();
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