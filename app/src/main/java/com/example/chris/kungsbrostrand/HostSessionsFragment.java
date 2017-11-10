package com.example.chris.kungsbrostrand;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 *
 */
public class HostSessionsFragment extends Fragment {

    private LinearLayout listHostSessions;
    private LatLng sessionLatLng;

    public HostSessionsFragment() {
        // Required empty public constructor
    }

    public static HostSessionsFragment newInstance() {
        HostSessionsFragment fragment = new HostSessionsFragment();
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
        /* Get the view fragment_user_profile */
        final View view = inflater.inflate(R.layout.fragment_host_sessions, container, false);

        listHostSessions = view.findViewById(R.id.list_host_sessions);
        View sessionsAttendingHeadingView = inflater.inflate(R.layout.your_sessions_heading,listHostSessions,false);
        TextView sessionsAttendingHeading = sessionsAttendingHeadingView.findViewById(R.id.yourSessionsHeadingTV);
        sessionsAttendingHeading.setText("Sessions Hosting");
        listHostSessions.addView(sessionsAttendingHeadingView);

        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        /* Get the currents user's information from the database */
        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not attending any sessions set that the sessionsAttending content has beeen found*/
                if (user.sessionsAttending.size()==0){

                }
                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        SessionRow sessionRow = new SessionRow();
                        sessionRow.populateList(sessions, getActivity(),listHostSessions);
                    }
                },user.sessionsAttending);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
