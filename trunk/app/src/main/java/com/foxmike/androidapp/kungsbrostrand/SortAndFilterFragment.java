package com.foxmike.androidapp.kungsbrostrand;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class SortAndFilterFragment extends DialogFragment {

    private RadioGroup radioGroup;
    private RadioButton distance1, distance2;
    private ToggleButton sortDateTB;
    private ToggleButton sortDistanceTB;
    private OnListSessionsSortListener onListSessionsSortListener;
    private OnListSessionsFilterListener onListSessionsFilterListener;
    private static final String ARG_SORT= "sort";
    private static final String ARG_FILTER = "filter";
    private String mSortType;
    private int mFilterDistance = 3000;
    private ImageButton closeButton;

    public SortAndFilterFragment() {
        // Required empty public constructor
    }

    public static SortAndFilterFragment newInstance(String sort, int filter) {
        SortAndFilterFragment fragment = new SortAndFilterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SORT, sort);
        args.putInt(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSortType = getArguments().getString(ARG_SORT);
            mFilterDistance = getArguments().getInt(ARG_FILTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sort_and_filter, container, false);
        sortDateTB = view.findViewById(R.id.sortDateToggle);
        sortDistanceTB = view.findViewById(R.id.sortDistanceToggle);
        closeButton = view.findViewById(R.id.closeImageButton);

        // Setup sort on DATE toggle button
        sortDateTB.setText("Datum");
        sortDateTB.setTextOn("Datum");
        sortDateTB.setTextOff("Datum");
        sortDateTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sortDistanceTB.setChecked(!b);
                mSortType ="date";
                onListSessionsSortListener.OnListSessionsSort(mSortType);
            }
        });

        // Setup sort on Distance toggle button
        sortDistanceTB.setText("Avstånd");
        sortDistanceTB.setTextOn("Avstånd");
        sortDistanceTB.setTextOff("Avstånd");
        sortDistanceTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sortDateTB.setChecked(!b);
                mSortType = "distance";
                onListSessionsSortListener.OnListSessionsSort(mSortType);
            }
        });

        // Set initial state of sort buttons
        if (mSortType.equals("distance")) {
            sortDistanceTB.setChecked(true);
            sortDateTB.setChecked(false);
        } else {
            sortDistanceTB.setChecked(false);
            sortDateTB.setChecked(true);
        }

        // Setup filter on distance group buttons
        radioGroup = view.findViewById(R.id.filterDistanceRadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                switch (radioButtonID) {
                    case R.id.distance1:
                        mFilterDistance = getActivity().getResources().getInteger(R.integer.distance1);
                        break;
                    case R.id.distance2:
                        mFilterDistance = getActivity().getResources().getInteger(R.integer.distance2);
                        break;
                    case R.id.distance3:
                        mFilterDistance = getActivity().getResources().getInteger(R.integer.distance3);
                        break;
                    case R.id.distance4:
                        mFilterDistance = getActivity().getResources().getInteger(R.integer.distance4);
                        break;
                    case R.id.distance5:
                        mFilterDistance = getActivity().getResources().getInteger(R.integer.distance5);
                        break;
                    case R.id.distance6:
                        mFilterDistance = getActivity().getResources().getInteger(R.integer.distanceMax);
                        break;
                }
                onListSessionsFilterListener.OnListSessionsFilter(mFilterDistance);
            }
        });

        // Set initial state of filter buttons
        if (mFilterDistance== getActivity().getResources().getInteger(R.integer.distance1)) {
            radioGroup.check(R.id.distance1);
        }
        if (mFilterDistance== getActivity().getResources().getInteger(R.integer.distance2)) {
            radioGroup.check(R.id.distance2);
        }
        if (mFilterDistance== getActivity().getResources().getInteger(R.integer.distance3)) {
            radioGroup.check(R.id.distance3);
        }
        if (mFilterDistance== getActivity().getResources().getInteger(R.integer.distance4)) {
            radioGroup.check(R.id.distance4);
        }
        if (mFilterDistance== getActivity().getResources().getInteger(R.integer.distance5)) {
            radioGroup.check(R.id.distance5);
        }
        if (mFilterDistance== getActivity().getResources().getInteger(R.integer.distanceMax)) {
            radioGroup.check(R.id.distance6);
        }

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListSessionsSortListener) {
            onListSessionsSortListener = (OnListSessionsSortListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListSessionsSortListener");
        }
        if (context instanceof OnListSessionsFilterListener) {
            onListSessionsFilterListener = (OnListSessionsFilterListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListSessionsFilterListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onListSessionsSortListener = null;
        onListSessionsFilterListener = null;
    }

    public interface OnListSessionsSortListener {
        void OnListSessionsSort(String sortType);
    }

    public interface OnListSessionsFilterListener {
        void OnListSessionsFilter(int filterDistance);
    }
}