package com.example.chris.kungsbrostrand;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class InboxFragment extends Fragment {

    private ViewPager inboxPager;

    private InboxPagerAdapter inboxPagerAdapter;

    private TabLayout tabLayout;

    public InboxFragment() {
        // Required empty public constructor
    }

    public static InboxFragment newInstance() {
        InboxFragment fragment = new InboxFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        inboxPager = (ViewPager) view.findViewById(R.id.inboxPager);
        inboxPagerAdapter = new InboxPagerAdapter(getChildFragmentManager());

        inboxPager.setAdapter(inboxPagerAdapter);

        tabLayout = (TabLayout) view.findViewById(R.id.inbox_tabs);

        tabLayout.setupWithViewPager(inboxPager);


        return view;
    }

    public void cleanInboxListeners() {
        if (getChildFragmentManager().findFragmentByTag(makeFragmentName(R.id.inboxPager,1)) != null) {
            ChatsFragment chatsFragment = (ChatsFragment) getChildFragmentManager().findFragmentByTag(makeFragmentName(R.id.inboxPager,1));
            chatsFragment.cleanListeners();
        }
        if (getChildFragmentManager().findFragmentByTag(makeFragmentName(R.id.inboxPager,2)) != null) {
            FriendsFragment friendsFragment = (FriendsFragment) getChildFragmentManager().findFragmentByTag(makeFragmentName(R.id.inboxPager,2));
            friendsFragment.cleanListeners();
        }

    }

    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

}
