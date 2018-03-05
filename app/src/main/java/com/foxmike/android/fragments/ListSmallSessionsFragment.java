package com.foxmike.android.fragments;


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
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;


public class ListSmallSessionsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnSessionClickedListener onSessionClickedListener;

    private RecyclerView smallSessionsListRV;
    private ListSmallSessionsAdapter listSmallSessionsAdapter;
    private ArrayList<Session> sessionArrayList = new ArrayList<>();


    public ListSmallSessionsFragment() {
        // Required empty public constructor
    }

    public static ListSmallSessionsFragment newInstance(ArrayList<Session> sessionArrayList) {


        ListSmallSessionsFragment fragment = new ListSmallSessionsFragment();
        Bundle args = new Bundle();
        String strSessionArrayList = new Gson().toJson(sessionArrayList);
        args.putString("sessionArrayList",strSessionArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String strSessionArrayList = getArguments().getString("sessionArrayList");
            sessionArrayList = new Gson().fromJson(strSessionArrayList, new TypeToken<ArrayList<Session>>(){}.getType());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_small_sessions, container, false);

        smallSessionsListRV = (RecyclerView) view.findViewById(R.id.small_sessions_list_RV);
        smallSessionsListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listSmallSessionsAdapter = new ListSmallSessionsAdapter(sessionArrayList, onSessionClickedListener, getContext());
        smallSessionsListRV.setAdapter(listSmallSessionsAdapter);

        return view;


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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
    }

}
