package com.foxmike.android.adapters;

import android.os.Parcelable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 2018-11-02.
 */

public class BottomNavigationAdapter extends SmartFragmentStatePagerAdapter {
    private final List<Fragment> fragments = new ArrayList<>();

    // TODO Check so that this is not slowing down lists, added because app crashed when it tried to reatin state of old fragments, has to do with backstack
    @Override
    public Parcelable saveState() {
        return null;
    }

    public BottomNavigationAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }
    // Our custom method that populates this Adapter with Fragments
    public void addFragments(Fragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
