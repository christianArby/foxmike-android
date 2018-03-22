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
import com.foxmike.android.interfaces.OnSessionBranchesFoundListener;
import com.foxmike.android.interfaces.OnSessionsFoundListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.models.SessionBranch;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.models.Session;
import com.foxmike.android.adapters.SmallSessionsPagerAdapter;
import com.foxmike.android.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This fragment lists all sessions the current user is hosting
 */
public class HostSessionsFragment extends Fragment {

    private OnCreateSessionClickedListener onCreateSessionClickedListener;
    private FloatingActionButton createSessionBtn;
    private ViewPager hostSessionsPager;
    private SmallSessionsPagerAdapter hostSessionsPagerAdapter;
    private TabLayout tabLayout;

    public HostSessionsFragment() {
        // Required empty public constructor
    }

    public static HostSessionsFragment newInstance() {
        HostSessionsFragment fragment = new HostSessionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Get the view fragment_user_account */
        final View view = inflater.inflate(R.layout.fragment_host_sessions, container, false);
        hostSessionsPager = (ViewPager) view.findViewById(R.id.host_sessions_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.host_sessions_tabs);
        // Setup create session button
        createSessionBtn = view.findViewById(R.id.add_session_btn);
        createSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateSessionClickedListener.OnCreateSessionClicked();
            }
        });

        hostSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), true, getString(R.string.advertised_text), getString(R.string.advertised_not_text));
        hostSessionsPager.setAdapter(hostSessionsPagerAdapter);
        tabLayout.setupWithViewPager(hostSessionsPager);


        return view;
    }

    // Function which load the tab layout and viewpager
    public void loadPages(final boolean update) {
        // If this function was initiated through an update update the fragments/pages otherwise build them from scratch
        if (!update) {
            hostSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), true, getString(R.string.advertised_text), getString(R.string.advertised_not_text));
            hostSessionsPager.setAdapter(hostSessionsPagerAdapter);
            tabLayout.setupWithViewPager(hostSessionsPager);
        } else {
            hostSessionsPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateSessionClickedListener) {
            onCreateSessionClickedListener = (OnCreateSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onCreateSessionClickedListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        onCreateSessionClickedListener = null;
    }
    public interface OnCreateSessionClickedListener {
        void OnCreateSessionClicked();
    }
}