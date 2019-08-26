package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.foxmike.android.R;
import com.foxmike.android.adapters.ListSessionTypesAdapter;

import java.util.HashMap;

import static com.foxmike.android.utils.Distance.DISTANCE_INTEGERS_SE;
import static com.foxmike.android.utils.Distance.DISTANCE_STRINGS_SE;
import static com.foxmike.android.utils.Price.PRICES_INTEGERS_SE;
import static com.foxmike.android.utils.Price.PRICES_STRINGS_SE;
import static com.foxmike.android.utils.StaticResources.maxDefaultHour;
import static com.foxmike.android.utils.StaticResources.maxDefaultMinute;
import static com.foxmike.android.utils.StaticResources.minDefaultHour;
import static com.foxmike.android.utils.StaticResources.minDefaultMinute;

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
    private int mFilterDistance = DISTANCE_INTEGERS_SE.get("100 mil");
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
    private HashMap<String,String> sessionTypeDictionary;
    private TextView clearAll;
    private ListSessionTypesAdapter listSessionTypesAdapter;
    private HashMap<String, Drawable> sessionTypeDrawables;
    private HashMap<String, ColorStateList> checkedColors;
    private Spinner spinnerMax;
    private Spinner spinnerMin;
    private ArrayAdapter<CharSequence> adapterPriceMin;
    private ArrayAdapter<CharSequence> adapterPriceMax;
    private Switch foxmikePlusSwitch;
    private boolean foxmikePlusOnly;

    public SortAndFilterFragment() {
        // Required empty public constructor
    }

    public static SortAndFilterFragment newInstance(String sort, int filter, int minPrice, int maxPrice, int minHour, int minMinute, int maxHour, int maxMinute, int distance, boolean foxmikePlusOnly, HashMap<String, Boolean> sessionTypeChosen, HashMap<String,String> sessionTypeDictionary) {
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
        args.putBoolean("foxmikePlusOnly", foxmikePlusOnly);
        args.putSerializable("sessionTypeChosen", sessionTypeChosen);
        args.putSerializable("sessionTypeDictionary", sessionTypeDictionary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFilterDistance = getArguments().getInt(ARG_FILTER);
            minPrice = getArguments().getInt("minPrice");
            maxPrice = getArguments().getInt("maxPrice");
            minHour = getArguments().getInt("minHour");
            minMinute = getArguments().getInt("minMinute");
            maxHour = getArguments().getInt("maxHour");
            maxMinute = getArguments().getInt("maxMinute");
            mFilterDistance = getArguments().getInt("distance");
            foxmikePlusOnly = getArguments().getBoolean("foxmikePlusOnly", foxmikePlusOnly);
            sessionTypeChosen = (HashMap<String, Boolean>)getArguments().getSerializable("sessionTypeChosen");
            sessionTypeDictionary = (HashMap<String, String>)getArguments().getSerializable("sessionTypeDictionary");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sort_and_filter, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        closeButton = view.findViewById(R.id.closeImageButton);
        timeSeekbar = view.findViewById(R.id.rangeSeekbar);
        minTime = view.findViewById(R.id.startTime);
        maxTime = view.findViewById(R.id.endTime);
        distanceSeekbar = view.findViewById(R.id.distanceSeekbar);
        chosenDistance = view.findViewById(R.id.chosenDistance);
        sessionTypeRV = view.findViewById(R.id.sessionTypeRV);
        sessionTypeProgress = view.findViewById(R.id.sessionTypeProgress);
        clearAll = view.findViewById(R.id.clearAll);
        foxmikePlusSwitch = view.findViewById(R.id.foxmikePlusSwitch);

        sessionTypeDrawables = new HashMap<>();
        sessionTypeDrawables.put("checked", getResources().getDrawable(R.drawable.ic_check_black_24dp));
        sessionTypeDrawables.put("DDD", getResources().getDrawable(R.drawable.strength));
        sessionTypeDrawables.put("AAA", getResources().getDrawable(R.drawable.running));
        sessionTypeDrawables.put("BBB", getResources().getDrawable(R.drawable.yoga));
        sessionTypeDrawables.put("EEE", getResources().getDrawable(R.drawable.cardio));
        sessionTypeDrawables.put("CCC", getResources().getDrawable(R.drawable.crossfit));
        sessionTypeDrawables.put("default", getResources().getDrawable(R.mipmap.ic_people_black_24dp));
        ColorStateList checkedColor = ColorStateList.valueOf(Color.parseColor("#006959"));
        ColorStateList notCheckedColor = ColorStateList.valueOf(Color.parseColor("#00bfa5"));

        if (foxmikePlusOnly) {
            foxmikePlusSwitch.setChecked(true);
        }

        foxmikePlusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                foxmikePlusOnly = isChecked;
                onFilterChangedListener.OnFoxmikePlusOnlyChanged(isChecked);
            }
        });

        checkedColors = new HashMap<>();
        checkedColors.put("isChecked", checkedColor);
        checkedColors.put("isNotChecked", notCheckedColor);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        sessionTypeRV.setLayoutManager(linearLayoutManager);

        listSessionTypesAdapter = new ListSessionTypesAdapter(sessionTypeDictionary, sessionTypeChosen, sessionTypeDrawables, checkedColors, onFilterChangedListener, new OnChosenTypesChangedListener() {
            @Override
            public void OnChosenTypeChanged(HashMap<String, Boolean> changedSessionTypeChosen) {
                sessionTypeChosen = changedSessionTypeChosen;
                toggleClearFilterTextColor();
            }
        });
        sessionTypeRV.setAdapter(listSessionTypesAdapter);
        sessionTypeProgress.setVisibility(View.GONE);

        timeSeekbar.setMinValue(240);
        timeSeekbar.setMaxValue(1425);

        timeSeekbar.setMinStartValue(minHour*60+minMinute);
        timeSeekbar.setMaxStartValue(maxHour*60+maxMinute);
        timeSeekbar.apply();


        timeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                minHour = minValue.intValue()/60; //since both are ints, you get an int
                minMinute =minValue.intValue() % 60;

                maxHour = maxValue.intValue()/60; //since both are ints, you get an int
                maxMinute = maxValue.intValue() % 60 ;
                onFilterChangedListener.OnTimeRangeChanged(minHour, minMinute, maxHour, maxMinute);
                toggleClearFilterTextColor();

            }
        });

        timeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                minHour = minValue.intValue()/60; //since both are ints, you get an int
                minMinute =minValue.intValue() % 60;

                maxHour = maxValue.intValue()/60; //since both are ints, you get an int
                maxMinute = maxValue.intValue() % 60 ;
                minTime.setText(String.format("%02d:%02d", minHour, minMinute));
                maxTime.setText(String.format("%02d:%02d", maxHour, maxMinute));
            }
        });

        // Setup price filter
        spinnerMin = (Spinner) view.findViewById(R.id.priceSpinnerMin);
        adapterPriceMin = ArrayAdapter.createFromResource(getActivity(),
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
                    minPrice = PRICES_INTEGERS_SE.get(minPriceString);
                    toggleClearFilterTextColor();
                }
                minClicked++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onFilterChangedListener.OnMinPriceChanged(0);
                minPrice = 0;
                toggleClearFilterTextColor();
            }
        });

        spinnerMax = (Spinner) view.findViewById(R.id.priceSpinnerMax);
        adapterPriceMax = ArrayAdapter.createFromResource(getActivity(),
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
                    maxPrice = PRICES_INTEGERS_SE.get(maxPriceString);
                    toggleClearFilterTextColor();
                }

                maxClicked++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onFilterChangedListener.OnMaxPriceChanged(PRICES_INTEGERS_SE.get("Max"));
                maxPrice = PRICES_INTEGERS_SE.get("Max");
                toggleClearFilterTextColor();
            }
        });

        HashMap<Integer, Integer> seekbarDistanceValuesMap = new HashMap<>();
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("100 mil"), 70);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("6 mil"), 60);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("4 mil"), 50);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("16 km"), 40);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("8 km"), 30);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("3 km"), 20);
        seekbarDistanceValuesMap.put(DISTANCE_INTEGERS_SE.get("1 km"), 10);

        distanceSeekbar.setMaxValue(70).setSteps(10).setMinStartValue(seekbarDistanceValuesMap.get(mFilterDistance)).apply();

        distanceSeekbar.setOnSeekbarFinalValueListener(new OnSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number value) {
                onFilterChangedListener.OnDistanceFilterChanged(mFilterDistance);
            }
        });

        distanceSeekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                switch (value.intValue()) {
                    case 70:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("100 mil")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("100 mil");
                        break;
                    case 60:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("6 mil")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("6 mil");
                        break;
                    case 50:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("4 mil")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("4 mil");
                        break;
                    case 40:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("16 km")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("16 km");
                        break;
                    case 30:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("8 km")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("8 km");
                        break;
                    case 20:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("3 km")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("3 km");
                        break;
                    case 10:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("1 km")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("1 km");
                        break;
                    case 0:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(DISTANCE_INTEGERS_SE.get("1 km")));
                        mFilterDistance = DISTANCE_INTEGERS_SE.get("1 km");
                        break;
                    default:
                        chosenDistance.setText(DISTANCE_STRINGS_SE.get(mFilterDistance));
                        break;
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset all values
                minHour=minDefaultHour;
                minMinute=minDefaultMinute;
                maxHour=maxDefaultHour;
                maxMinute=maxDefaultMinute;
                maxPrice=PRICES_INTEGERS_SE.get("Max");
                minPrice=0;

                //reset all views
                timeSeekbar.setMinValue(240);
                timeSeekbar.setMaxValue(1425);
                timeSeekbar.setMinStartValue(minHour*60+minMinute);
                timeSeekbar.setMaxStartValue(maxHour*60+maxMinute);
                timeSeekbar.apply();

                if (listSessionTypesAdapter !=null) {
                    listSessionTypesAdapter = null;
                }
                for (String sessionTypeChoice: sessionTypeChosen.keySet()) {
                    sessionTypeChosen.put(sessionTypeChoice, false);
                }
                ListSessionTypesAdapter listSessionTypesAdapter = new ListSessionTypesAdapter(sessionTypeDictionary, sessionTypeChosen, sessionTypeDrawables, checkedColors, onFilterChangedListener, new OnChosenTypesChangedListener() {
                    @Override
                    public void OnChosenTypeChanged(HashMap<String, Boolean> changedSessionTypeChosen) {
                        sessionTypeChosen = changedSessionTypeChosen;
                        toggleClearFilterTextColor();
                    }
                });
                sessionTypeRV.setAdapter(listSessionTypesAdapter);

                //

                onFilterChangedListener.OnResetFilter();
                spinnerMax.setSelection(adapterPriceMax.getPosition(PRICES_STRINGS_SE.get(maxPrice)));
                spinnerMin.setSelection(adapterPriceMin.getPosition(PRICES_STRINGS_SE.get(-1)));

                toggleClearFilterTextColor();

            }
        });

        toggleClearFilterTextColor();

        return view;
    }

    private void toggleClearFilterTextColor() {
        if (this.minHour==minDefaultHour && minMinute==minDefaultMinute && maxHour==maxDefaultHour && maxMinute==maxDefaultMinute && this.maxPrice==PRICES_INTEGERS_SE.get("Max") && this.minPrice==0 && !this.sessionTypeChosen.containsValue(true)) {
            clearAll.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.grayTextColor)));
        } else {
            clearAll.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.foxmikePrimaryColor)));
        }
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
        void OnDistanceFilterChanged(int filterDistance);
        void OnMinPriceChanged(int minPrice);
        void OnMaxPriceChanged(int maxPrice);
        void OnTimeRangeChanged(int minHour, int minMinute, int maxHour, int maxMinute);
        void OnSessionTypeChanged(HashMap<String, Boolean> sessionTypeChosen);
        void OnFoxmikePlusOnlyChanged(boolean foxmikePlusOnly);
        void OnResetFilter();
    }

    public interface OnChosenTypesChangedListener {
        void OnChosenTypeChanged(HashMap<String, Boolean> changedSessionTypeChosen);
    }
}