package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foxmike.android.R;
import com.foxmike.android.adapters.InboxPagerAdapter;
/**
 * This fragment sets up the tabs for the inbox and loads the fragments ChatsFragment, FriendsFragment and RequestsFragment
 */
public class InboxFragment extends Fragment {

    private FloatingActionButton searchFab;
    private ViewPager inboxPager;
    private InboxPagerAdapter inboxPagerAdapter;
    private TabLayout tabLayout;
    private OnSearchClickedListener onSearchClickedListener;

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
        inboxPagerAdapter = new InboxPagerAdapter(getChildFragmentManager(), getContext());
        inboxPager.setAdapter(inboxPagerAdapter);
        tabLayout = (TabLayout) view.findViewById(R.id.inbox_tabs);
        tabLayout.setupWithViewPager(inboxPager);
        searchFab = view.findViewById(R.id.searchFAB);

        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchClickedListener.OnSearchClicked();
            }
        });

        return view;
    }
    public void cleanInboxListeners() {

    }

    public interface OnSearchClickedListener{
        void OnSearchClicked();
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
