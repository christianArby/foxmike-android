package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foxmike.android.R;
import com.foxmike.android.adapters.InboxPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * This fragment sets up the tabs for the inbox and loads the fragments ChatsFragment, FriendsFragment and RequestsFragment
 */
public class InboxFragment extends Fragment {

    public static final String TAG = InboxFragment.class.getSimpleName();

    private FloatingActionButton searchFab;
    private ViewPager inboxPager;
    private InboxPagerAdapter inboxPagerAdapter;
    private TabLayout tabLayout;
    private OnSearchClickedListener onSearchClickedListener;
    private long mLastClickTime = 0;

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
        inboxPager.setOffscreenPageLimit(2);
        inboxPagerAdapter = new InboxPagerAdapter(getChildFragmentManager(), getResources().getString(R.string.tab_notifications), getResources().getString(R.string.tab_messages), getResources().getString(R.string.tab_friends));
        inboxPager.setAdapter(inboxPagerAdapter);

        inboxPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position==2) {
                    FirebaseDatabase.getInstance().getReference().child("unreadNotifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout = (TabLayout) view.findViewById(R.id.inbox_tabs);
        tabLayout.setupWithViewPager(inboxPager);
        searchFab = view.findViewById(R.id.searchFAB);

        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onSearchClickedListener.OnSearchClicked();
            }
        });

        return view;
    }
    public void setPage(int page) {
        if (inboxPager!=null) {
            inboxPager.setCurrentItem(page);
        }

    }

    public interface OnSearchClickedListener{
        void OnSearchClicked();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(inboxPager!=null) {
            inboxPager.setAdapter(null);
            inboxPager=null;
        }
        tabLayout = null;
        inboxPagerAdapter = null;
        searchFab = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchClickedListener) {
            onSearchClickedListener = (OnSearchClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onSearchClickedListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        onSearchClickedListener = null;
    }
}
