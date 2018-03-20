package com.foxmike.android.adapters;
//Checked
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.foxmike.android.fragments.ListSmallSessionsFragment;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;

import java.util.ArrayList;

/**
 * This adapter takes two arraylists of sessions and creates two fragments of ListSmallSessionsFragment and populates a viewpager with these two fragments
 */

public class SmallSessionsPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<SessionBranch> firstSessionBranchArrayList;
    private ArrayList<SessionBranch> secondSessionBranchArrayList;
    private String headerOne;
    private String headerTwo;

    public SmallSessionsPagerAdapter(FragmentManager fm, ArrayList<SessionBranch> firstSessionBranchArrayList, ArrayList<SessionBranch> secondSessionBranchArrayList, String headerOne, String headerTwo) {
        super(fm);
        this.firstSessionBranchArrayList = firstSessionBranchArrayList;
        this.secondSessionBranchArrayList = secondSessionBranchArrayList;
        this.headerOne = headerOne;
        this.headerTwo = headerTwo;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ListSmallSessionsFragment firstSessions = ListSmallSessionsFragment.newInstance(this.firstSessionBranchArrayList);
                return firstSessions;

            case 1:
                ListSmallSessionsFragment secondSessions = ListSmallSessionsFragment.newInstance(this.secondSessionBranchArrayList);
                return secondSessions;

            default:
                return null;

        }
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

    public void updateData(ArrayList<SessionBranch> firstSessionBranchArrayList, ArrayList<SessionBranch> secondSessionBranchArrayList) {
        this.firstSessionBranchArrayList = firstSessionBranchArrayList;
        this.secondSessionBranchArrayList = secondSessionBranchArrayList;
    }

}