package com.example.chris.kungsbrostrand;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class SessionContainerFragment extends Fragment {
    private static final String SESSION_FRAGMENT_TYPE = "param1";
    ListSessionsFragment listSessionsFragment;
    MapsFragment mapsFragment;

    public SessionContainerFragment() {
        // Required empty public constructor
    }

    public static SessionContainerFragment newInstance(String sessionFragmentType) {
        SessionContainerFragment fragment = new SessionContainerFragment();
        Bundle args = new Bundle();
        args.putString(SESSION_FRAGMENT_TYPE, sessionFragmentType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_container, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        insertNestedFragment();
    }

    private void insertNestedFragment() {
        Bundle args = getArguments();
        String session_fragment_type = args.getString("param1");

        if (listSessionsFragment==null) {
            listSessionsFragment = ListSessionsFragment.newInstance();
        }

        if (mapsFragment==null) {
            mapsFragment = MapsFragment.newInstance();
        }
    }

    public void chooseSessionFragmentType(String sessionFragmentType) {

        FragmentManager fragMgr = getFragmentManager();
        FragmentTransaction xact = fragMgr.beginTransaction();

        if (sessionFragmentType.equals("ListSessionsFragment")) {
            if (null == fragMgr.findFragmentByTag("weekdayFragment")) {
                xact.add(R.id.weekdayFilterFragmentContainer, WeekdayFilterFragment.newInstance(),"weekdayFragment");
            } else {
                xact.show(fragMgr.findFragmentByTag("weekdayFragment"));
            }
            if (null == fragMgr.findFragmentByTag("mapsFragment")) {
                xact.add(R.id.sessionContainer, mapsFragment,"mapsFragment");
                xact.detach(mapsFragment);
            } else {
                xact.detach(mapsFragment);
            }
            if (null == fragMgr.findFragmentByTag("ListSessionsFragment")) {
                xact.add(R.id.sessionContainer, listSessionsFragment,"ListSessionsFragment");
            } else {
                xact.attach(listSessionsFragment);
            }

        }

        if (sessionFragmentType.equals("mapsFragment")) {
            if (null == fragMgr.findFragmentByTag("weekdayFragment")) {
                xact.add(R.id.weekdayFilterFragmentContainer, WeekdayFilterFragment.newInstance(),"weekdayFragment");
            } else {
                xact.show(fragMgr.findFragmentByTag("weekdayFragment"));
            }
            if (null == fragMgr.findFragmentByTag("ListSessionsFragment")) {
                xact.add(R.id.sessionContainer, listSessionsFragment,"ListSessionsFragment");
                xact.detach(listSessionsFragment);
            } else {
                xact.detach(listSessionsFragment);
            }
            if (null == fragMgr.findFragmentByTag("mapsFragment")) {
                xact.add(R.id.sessionContainer, mapsFragment,"mapsFragment");
            } else {
                xact.attach(mapsFragment);
            }
        }

        xact.commit();
    }

}
