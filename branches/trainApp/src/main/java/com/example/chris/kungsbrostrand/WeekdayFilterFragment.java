package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;

public class WeekdayFilterFragment extends Fragment{

    public WeekdayFilterFragment() {
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
    public HashMap<String,Boolean> firstWeekdayHashMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    GeoFire geoFire;
    TreeMap<Integer,String> nearSessions;
    View inflatedView;
    private OnWeekdayChangedListener onWeekdayChangedListener;
    private OnWeekdayButtonClickedListener onWeekdayButtonClickedListener;

    public static WeekdayFilterFragment newInstance() {
        WeekdayFilterFragment fragment = new WeekdayFilterFragment();
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

        firstWeekdayHashMap = new HashMap<String,Boolean>();

        toggleButtonHashMap = new HashMap<Integer, ToggleButton>();
        Session dummySession = new Session();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar todayCal = Calendar.getInstance();
        final Calendar cal = Calendar.getInstance();


        for(int i=1; i<8; i++){
            String stringDate = sdf.format(cal.getTime());
            firstWeekdayHashMap.put(stringDate, true);
            cal.add(Calendar.DATE,1);
        }

        this.inflatedView = inflater.inflate(R.layout.fragment_weekday_filter, container, false);

        toggleButton1 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton1);
        final Calendar calDate1= Calendar.getInstance();

        SessionDate sessionDate1 = new SessionDate(calDate1);
        toggleButton1.setText(dummySession.textDay(sessionDate1) + "\n" + Integer.toString(sessionDate1.day));
        toggleButton1.setTextOn(dummySession.textDay(sessionDate1) + "\n" + Integer.toString(sessionDate1.day));
        toggleButton1.setTextOff(dummySession.textDay(sessionDate1) + "\n" + Integer.toString(sessionDate1.day));
        toggleButton1.setChecked(true);


        toggleButton2 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton2);
        final Calendar calDate2= Calendar.getInstance();
        calDate2.add(Calendar.DATE,1);
        SessionDate sessionDate2 = new SessionDate(calDate2);
        toggleButton2.setText(dummySession.textDay(sessionDate2) + "\n" + Integer.toString(sessionDate2.day));
        toggleButton2.setTextOn(dummySession.textDay(sessionDate2) + "\n" + Integer.toString(sessionDate2.day));
        toggleButton2.setTextOff(dummySession.textDay(sessionDate2) + "\n" + Integer.toString(sessionDate2.day));
        toggleButton2.setChecked(true);

        toggleButton3 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton3);
        final Calendar calDate3= Calendar.getInstance();
        calDate3.add(Calendar.DATE,2);
        SessionDate sessionDate3 = new SessionDate(calDate3);
        toggleButton3.setText(dummySession.textDay(sessionDate3) + "\n" + Integer.toString(sessionDate3.day));
        toggleButton3.setTextOn(dummySession.textDay(sessionDate3) + "\n" + Integer.toString(sessionDate3.day));
        toggleButton3.setTextOff(dummySession.textDay(sessionDate3) + "\n" + Integer.toString(sessionDate3.day));
        toggleButton3.setChecked(true);

        toggleButton4 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton4);
        final Calendar calDate4= Calendar.getInstance();
        calDate4.add(Calendar.DATE,3);
        SessionDate sessionDate4 = new SessionDate(calDate4);
        toggleButton4.setText(dummySession.textDay(sessionDate4) + "\n" + Integer.toString(sessionDate4.day));
        toggleButton4.setTextOn(dummySession.textDay(sessionDate4) + "\n" + Integer.toString(sessionDate4.day));
        toggleButton4.setTextOff(dummySession.textDay(sessionDate4) + "\n" + Integer.toString(sessionDate4.day));
        toggleButton4.setChecked(true);

        toggleButton5 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton5);
        final Calendar calDate5= Calendar.getInstance();
        calDate5.add(Calendar.DATE,4);
        SessionDate sessionDate5 = new SessionDate(calDate5);
        toggleButton5.setText(dummySession.textDay(sessionDate5) + "\n" + Integer.toString(sessionDate5.day));
        toggleButton5.setTextOn(dummySession.textDay(sessionDate5) + "\n" + Integer.toString(sessionDate5.day));
        toggleButton5.setTextOff(dummySession.textDay(sessionDate5) + "\n" + Integer.toString(sessionDate5.day));
        toggleButton5.setChecked(true);

        toggleButton6 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton6);
        final Calendar calDate6= Calendar.getInstance();
        calDate6.add(Calendar.DATE,5);
        SessionDate sessionDate6 = new SessionDate(calDate6);
        toggleButton6.setText(dummySession.textDay(sessionDate6) + "\n" + Integer.toString(sessionDate6.day));
        toggleButton6.setTextOn(dummySession.textDay(sessionDate6) + "\n" + Integer.toString(sessionDate6.day));
        toggleButton6.setTextOff(dummySession.textDay(sessionDate6) + "\n" + Integer.toString(sessionDate6.day));
        toggleButton6.setChecked(true);

        toggleButton7 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton7);
        final Calendar calDate7= Calendar.getInstance();
        calDate7.add(Calendar.DATE,6);
        SessionDate sessionDate7 = new SessionDate(calDate7);
        toggleButton7.setText(dummySession.textDay(sessionDate7) + "\n" + Integer.toString(sessionDate7.day));
        toggleButton7.setTextOn(dummySession.textDay(sessionDate7) + "\n" + Integer.toString(sessionDate7.day));
        toggleButton7.setTextOff(dummySession.textDay(sessionDate7) + "\n" + Integer.toString(sessionDate7.day));
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
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(1,1,toggleMap1);
            }
        });

        toggleButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(1,2,toggleMap1);
            }
        });

        toggleButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(1,3,toggleMap1);
            }
        });

        toggleButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(1,4,toggleMap1);
            }
        });

        toggleButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(1,5,toggleMap1);
            }
        });

        toggleButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(1,6,toggleMap1);
            }
        });

        toggleButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeekdayButtonClickedListener.OnWeekdayButtonClicked(1,7,toggleMap1);
            }
        });





        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate1.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate1.getTime()),false, getActivity());
                }
            }
        });

        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate2.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate2.getTime()),false, getActivity());
                }
            }
        });

        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate3.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate3.getTime()),false, getActivity());
                }
            }
        });

        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate4.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate4.getTime()),false, getActivity());
                }
            }
        });

        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate5.getTime()),true, getActivity());
                } else {
                    String test = sdf.format(calDate5.getTime());
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate5.getTime()),false, getActivity());
                }
            }
        });

        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate6.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate6.getTime()),false, getActivity());
                }
            }
        });

        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate7.getTime()),true, getActivity());
                } else {
                    onWeekdayChangedListener.OnWeekdayChanged(1,sdf.format(calDate7.getTime()),false, getActivity());
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
            if (week==1) {
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

    public HashMap<Integer,Boolean> getAndUpdateToggleMap1() {
        for (Integer key : this.toggleMap2.keySet()) {
            this.toggleMap1.put(key,this.toggleButtonHashMap.get(key).isChecked());
        }
        return this.toggleMap1;
    }

    public HashMap<Integer,Boolean> getToggleMap1() {
        return this.toggleMap1;
    }

    public void setToggleMap2(HashMap<Integer,Boolean> toggleMap2) {
        this.toggleMap2=toggleMap2;
    }

    public void setToggleButton(Integer button, Boolean check) {
        toggleButtonHashMap.get(button).setChecked(check);
    }
}
