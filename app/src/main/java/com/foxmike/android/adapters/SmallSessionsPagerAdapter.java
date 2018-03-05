package com.foxmike.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.foxmike.android.fragments.ListSmallSessionsFragment;
import com.foxmike.android.models.Session;

import java.util.ArrayList;

/**
 * Created by chris on 2018-02-07.
 */

public class SmallSessionsPagerAdapter extends FragmentPagerAdapter{

    private ArrayList<Session> firstSessionsArrayList;
    private ArrayList<Session> secondSessionsArrayList;
    private String headerOne;
    private String headerTwo;

    public SmallSessionsPagerAdapter(FragmentManager fm, ArrayList<Session> firstSessionsArrayList, ArrayList<Session> secondSessionsArrayList, String headerOne, String headerTwo) {
        super(fm);
        this.firstSessionsArrayList = firstSessionsArrayList;
        this.secondSessionsArrayList = secondSessionsArrayList;
        this.headerOne = headerOne;
        this.headerTwo = headerTwo;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ListSmallSessionsFragment firstSessions = ListSmallSessionsFragment.newInstance(this.firstSessionsArrayList);
                return firstSessions;

            case 1:
                ListSmallSessionsFragment secondSessions = ListSmallSessionsFragment.newInstance(this.secondSessionsArrayList);
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

}