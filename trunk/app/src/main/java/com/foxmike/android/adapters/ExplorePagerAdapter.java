/*
package com.foxmike.android.adapters;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.foxmike.android.fragments.ListSessionsFragment;
import com.foxmike.android.fragments.MapsFragment;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;

import java.util.ArrayList;
import java.util.HashMap;

*/
/**
 * Created by chris on 2019-02-07.
 *//*


public class ExplorePagerAdapter extends FragmentPagerAdapter {
    private ListSessionsFragment listSessionsFragment;
    private MapsFragment mapsFragment;

    public ExplorePagerAdapter(FragmentManager fm) {
        super(fm);
    }





    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                listSessionsFragment = ListSessionsFragment.newInstance();
                return listSessionsFragment;
            case 1:
                mapsFragment = MapsFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
                mapsFragment.setArguments(bundle);
                return mapsFragment;
            default:
                return null;
        }


    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                listSessionsFragment = (ListSessionsFragment) createdFragment;
                break;
            case 1:
                mapsFragment = (MapsFragment) createdFragment;
                break;
        }
        return createdFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    public void updateFragments(HashMap<String, Session> sessions, ArrayList<Advertisement> advertisements, Location locationClosetoSessions) {
        // do work on the referenced Fragments, but first check if they
        // even exist yet, otherwise you'll get an NPE.

        boolean boListSessionsFrament = true;
        if (listSessionsFragment == null) {
            boListSessionsFrament = false;
        }

        boolean BomapsFragment = true;
        if (mapsFragment == null) {
            BomapsFragment = false;
        }


        if (listSessionsFragment != null) {
            listSessionsFragment.updateSessionListView(sessions, advertisements, locationClosetoSessions);
            listSessionsFragment.stopSwipeRefreshingSymbol();
        }

        if (mapsFragment != null) {
            mapsFragment.addMarkersToMap(sessions);
        }
    }

    public void goToMyLocation() {
        if (mapsFragment != null) {
            mapsFragment.goToMyLocation();
        }
    }

    public void switchMapActive(boolean mapIsVisible) {
        if (mapsFragment != null) {
            if (mapIsVisible) {
                mapsFragment.showRecylerView(true);
            } else {
                mapsFragment.showRecylerView(false);
            }
        }
    }

    public void locationNotFound() {
        if (listSessionsFragment != null) {
            listSessionsFragment.emptyListView();
            listSessionsFragment.stopSwipeRefreshingSymbol();
        }
    }


}
*/
