package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.foxmike.android.R;
import com.foxmike.android.adapters.InboxPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

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
