package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.net.Inet4Address;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeekdayFilterFragmentB extends Fragment {


    public WeekdayFilterFragmentB() {
        // Required empty public constructor
    }

    HashMap<Integer,Boolean> toggleMap1 = new HashMap<Integer, Boolean>();
    HashMap<Integer,Boolean> toggleMap2 = new HashMap<Integer, Boolean>();
    HashMap<Integer,ToggleButton> toggleButtonHashMap;

    ToggleButton toggleButton1;
    ToggleButton toggleButton2;
    ToggleButton toggleButton3;
    ToggleButton toggleButton4;
    ToggleButton toggleButton5;
    ToggleButton toggleButton6;
    ToggleButton toggleButton7;
    public HashMap<String,Boolean> secondWeekdayHashMap;
    View inflatedView;
    private OnWeekdayChangedListener onWeekdayChangedListener;
    private OnWeekdayButtonClickedListener onWeekdayButtonClickedListener;

    public static WeekdayFilterFragmentB newInstance() {
        WeekdayFilterFragmentB fragment = new WeekdayFilterFragmentB();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        secondWeekdayHashMap = new HashMap<String,Boolean>();

        toggleButtonHashMap = new HashMap<Integer, ToggleButton>();
        Session dummySession = new Session();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar todayCal = Calendar.getInstance();
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,7);


        for(int i=1; i<8; i++){
            String stringDate = sdf.format(cal.getTime());
            secondWeekdayHashMap.put(stringDate, true);
            cal.add(Calendar.DATE,1);
        }



        this.inflatedView = inflater.inflate(R.layout.fragment_weekday_filter, container, false);

        toggleButton1 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton1);
        final Calendar calDate8= Calendar.getInstance();
        calDate8.add(Calendar.DATE,7);
        SessionDate sessionDate8 = new SessionDate(calDate8);
        toggleButton1.setText(dummySession.textDay(sessionDate8) + "\n" + Integer.toString(sessionDate8.day));
        toggleButton1.setTextOn(dummySession.textDay(sessionDate8) + "\n" + Integer.toString(sessionDate8.day));
        toggleButton1.setTextOff(dummySession.textDay(sessionDate8) + "\n" + Integer.toString(sessionDate8.day));
        toggleButton1.setChecked(true);


        toggleButton2 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton2);
        final Calendar calDate9= Calendar.getInstance();
        calDate9.add(Calendar.DATE,8);
        SessionDate sessionDate9 = new SessionDate(calDate9);
        toggleButton2.setText(dummySession.textDay(sessionDate9) + "\n" + Integer.toString(sessionDate9.day));
        toggleButton2.setTextOn(dummySession.textDay(sessionDate9) + "\n" + Integer.toString(sessionDate9.day));
        toggleButton2.setTextOff(dummySession.textDay(sessionDate9) + "\n" + Integer.toString(sessionDate9.day));
        toggleButton2.setChecked(true);

        toggleButton3 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton3);
        final Calendar calDate10= Calendar.getInstance();
        calDate10.add(Calendar.DATE,9);
        SessionDate sessionDate10 = new SessionDate(calDate10);
        toggleButton3.setText(dummySession.textDay(sessionDate10) + "\n" + Integer.toString(sessionDate10.day));
        toggleButton3.setTextOn(dummySession.textDay(sessionDate10) + "\n" + Integer.toString(sessionDate10.day));
        toggleButton3.setTextOff(dummySession.textDay(sessionDate10) + "\n" + Integer.toString(sessionDate10.day));
        toggleButton3.setChecked(true);

        toggleButton4 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton4);
        final Calendar calDate11= Calendar.getInstance();
        calDate11.add(Calendar.DATE,10);
        SessionDate sessionDate11 = new SessionDate(calDate11);
        toggleButton4.setText(dummySession.textDay(sessionDate11) + "\n" + Integer.toString(sessionDate11.day));
        toggleButton4.setTextOn(dummySession.textDay(sessionDate11) + "\n" + Integer.toString(sessionDate11.day));
        toggleButton4.setTextOff(dummySession.textDay(sessionDate11) + "\n" + Integer.toString(sessionDate11.day));
        toggleButton4.setChecked(true);

        toggleButton5 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton5);
        final Calendar calDate12= Calendar.getInstance();
        calDate12.add(Calendar.DATE,11);
        SessionDate sessionDate12 = new SessionDate(calDate12);
        toggleButton5.setText(dummySession.textDay(sessionDate12) + "\n" + Integer.toString(sessionDate12.day));
        toggleButton5.setTextOn(dummySession.textDay(sessionDate12) + "\n" + Integer.toString(sessionDate12.day));
        toggleButton5.setTextOff(dummySession.textDay(sessionDate12) + "\n" + Integer.toString(sessionDate12.day));
        toggleButton5.setChecked(true);

        toggleButton6 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton6);
        final Calendar calDate13= Calendar.getInstance();
        calDate13.add(Calendar.DATE,12);
        SessionDate sessionDate13 = new SessionDate(calDate13);
        toggleButton6.setText(dummySession.textDay(sessionDate13) + "\n" + Integer.toString(sessionDate13.day));
        toggleButton6.setTextOn(dummySession.textDay(sessionDate13) + "\n" + Integer.toString(sessionDate13.day));
        toggleButton6.setTextOff(dummySession.textDay(sessionDate13) + "\n" + Integer.toString(sessionDate13.day));
        toggleButton6.setChecked(true);

        toggleButton7 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton7);
        final Calendar calDate14= Calendar.getInstance();
        calDate14.add(Calendar.DATE,13);
        SessionDate sessionDate14 = new SessionDate(calDate14);
        toggleButton7.setText(dummySession.textDay(sessionDate14) + "\n" + Integer.toString(sessionDate14.day));
        toggleButton7.setTextOn(dummySession.textDay(sessionDate14) + "\n" + Integer.toString(sessionDate14.day));
        toggleButton7.setTextOff(dummySession.textDay(sessionDate14) + "\n" + Integer.toString(sessionDate14.day));
        toggleButton7.setChecked(true);

        toggleButtonHashMap.put(1,toggleButton1);
        toggleButtonHashMap.put(2,toggleButton2);
        toggleButtonHashMap.put(3,toggleButton3);
        toggleButtonHashMap.put(4,toggleButton4);
        toggleButtonHashMap.put(5,toggleButton5);
        toggleButtonHashMap.put(6,toggleButton6);
        toggleButtonHashMap.put(7,toggleButton7);



        toggleMap1.put(1,toggleButton1.isChecked());
        toggleMap1.put(2,toggleButton2.isChecked());
        toggleMap1.put(3,toggleButton3.isChecked());
        toggleMap1.put(4,toggleButton4.isChecked());
        toggleMap1.put(5,toggleButton5.isChecked());
        toggleMap1.put(6,toggleButton6.isChecked());
        toggleMap1.put(7,toggleButton7.isChecked());

        toggleMap2.put(1,toggleButton1.isChecked());
        toggleMap2.put(2,toggleButton2.isChecked());
        toggleMap2.put(3,toggleButton3.isChecked());
        toggleMap2.put(4,toggleButton4.isChecked());
        toggleMap2.put(5,toggleButton5.isChecked());
        toggleMap2.put(6,toggleButton6.isChecked());
        toggleMap2.put(7,toggleButton7.isChecked());

        toggleButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(2,1,toggleMap2);
            }
        });

        toggleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(2,2,toggleMap2);
            }
        });

        toggleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(2,3,toggleMap2);
            }
        });

        toggleButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(2,4,toggleMap2);
            }
        });

        toggleButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(2,5,toggleMap2);
            }
        });

        toggleButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(2,6,toggleMap2);
            }
        });

        toggleButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(2,7,toggleMap2);
            }
        });


        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate8.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate8.getTime()),false, getActivity());
                }
            }
        });

        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate9.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate9.getTime()),false, getActivity());
                }
            }
        });

        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String test = sdf.format(calDate10.getTime());
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate10.getTime()),true, getActivity());
                } else {
                    String test = sdf.format(calDate10.getTime());
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate10.getTime()),false, getActivity());
                }
            }
        });

        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate11.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate11.getTime()),false, getActivity());
                }
            }
        });

        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate12.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate12.getTime()),false, getActivity());
                }
            }
        });

        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate13.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate13.getTime()),false, getActivity());
                }
            }
        });

        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate14.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(2,sdf.format(calDate14.getTime()),false, getActivity());
                }
            }
        });

        // Inflate the layout for this fragment
        return inflatedView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWeekdayChangedListener) {
            onWeekdayChangedListener = (OnWeekdayChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
        int buttonChecked=0;
        int countChecked = 0;

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
            if (week==2) {
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

    public void setToggleMap1(HashMap<Integer,Boolean> toggleMap1) {
        this.toggleMap1=toggleMap1;
    }

    public void setToggleButton(Integer button, Boolean check) {
        toggleButtonHashMap.get(button).setChecked(check);
    }
}
