package com.foxmike.android.adapters;
//Checked

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.foxmike.android.fragments.ChatsFragment;
import com.foxmike.android.fragments.FriendsFragment;
import com.foxmike.android.fragments.NotificationsFragment;

/**
 * This adapter sets up the three tabs in Inbox fragment and fill those pages with corresponding fragments
 */

public class InboxPagerAdapter extends FragmentPagerAdapter{
    private String tab1;
    private String tab2;
    private String tab3;

    public InboxPagerAdapter(FragmentManager fm, String tab1, String tab2, String tab3) {
        super(fm);
        this.tab1 = tab1;
        this.tab2 = tab2;
        this.tab3 = tab3;


    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                return notificationsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
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
                return tab1;
            case 1:
                return tab2;
            case 2:
                return tab3;
            default:
                return null;
        }
    }
}
