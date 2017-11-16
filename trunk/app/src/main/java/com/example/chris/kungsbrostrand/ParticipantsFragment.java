package com.example.chris.kungsbrostrand;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class ParticipantsFragment extends DialogFragment {

    private HashMap<String,Boolean> participants;
    private LinearLayout list1;



    public ParticipantsFragment() {
        // Required empty public constructor
    }

    public static ParticipantsFragment newInstance(HashMap<String,Boolean> participants) {
        ParticipantsFragment fragment = new ParticipantsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hashmap",participants);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullscreenDialog);

        this.participants = new HashMap<String,Boolean>();
        Bundle b = this.getArguments();
        if(b.getSerializable("hashmap") != null) {
            this.participants = (HashMap<String,Boolean>)b.getSerializable("hashmap");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d!=null){
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participants, container, false);

        list1 = view.findViewById(R.id.list_of_participants);
        View list1HeadingView = inflater.inflate(R.layout.your_sessions_heading,list1,false);
        TextView list1Heading = list1HeadingView.findViewById(R.id.yourSessionsHeadingTV);
        list1Heading.setText("Participants");
        list1.addView(list1HeadingView);

        ParticipantsRow participantsRow = new ParticipantsRow();
        participantsRow.populateList(this.participants, getActivity(),list1);

        return view;
    }

}
