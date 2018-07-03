package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.utils.HeaderItemDecoration;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.adapters.sessionsAdapter;

import java.util.ArrayList;
/**
 * This fragment creates a list of sessions based on an arraylist of session objects given as arguments. It also
 * uses an location object in order to sort the sessions on distance from user
 */
public class  ListSessionsFragment extends Fragment {

    private RecyclerView mSessionList;
    private com.foxmike.android.adapters.sessionsAdapter sessionsAdapter;
    private Location currentLocation;
    private OnSessionClickedListener onSessionClickedListener;
    private OnRefreshSessionsListener onRefreshSessionsListener;
    private SwipeRefreshLayout listSessionsSwipeRefreshLayout;
    private OnListSessionsScrollListener onListSessionsScrollListener;
    private TextView noSessionsFound;

    public ListSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSessionsFragment newInstance() {
        ListSessionsFragment fragment = new ListSessionsFragment();
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

    /** Inflate the layout for this fragment (which is a RecyclerView) when creating view for fragment*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_sessions, container, false);
        mSessionList = view.findViewById(R.id.session_list);
        listSessionsSwipeRefreshLayout = view.findViewById(R.id.session_list_swipe_layout);
        noSessionsFound = view.findViewById(R.id.noSessionsFound);
        //mSessionList.setHasFixedSize(true); TODO What does this mean
        mSessionList.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Tell the parent activity when the list is scrolled (in order to hide FAB buttons)
        mSessionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                onListSessionsScrollListener.OnListSessionsScroll(dy);
            }
        });
        // Tell the parent activity when the list is swiped to refresh (in order to refresh data from database)
        listSessionsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshSessionsListener.OnRefreshSessions();
            }
        });

        return view;
    }

    /** Use sessionsAdapter to generate view mSessionList*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }
    // Function to refresh data in sessionsAdapter
    public void updateSessionListView(ArrayList<Session> sessions, Location location) {
        if (sessionsAdapter!=null) {
            noSessionsFound.setVisibility(View.GONE);
            sessionsAdapter.refreshData(sessions,location);
        } else {
            noSessionsFound.setVisibility(View.GONE);
            sessionsAdapter = new sessionsAdapter(sessions, getActivity(), location, onSessionClickedListener);
            if (mSessionList!=null) {
                HeaderItemDecoration headerItemDecoration = new HeaderItemDecoration(mSessionList, (HeaderItemDecoration.StickyHeaderInterface) sessionsAdapter);
                mSessionList.addItemDecoration(headerItemDecoration);
                mSessionList.setAdapter(sessionsAdapter);
            }
            sessionsAdapter.notifyDataSetChanged();
        }
    }

    public void emptyListView() {
        noSessionsFound.setVisibility(View.VISIBLE);
    }

    public void stopSwipeRefreshingSymbol() {
        listSessionsSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
        if (context instanceof OnRefreshSessionsListener) {
            onRefreshSessionsListener = (OnRefreshSessionsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRefreshSessionsListener");
        }
        if (context instanceof OnListSessionsScrollListener) {
            onListSessionsScrollListener = (OnListSessionsScrollListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListSessionsScrollListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
        onRefreshSessionsListener = null;
        onListSessionsScrollListener = null;
    }

    public interface OnRefreshSessionsListener {
        void OnRefreshSessions();
    }

    public interface OnListSessionsScrollListener {
        void OnListSessionsScroll(int dy);
    }
}