package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSmallSessionsAdapter;
import com.foxmike.android.interfaces.OnSessionBranchClickedListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.SessionBranch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
/**
 * This fragment creates a list of sessions based on an arraylist of session objects given as arguments
 */
public class ListSmallSessionsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnSessionBranchClickedListener onSessionBranchClickedListener;
    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsAdapter listSmallSessionsAdapter;
    private ArrayList<SessionBranch> sessionBranchArrayList = new ArrayList<>();


    public ListSmallSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSmallSessionsFragment newInstance(ArrayList<SessionBranch> sessionBranchArrayList) {

        ListSmallSessionsFragment fragment = new ListSmallSessionsFragment();
        Bundle args = new Bundle();
        String strSessionBranchArrayList = new Gson().toJson(sessionBranchArrayList);
        args.putString("sessionBranchArrayList",strSessionBranchArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String strSessionBranchArrayList = getArguments().getString("sessionBranchArrayList");
            sessionBranchArrayList = new Gson().fromJson(strSessionBranchArrayList, new TypeToken<ArrayList<SessionBranch>>(){}.getType());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_small_sessions, container, false);

        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listSmallSessionsAdapter = new ListSmallSessionsAdapter(sessionBranchArrayList, onSessionBranchClickedListener, getContext());
        smallSessionsListRV.setAdapter(listSmallSessionsAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionBranchClickedListener) {
            onSessionBranchClickedListener = (OnSessionBranchClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionBranchClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionBranchClickedListener = null;
    }

}
