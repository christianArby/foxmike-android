package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.foxmike.android.R;
/**
 * This opens up a dialog fragment in order to choose way to sort and filter
 */
public class SortAndFilterFragment extends DialogFragment {

    private RadioGroup radioGroup;
    private RadioButton distance1, distance2;
    private ToggleButton sortDateTB;
    private ToggleButton sortDistanceTB;
    private OnFilterChangedListener onFilterChangedListener;
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
        sortDateTB.setText(getString(R.string.date_text));
        sortDateTB.setTextOn(getString(R.string.date_text));
        sortDateTB.setTextOff(getString(R.string.date_text));
        sortDateTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sortDistanceTB.setChecked(!b);
                mSortType ="date";
                onFilterChangedListener.OnSortTypeChanged(mSortType);
            }
        });

        // Setup price filter
        Spinner spinnerMin = (Spinner) view.findViewById(R.id.priceSpinnerMin);
        ArrayAdapter<CharSequence> adapterPriceMin = ArrayAdapter.createFromResource(getActivity(),
                R.array.price_array_filter_min, android.R.layout.simple_spinner_item);
        adapterPriceMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMin.setAdapter(adapterPriceMin);
        spinnerMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // TODO FIX CURRENCY
                String minPriceString = (String) spinnerMin.getSelectedItem();
                int minPrice;
                String sPriceMin = minPriceString.replaceAll("[^0-9]", "");
                if (sPriceMin.length()>1) {
                    minPrice = Integer.parseInt(sPriceMin);
                } else {
                    minPrice = 0;
                }
                onFilterChangedListener.OnMinPriceChanged(minPrice);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onFilterChangedListener.OnMinPriceChanged(0);
            }
        });

        Spinner spinnerMax = (Spinner) view.findViewById(R.id.priceSpinnerMax);
        ArrayAdapter<CharSequence> adapterPriceMax = ArrayAdapter.createFromResource(getActivity(),
                R.array.price_array_filter_max, android.R.layout.simple_spinner_item);
        adapterPriceMax.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMax.setAdapter(adapterPriceMax);
        spinnerMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO FIX CURRENCY
                String maxPriceString = (String) spinnerMax.getSelectedItem();
                int maxPrice;
                String sPriceMax = maxPriceString.replaceAll("[^0-9]", "");
                if (sPriceMax.length()>1) {
                    maxPrice = Integer.parseInt(sPriceMax);
                } else {
                    maxPrice = 0;
                }
                onFilterChangedListener.OnMaxPriceChanged(maxPrice);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onFilterChangedListener.OnMaxPriceChanged(100000);
            }
        });

        // Setup sort on Distance toggle button
        sortDistanceTB.setText(getString(R.string.distance_word));
        sortDistanceTB.setTextOn(getString(R.string.distance_word));
        sortDistanceTB.setTextOff(getString(R.string.distance_word));
        sortDistanceTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sortDateTB.setChecked(!b);
                mSortType = "distance";
                onFilterChangedListener.OnSortTypeChanged(mSortType);
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
                onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
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

        if (context instanceof OnFilterChangedListener) {
            onFilterChangedListener = (OnFilterChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFilterChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFilterChangedListener = null;
    }

    public interface OnFilterChangedListener {
        void OnSortTypeChanged(String sortType);
        void OnDistanceFilterChanged(int filterDistance);
        void OnMinPriceChanged(int minPrice);
        void OnMaxPriceChanged(int maxPrice);
    }
}