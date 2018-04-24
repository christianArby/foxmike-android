package com.foxmike.android.adapters;
//Checked
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.foxmike.android.R;
import com.foxmike.android.fragments.ChatsFragment;
import com.foxmike.android.fragments.FriendsFragment;

/**
 * This adapter sets up the three tabs in Inbox fragment and fill those pages with corresponding fragments
 */

public class InboxPagerAdapter extends FragmentPagerAdapter{
    Context context;

    public InboxPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
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
                return context.getResources().getString(R.string.tab_messages);
            case 1:
                return context.getString(R.string.tab_friends);
            default:
                return null;
        }
    }
}