package com.foxmike.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.foxmike.android.fragments.ChatsFragment;
import com.foxmike.android.fragments.FriendsFragment;
import com.foxmike.android.fragments.RequestsFragment;

/**
 * Created by chris on 2017-11-18.
 */

public class InboxPagerAdapter extends FragmentPagerAdapter{

    public InboxPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;


            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "MEDDELANDEN";
            case 1:
                return "VÄNNER";
            case 2:
                return "NOTISER";
            default:
                return null;
        }
    }
}
