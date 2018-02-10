package com.example.chris.kungsbrostrand;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by chris on 2018-02-07.
 */

public class HostSessionsPagerAdapter extends FragmentPagerAdapter{

    private ArrayList<Session> AdvSessionsArrayList;
    private ArrayList<Session> NotAdvSessionsArrayList;

    public HostSessionsPagerAdapter(FragmentManager fm, ArrayList<Session> AdvSessionsArrayList, ArrayList<Session> NotAdvSessionsArrayList) {
        super(fm);
        this.AdvSessionsArrayList = AdvSessionsArrayList;
        this.NotAdvSessionsArrayList = NotAdvSessionsArrayList;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ListSmallSessionsFragment advListSessions = ListSmallSessionsFragment.newInstance(this.AdvSessionsArrayList);
                return advListSessions;

            case 1:
                ListSmallSessionsFragment notAdvListSessions = ListSmallSessionsFragment.newInstance(this.NotAdvSessionsArrayList);
                return notAdvListSessions;

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
                return "Annonserade";
            case 1:
                return "Avannonserade";
            default:
                return null;
        }
    }

}