package com.example.chris.kungsbrostrand;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeekdayFilterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeekdayFilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeekdayFilterFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public ListSessionsActivity listSessionsActivity;


    ToggleButton toggleButton1;
    ToggleButton toggleButton2;
    ToggleButton toggleButton3;
    ToggleButton toggleButton4;
    ToggleButton toggleButton5;
    ToggleButton toggleButton6;
    ToggleButton toggleButton7;

    View inflatedView;

    // TODO: Rename and change types of parameters


    private OnFragmentInteractionListener mListener;

    public WeekdayFilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WeekdayFilterFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        this.inflatedView = inflater.inflate(R.layout.fragment_weekday_filter, container, false);

        listSessionsActivity = (ListSessionsActivity) getActivity();



        Session dummySession = new Session();
        Calendar cal = Calendar.getInstance();

        SessionDate todaysSessionDate = new SessionDate(cal);



        toggleButton1 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton1);
        toggleButton1.setText(dummySession.textDay(todaysSessionDate));
        toggleButton1.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton1.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton1.setChecked(true);
        toggleButton2 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton2);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton2.setText(dummySession.textDay(todaysSessionDate));
        toggleButton2.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton2.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton2.setChecked(true);
        toggleButton3 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton3);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton3.setText(dummySession.textDay(todaysSessionDate));
        toggleButton3.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton3.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton3.setChecked(true);
        toggleButton4 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton4);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton4.setText(dummySession.textDay(todaysSessionDate));
        toggleButton4.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton4.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton4.setChecked(true);
        toggleButton5 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton5);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton5.setText(dummySession.textDay(todaysSessionDate));
        toggleButton5.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton5.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton5.setChecked(true);
        toggleButton6 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton6);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton6.setText(dummySession.textDay(todaysSessionDate));
        toggleButton6.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton6.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton6.setChecked(true);
        toggleButton7 = (ToggleButton) inflatedView.findViewById(R.id.toggleButton7);
        todaysSessionDate.day = todaysSessionDate.day +1;
        toggleButton7.setText(dummySession.textDay(todaysSessionDate));
        toggleButton7.setTextOn(dummySession.textDay(todaysSessionDate));
        toggleButton7.setTextOff(dummySession.textDay(todaysSessionDate));
        toggleButton7.setChecked(true);

        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listSessionsActivity.weekdayHashMap.put(toggleButton1.getText().toString(),true);
                } else {
                    listSessionsActivity.weekdayHashMap.put(toggleButton1.getText().toString(),false);
                }
                listSessionsActivity.filterSessions();
            }
        });

        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listSessionsActivity.weekdayHashMap.put(toggleButton2.getText().toString(),true);
                } else {
                    listSessionsActivity.weekdayHashMap.put(toggleButton2.getText().toString(),false);
                }
                listSessionsActivity.filterSessions();
            }
        });

        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listSessionsActivity.weekdayHashMap.put(toggleButton3.getText().toString(),true);
                } else {
                    listSessionsActivity.weekdayHashMap.put(toggleButton3.getText().toString(),false);
                }
                listSessionsActivity.filterSessions();
            }
        });

        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listSessionsActivity.weekdayHashMap.put(toggleButton4.getText().toString(),true);
                } else {
                    listSessionsActivity.weekdayHashMap.put(toggleButton4.getText().toString(),false);
                }
                listSessionsActivity.filterSessions();
            }
        });

        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listSessionsActivity.weekdayHashMap.put(toggleButton5.getText().toString(),true);
                } else {
                    listSessionsActivity.weekdayHashMap.put(toggleButton5.getText().toString(),false);
                }
                listSessionsActivity.filterSessions();
            }
        });

        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listSessionsActivity.weekdayHashMap.put(toggleButton6.getText().toString(),true);
                } else {
                    listSessionsActivity.weekdayHashMap.put(toggleButton6.getText().toString(),false);
                }
                listSessionsActivity.filterSessions();
            }
        });

        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listSessionsActivity.weekdayHashMap.put(toggleButton7.getText().toString(),true);
                } else {
                    listSessionsActivity.weekdayHashMap.put(toggleButton7.getText().toString(),false);
                }
                listSessionsActivity.filterSessions();
            }
        });





        // Inflate the layout for this fragment
        return inflatedView;




    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
