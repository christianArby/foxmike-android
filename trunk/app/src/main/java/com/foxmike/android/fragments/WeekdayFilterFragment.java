package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnWeekdayButtonClickedListener;
import com.foxmike.android.interfaces.OnWeekdayChangedListener;
import com.foxmike.android.utils.CustomToggleButton;
import com.foxmike.android.utils.TextTimestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
/**
 * This sets up 7 tooglebuttons with weekdays which listen for user clicks and sends the output
 * through OnWeekdayChangedListener and OnWeekdayButtonClickedListener
 */
public class WeekdayFilterFragment extends Fragment{

    public WeekdayFilterFragment() {
        // Required empty public constructor
    }

    private int weekFilter;
    private HashMap<Integer,Boolean> toggleMap1 = new HashMap<Integer, Boolean>();
    private HashMap<Integer,Boolean> toggleMap2 = new HashMap<Integer, Boolean>();
    private HashMap<Integer,ToggleButton> toggleButtonHashMap;
    private CustomToggleButton toggleButton1;
    private CustomToggleButton toggleButton2;
    private CustomToggleButton toggleButton3;
    private CustomToggleButton toggleButton4;
    private CustomToggleButton toggleButton5;
    private CustomToggleButton toggleButton6;
    private CustomToggleButton toggleButton7;

    private OnWeekdayChangedListener onWeekdayChangedListener;
    private OnWeekdayButtonClickedListener onWeekdayButtonClickedListener;

    public static WeekdayFilterFragment newInstance(int weekFilter) {
        WeekdayFilterFragment fragment = new WeekdayFilterFragment();
        Bundle args = new Bundle();
        args.putInt("weekFilter", weekFilter);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        weekFilter = getArguments().getInt("weekFilter",0);

        toggleButtonHashMap = new HashMap<Integer, ToggleButton>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        View inflatedView = inflater.inflate(R.layout.fragment_weekday_filter, container, false);

        // Bind UI components
        toggleButton1 = inflatedView.findViewById(R.id.toggleButton1);
        toggleButton2 = inflatedView.findViewById(R.id.toggleButton2);
        toggleButton3 = inflatedView.findViewById(R.id.toggleButton3);
        toggleButton4 = inflatedView.findViewById(R.id.toggleButton4);
        toggleButton5 = inflatedView.findViewById(R.id.toggleButton5);
        toggleButton6 = inflatedView.findViewById(R.id.toggleButton6);
        toggleButton7 = inflatedView.findViewById(R.id.toggleButton7);

        int n=0;
        if (weekFilter==1) {
            n=0;
        } else {
            n=7;
        }

        // Create date texts
        final Calendar calDate1= Calendar.getInstance();
        calDate1.add(Calendar.DATE,n);
        final Calendar calDate2= Calendar.getInstance();
        calDate2.add(Calendar.DATE,n+1);
        final Calendar calDate3= Calendar.getInstance();
        calDate3.add(Calendar.DATE,n+2);
        final Calendar calDate4= Calendar.getInstance();
        calDate4.add(Calendar.DATE,n+3);
        final Calendar calDate5= Calendar.getInstance();
        calDate5.add(Calendar.DATE,n+4);
        final Calendar calDate6= Calendar.getInstance();
        calDate6.add(Calendar.DATE,n+5);
        final Calendar calDate7= Calendar.getInstance();
        calDate7.add(Calendar.DATE,n+6);
        TextTimestamp sessionDate1 = new TextTimestamp(calDate1.getTimeInMillis());
        TextTimestamp sessionDate2 = new TextTimestamp(calDate2.getTimeInMillis());
        TextTimestamp sessionDate3 = new TextTimestamp(calDate3.getTimeInMillis());
        TextTimestamp sessionDate4 = new TextTimestamp(calDate4.getTimeInMillis());
        TextTimestamp sessionDate5 = new TextTimestamp(calDate5.getTimeInMillis());
        TextTimestamp sessionDate6 = new TextTimestamp(calDate6.getTimeInMillis());
        TextTimestamp sessionDate7 = new TextTimestamp(calDate7.getTimeInMillis());

        // Set togglebuttons to date texts
        if (weekFilter==1) {
            toggleButton1.setText(getResources().getString(R.string.today_filter_text));
            toggleButton1.setTextOn(getResources().getString(R.string.today_filter_text));
            toggleButton1.setTextOff(getResources().getString(R.string.today_filter_text));
            toggleButton1.setChecked(true);
        } else {
            setupToggleButton(toggleButton1,sessionDate1);
        }
        setupToggleButton(toggleButton2,sessionDate2);
        setupToggleButton(toggleButton3,sessionDate3);
        setupToggleButton(toggleButton4,sessionDate4);
        setupToggleButton(toggleButton5,sessionDate5);
        setupToggleButton(toggleButton6,sessionDate6);
        setupToggleButton(toggleButton7,sessionDate7);

        // Place togglebuttons in hashmap
        toggleButtonHashMap.put(1,toggleButton1);
        toggleButtonHashMap.put(2,toggleButton2);
        toggleButtonHashMap.put(3,toggleButton3);
        toggleButtonHashMap.put(4,toggleButton4);
        toggleButtonHashMap.put(5,toggleButton5);
        toggleButtonHashMap.put(6,toggleButton6);
        toggleButtonHashMap.put(7,toggleButton7);

        // Place week 1 togglebuttons booleans in Hashmap HashMap<Integer,Boolean>
        toggleMap1.put(1,toggleButton1.isChecked());
        toggleMap1.put(2,toggleButton2.isChecked());
        toggleMap1.put(3,toggleButton3.isChecked());
        toggleMap1.put(4,toggleButton4.isChecked());
        toggleMap1.put(5,toggleButton5.isChecked());
        toggleMap1.put(6,toggleButton6.isChecked());
        toggleMap1.put(7,toggleButton7.isChecked());

        // Assume week 2 togglebuttons to be booleans in Hashmap HashMap<Integer,Boolean>
        toggleMap2.put(1,toggleButton1.isChecked());
        toggleMap2.put(2,toggleButton2.isChecked());
        toggleMap2.put(3,toggleButton3.isChecked());
        toggleMap2.put(4,toggleButton4.isChecked());
        toggleMap2.put(5,toggleButton5.isChecked());
        toggleMap2.put(6,toggleButton6.isChecked());
        toggleMap2.put(7,toggleButton7.isChecked());

        // Set on click listeners to togglebuttons and call listener and send week, button pressed and Integer, Boolean hashmap
        toggleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(weekFilter,1,toggleMap1);
            }
        });

        toggleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(weekFilter,2,toggleMap1);
            }
        });

        toggleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(weekFilter,3,toggleMap1);
            }
        });

        toggleButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(weekFilter,4,toggleMap1);
            }
        });

        toggleButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(weekFilter,5,toggleMap1);
            }
        });

        toggleButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(weekFilter,6,toggleMap1);
            }
        });

        toggleButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(weekFilter,7,toggleMap1);
            }
        });


        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate1.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate1.getTime()),false, getActivity());
                }
            }
        });

        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate2.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate2.getTime()),false, getActivity());
                }
            }
        });

        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate3.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate3.getTime()),false, getActivity());
                }
            }
        });

        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate4.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate4.getTime()),false, getActivity());
                }
            }
        });

        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate5.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate5.getTime()),false, getActivity());
                }
            }
        });

        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate6.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate6.getTime()),false, getActivity());
                }
            }
        });

        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate7.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(weekFilter,sdf.format(calDate7.getTime()),false, getActivity());
                }
            }
        });

        // Inflate the layout for this fragment
        return inflatedView;

    }

    private void setupToggleButton(ToggleButton toggleButton,TextTimestamp textTimestamp) {
        toggleButton.setText(textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay());
        toggleButton.setTextOn(textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay());
        toggleButton.setTextOff(textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay());
        toggleButton.setChecked(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWeekdayChangedListener) {
            onWeekdayChangedListener = (OnWeekdayChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWeekdayChangedListener");
        }

        if (context instanceof OnWeekdayButtonClickedListener) {
            onWeekdayButtonClickedListener = (OnWeekdayButtonClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWeekdayButtonClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onWeekdayChangedListener = null;
        onWeekdayButtonClickedListener = null;
    }

    public void changeToggleMap(int week, int button, HashMap<Integer,Boolean> toggleMap1, HashMap<Integer,Boolean> toggleMap2) {
        this.toggleMap1 = toggleMap1;
        this.toggleMap2 = toggleMap2;

        Boolean allChecked= true;
        for (Boolean toggle : toggleMap1.values()) {
            if (!toggle) {
                allChecked = false;
            }
        }
        for (Boolean toggle : toggleMap2.values()) {
            if (!toggle) {
                allChecked = false;
            }
        }

        Boolean allUnChecked= false;
        int countChecked = 0;
        int buttonChecked=0;
        for (Integer key : toggleMap1.keySet()) {
            if (toggleMap1.get(key)) {
                countChecked++;
                buttonChecked=key;
            }
        }

        for (Integer key : toggleMap2.keySet()) {
            if (toggleMap2.get(key)) {
                countChecked++;
                buttonChecked=key+7;
            }
        }

        if (countChecked<2) {
            allUnChecked=true;
        }


        if (allChecked) {
            toggleButton1.setChecked(false);
            toggleButton2.setChecked(false);
            toggleButton3.setChecked(false);
            toggleButton4.setChecked(false);
            toggleButton5.setChecked(false);
            toggleButton6.setChecked(false);
            toggleButton7.setChecked(false);
            if (week==1 && weekFilter==1 && button!=0) {
                toggleButtonHashMap.get(button).setChecked(true);
            }
            if (week==2 && weekFilter==2 && button!=0) {
                toggleButtonHashMap.get(button).setChecked(true);
            }
        }

        int buttonClicked=0;
        if (week==1) {
            buttonClicked=button;
        }
        if (week==2) {
            buttonClicked=button+7;
        }


        if (allUnChecked && buttonChecked==buttonClicked) {
            toggleButton1.setChecked(true);
            toggleButton2.setChecked(true);
            toggleButton3.setChecked(true);
            toggleButton4.setChecked(true);
            toggleButton5.setChecked(true);
            toggleButton6.setChecked(true);
            toggleButton7.setChecked(true);
        }

        if (button==0) {
            toggleButton1.setChecked(true);
            toggleButton2.setChecked(true);
            toggleButton3.setChecked(true);
            toggleButton4.setChecked(true);
            toggleButton5.setChecked(true);
            toggleButton6.setChecked(true);
            toggleButton7.setChecked(true);
        }
    }

    public HashMap<Integer,Boolean> getAndUpdateToggleMap1() {
        for (Integer key : this.toggleMap2.keySet()) {
            this.toggleMap1.put(key,this.toggleButtonHashMap.get(key).isChecked());
        }
        return this.toggleMap1;
    }

    public HashMap<Integer,Boolean> getToggleMap1() {
        return this.toggleMap1;
    }

    public void setToggleMap1(HashMap<Integer,Boolean> toggleMap1) {
        this.toggleMap1=toggleMap1;
    }


    public HashMap<Integer,Boolean> getAndUpdateToggleMap2() {
        for (Integer key : this.toggleMap2.keySet()) {
            this.toggleMap2.put(key,this.toggleButtonHashMap.get(key).isChecked());
        }
        return this.toggleMap2;
    }

    public HashMap<Integer,Boolean> getToggleMap2() {
        return this.toggleMap2;
    }

    public void setToggleMap2(HashMap<Integer,Boolean> toggleMap2) {
        this.toggleMap2=toggleMap2;
    }

    public void setToggleButton(Integer button, Boolean check) {
        toggleButtonHashMap.get(button).setChecked(check);
    }
}
