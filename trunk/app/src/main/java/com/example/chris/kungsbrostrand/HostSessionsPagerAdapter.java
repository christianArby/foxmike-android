package com.example.chris.kungsbrostrand;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2018-02-07.
 */

public class HostSessionsPagerAdapter extends FragmentPagerAdapter{

    private ArrayList<Session> advSessionsArrayList;
    private ArrayList<Session> notAdvSessionsArrayList;
    private HashMap<Integer,String> advSessionsSectionHeaders;
    private HashMap<Integer,String> notAdvSessionsSectionHeaders;

    public HostSessionsPagerAdapter(FragmentManager fm, ArrayList<Session> advSessionsArrayList, HashMap<Integer,String> advSessionsSectionHeaders, ArrayList<Session> notAdvSessionsArrayList, HashMap<Integer,String> notAdvSessionsSectionHeaders) {
        super(fm);
        this.advSessionsArrayList = advSessionsArrayList;
        this.notAdvSessionsArrayList = notAdvSessionsArrayList;
        this.advSessionsSectionHeaders = advSessionsSectionHeaders;
        this.notAdvSessionsSectionHeaders = notAdvSessionsSectionHeaders;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ListSmallSessionsFragment advListSessions = ListSmallSessionsFragment.newInstance(this.advSessionsArrayList, this.advSessionsSectionHeaders);
                return advListSessions;

            case 1:
                ListSmallSessionsFragment notAdvListSessions = ListSmallSessionsFragment.newInstance(this.notAdvSessionsArrayList, this.notAdvSessionsSectionHeaders);
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