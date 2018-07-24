package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListStudiosAdapter;
import com.foxmike.android.interfaces.OnStudioBranchClickedListener;
import com.foxmike.android.interfaces.OnStudioBranchesFoundListener;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.models.Studio;
import com.foxmike.android.models.StudioBranch;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyFirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class HostListStudiosFragment extends Fragment {

    private OnStudioBranchClickedListener onStudioBranchClickedListener;
    private RecyclerView studioListRV;
    private ListStudiosAdapter listStudiosAdapter;
    private ArrayList<StudioBranch> studios = new ArrayList<>();
    private TextView noContent;


    public HostListStudiosFragment() {
        // Required empty public constructor
    }

    public static HostListStudiosFragment newInstance() {

        Bundle args = new Bundle();
        HostListStudiosFragment fragment = new HostListStudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_host_list_studios, container, false);

        studioListRV = (RecyclerView) view.findViewById(R.id.studios_list_RV);
        studioListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listStudiosAdapter = new ListStudiosAdapter(studios, onStudioBranchClickedListener, getContext());
        studioListRV.setAdapter(listStudiosAdapter);
        noContent = view.findViewById(R.id.noContent);

        return view;
    }

    // Function which downloads studios hosted by current user and saves the studios in the arraylist studios,
    private void initData() {
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Get the currents user's information from the database */
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                if (user.studios.size()!=0){
                    // Get which sessions the current user is hosting in a arraylist from the database
                    myFirebaseDatabase.getStudioBranches(user.getStudios(),new OnStudioBranchesFoundListener() {
                        @Override
                        public void OnStudioBranchesFound(ArrayList<StudioBranch> studioBranches) {

                            for (StudioBranch studioBranch: studioBranches) {
                                studios.add(studioBranch);
                            }

                            if (studios.size()>0) {
                                noContent.setVisibility(View.GONE);
                            } else {
                                noContent.setVisibility(View.VISIBLE);
                            }

                            if (listStudiosAdapter!=null) {
                                listStudiosAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStudioBranchClickedListener) {
            onStudioBranchClickedListener = (OnStudioBranchClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStudioBranchClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStudioBranchClickedListener = null;
    }

}
