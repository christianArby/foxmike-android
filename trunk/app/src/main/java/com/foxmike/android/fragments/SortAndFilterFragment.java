package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSessionTypesAdapter;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.foxmike.android.utils.Distance.DISTANCE_INTEGERS_SE;
import static com.foxmike.android.utils.Distance.DISTANCE_STRINGS_SE;
import static com.foxmike.android.utils.Price.PRICES_INTEGERS_SE;
import static com.foxmike.android.utils.Price.PRICES_STRINGS_SE;

/**
 * This opens up a dialog fragment in order to choose way to sort and filter
 */
public class SortAndFilterFragment extends DialogFragment {

    public static final String TAG = SortAndFilterFragment.class.getSimpleName();

    //private RadioGroup radioGroup;
    private RadioButton distance1, distance2;
    private ToggleButton sortDateTB;
    private ToggleButton sortDistanceTB;
    private OnFilterChangedListener onFilterChangedListener;
    private static final String ARG_SORT= "sort";
    private static final String ARG_FILTER = "filter";
    private String mSortType;
    private int mFilterDistance = DISTANCE_INTEGERS_SE.get("1000 km");
    private ImageButton closeButton;
    private int minClicked = 0;
    private int maxClicked = 0;
    private int minPrice;
    private int maxPrice;
    private int minHour = 4;
    private int minMinute = 0;
    private int maxHour = 23;
    private int maxMinute = 45;
    private CrystalRangeSeekbar timeSeekbar;
    private TextView minTime;
    private TextView maxTime;
    private CrystalSeekbar distanceSeekbar;
    private TextView chosenDistance;
    private RecyclerView sessionTypeRV;
    private HashMap<String, Boolean> sessionTypeChosen = new HashMap<>();
    private LinearLayout sessionTypeProgress;

    public SortAndFilterFragment() {
        // Required empty public constructor
    }

    public static SortAndFilterFragment newInstance(String sort, int filter, int minPrice, int maxPrice, int minHour, int minMinute, int maxHour, int maxMinute, int distance, HashMap<String, Boolean> sessionTypeChosen) {
        SortAndFilterFragment fragment = new SortAndFilterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SORT, sort);
        args.putInt(ARG_FILTER, filter);
        args.putInt("minPrice", minPrice);
        args.putInt("maxPrice", maxPrice);
        args.putInt("minHour", minHour);
        args.putInt("minMinute", minMinute);
        args.putInt("maxHour", maxHour);
        args.putInt("maxMinute", maxMinute);
        args.putInt("distance", distance);
        args.putSerializable("sessionTypeChosen", sessionTypeChosen);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSortType = getArguments().getString(ARG_SORT);
            mFilterDistance = getArguments().getInt(ARG_FILTER);
            minPrice = getArguments().getInt("minPrice");
            maxPrice = getArguments().getInt("maxPrice");
            minHour = getArguments().getInt("minHour");
            minMinute = getArguments().getInt("minMinute");
            maxHour = getArguments().getInt("maxHour");
            maxMinute = getArguments().getInt("maxMinute");
            mFilterDistance = getArguments().getInt("distance");
            sessionTypeChosen = (HashMap<String, Boolean>)getArguments().getSerializable("sessionTypeChosen");
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
        timeSeekbar = view.findViewById(R.id.rangeSeekbar);
        minTime = view.findViewById(R.id.startTime);
        maxTime = view.findViewById(R.id.endTime);
        distanceSeekbar = view.findViewById(R.id.distanceSeekbar);
        chosenDistance = view.findViewById(R.id.chosenDistance);
        sessionTypeRV = view.findViewById(R.id.sessionTypeRV);
        sessionTypeProgress = view.findViewById(R.id.sessionTypeProgress);

        HashMap<String, Drawable> sessionTypeDrawables = new HashMap<>();
        sessionTypeDrawables.put("checked", getResources().getDrawable(R.drawable.ic_check_black_24dp));
        sessionTypeDrawables.put("default", getResources().getDrawable(R.mipmap.ic_people_black_24dp));
        ColorStateList checkedColor = ColorStateList.valueOf(Color.parseColor("#006959"));
        ColorStateList notCheckedColor = ColorStateList.valueOf(Color.parseColor("#00bfa5"));

        HashMap<String, ColorStateList> checkedColors = new HashMap<>();
        checkedColors.put("isChecked", checkedColor);
        checkedColors.put("isNotChecked", notCheckedColor);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        sessionTypeRV.setLayoutManager(linearLayoutManager);

        Locale current = getResources().getConfiguration().locale;
        FirebaseDatabase.getInstance().getReference().child("sessionTypeArray").child(current.toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                ArrayList<String> sessionTypeArray = new ArrayList<>();
                if (dataSnapshot.getValue()!=null) {
                    for (com.google.firebase.database.DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        sessionTypeArray.add(snapshot.getKey());
                    }
                    ListSessionTypesAdapter listSessionTypesAdapter = new ListSessionTypesAdapter(sessionTypeArray, sessionTypeChosen, sessionTypeDrawables, checkedColors, onFilterChangedListener);
                    sessionTypeRV.setAdapter(listSessionTypesAdapter);
                    sessionTypeProgress.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        timeSeekbar.setMinValue(240);
        timeSeekbar.setMaxValue(1425);

        timeSeekbar.setMinStartValue(minHour*60+minMinute);
        timeSeekbar.setMaxStartValue(maxHour*60+maxMinute);
        timeSeekbar.apply();


        timeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {

            }
        });

        timeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {

                int minHour = minValue.intValue()/60; //since both are ints, you get an int
                int minMinute =minValue.intValue() % 60;

                int maxHour = maxValue.intValue()/60; //since both are ints, you get an int
                int maxMinute = maxValue.intValue() % 60 ;



                minTime.setText(String.format("%02d:%02d", minHour, minMinute));
                maxTime.setText(String.format("%02d:%02d", maxHour, maxMinute));

                onFilterChangedListener.OnTimeRangeChanged(minHour, minMinute, maxHour, maxMinute);
            }
        });

        // Setup sort on DATE toggle button
        sortDateTB.setText(getString(R.string.date_text));
        sortDateTB.setTextOn(getString(R.string.date_text));
        sortDateTB.setTextOff(getString(R.string.date_text));

        // Set initial state of sort buttons
        if (mSortType.equals("distance")) {
            sortDistanceTB.setChecked(true);
            sortDateTB.setChecked(false);
        } else {
            sortDistanceTB.setChecked(false);
            sortDateTB.setChecked(true);
        }

        sortDateTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sortDistanceTB.setChecked(!b);
                mSortType ="date";
                onFilterChangedListener.OnSortTypeChanged(mSortType);
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

        // Setup price filter
        Spinner spinnerMin = (Spinner) view.findViewById(R.id.priceSpinnerMin);
        ArrayAdapter<CharSequence> adapterPriceMin = ArrayAdapter.createFromResource(getActivity(),
                R.array.price_array_filter_min, R.layout.spinner_text);
        adapterPriceMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMin.setAdapter(adapterPriceMin);

        if (minPrice!=0) {
            spinnerMin.setSelection(adapterPriceMin.getPosition(PRICES_STRINGS_SE.get(minPrice)));
        }
        spinnerMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (minClicked!=0) {
                    String minPriceString = (String) spinnerMin.getSelectedItem();
                    onFilterChangedListener.OnMinPriceChanged(PRICES_INTEGERS_SE.get(minPriceString));
                }
                minClicked++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onFilterChangedListener.OnMinPriceChanged(0);
            }
        });

        Spinner spinnerMax = (Spinner) view.findViewById(R.id.priceSpinnerMax);
        ArrayAdapter<CharSequence> adapterPriceMax = ArrayAdapter.createFromResource(getActivity(),
                R.array.price_array_filter_max, R.layout.spinner_text);
        adapterPriceMax.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMax.setAdapter(adapterPriceMax);
        spinnerMax.setSelection(adapterPriceMax.getPosition(PRICES_STRINGS_SE.get(maxPrice)));
        spinnerMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (maxClicked!=0) {
                    // TODO FIX CURRENCY
                    String maxPriceString = (String) spinnerMax.getSelectedItem();
                    onFilterChangedListener.OnMaxPriceChanged(PRICES_INTEGERS_SE.get(maxPriceString));
                }

                maxClicked++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onFilterChangedListener.OnMaxPriceChanged(PRICES_INTEGERS_SE.get("Max"));
            }
        });

        HashMap<Integer, Integer> seekbarDistanceValuesMap = new HashMap<>();
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("1000 km"), 60);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("80 km"), 50);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("40 km"), 40);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("16 km"), 30);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("8 km"), 20);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("3 km"), 10);

        distanceSeekbar.setMaxValue(60).setSteps(10).setMinStartValue(seekbarDistanceValuesMap.get(mFilterDistance)).apply();

        distanceSeekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                switch (value.intValue()) {
                    case 60:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("1000 km")));
                        if (mFilterDistance!=DISTANCE_INTEGERS_SE.get("1000 km")) {
                            mFilterDistance = DISTANCE_INTEGERS_SE.get("1000 km");
                            onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
                        }
                        return;
                    case 50:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(80));
                        mFilterDistance = 80;
                        onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
                        return;
                    case 40:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(40));
                        mFilterDistance = 40;
                        onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
                        return;
                    case 30:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(16));
                        mFilterDistance = 16;
                        onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
                        return;
                    case 20:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(8));
                        mFilterDistance = 8;
                        onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
                        return;
                    case 10:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(3));
                        mFilterDistance = 3;
                        onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
                }
            }
        });

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
        void OnTimeRangeChanged(int minHour, int minMinute, int maxHour, int maxMinute);
        void OnSessionTypeChanged(HashMap<String, Boolean> sessionTypeChosen);
    }
}