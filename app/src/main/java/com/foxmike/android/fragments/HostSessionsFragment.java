package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.foxmike.android.R;
import com.foxmike.android.adapters.SmallSessionsPagerAdapter;
import com.foxmike.android.interfaces.OnCreateSessionClickedListener;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * This fragment lists all sessions the current user is hosting
 */
public class HostSessionsFragment extends Fragment {

    public static final String TAG = HostSessionsFragment.class.getSimpleName();

    private OnCreateSessionClickedListener onCreateSessionClickedListener;
    private FloatingActionButton createSessionBtn;
    private ViewPager hostSessionsPager;
    private SmallSessionsPagerAdapter hostSessionsPagerAdapter;
    private TabLayout tabLayout;
    private long mLastClickTime = 0;
    private HashMap<String, String> sessionTypeDictionary;

    public HostSessionsFragment() {
        // Required empty public constructor
    }

    public static HostSessionsFragment newInstance(HashMap<String,String> sessionTypeDictionary) {
        HostSessionsFragment fragment = new HostSessionsFragment();
        Bundle args = new Bundle();
        args.putSerializable("sessionTypeDictionary", sessionTypeDictionary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionTypeDictionary = (HashMap<String, String>)getArguments().getSerializable("sessionTypeDictionary");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Get the view fragment_user_account */
        View view = inflater.inflate(R.layout.fragment_host_sessions, container, false);
        hostSessionsPager = (ViewPager) view.findViewById(R.id.host_sessions_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.host_sessions_tabs);
        // Setup create session button
        createSessionBtn = view.findViewById(R.id.add_session_btn);

        FirebaseDatabaseViewModel stripeDepositionPaymentIntentIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> stripeDepositionPaymentIntentIdLiveData = stripeDepositionPaymentIntentIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeDepositionPaymentIntentId"));
        stripeDepositionPaymentIntentIdLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                createSessionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
                        rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stripeAccountId").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean userHasStripeAccount;
                                userHasStripeAccount = dataSnapshot.getValue() != null;
                                if (userHasStripeAccount) {
                                    onCreateSessionClickedListener.OnCreateSessionClicked();
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.you_have_no_stripe_account, Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });

            }
        });







        hostSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), true, getString(R.string.sessions), getString(R.string.avertisements), sessionTypeDictionary);
        hostSessionsPager.setAdapter(hostSessionsPagerAdapter);
        tabLayout.setupWithViewPager(hostSessionsPager);


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (hostSessionsPager!=null) {
            hostSessionsPager.setAdapter(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hostSessionsPagerAdapter!=null) {
            hostSessionsPagerAdapter=null;
        }
    }

    // Function which load the tab layout and viewpager
    public void loadPages(boolean update) {
        // If this function was initiated through an update update the fragments/pages otherwise build them from scratch
        if (!update) {
            hostSessionsPagerAdapter = new SmallSessionsPagerAdapter(getChildFragmentManager(), true, getString(R.string.sessions), getString(R.string.avertisements), sessionTypeDictionary);
            hostSessionsPager.setAdapter(hostSessionsPagerAdapter);
            tabLayout.setupWithViewPager(hostSessionsPager);
        } else {
            hostSessionsPagerAdapter.notifyDataSetChanged();
        }
    }

    public void setPage(int page) {
        if (hostSessionsPager!=null) {
            hostSessionsPager.setCurrentItem(page);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateSessionClickedListener) {
            onCreateSessionClickedListener = (OnCreateSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onCreateSessionClickedListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        onCreateSessionClickedListener = null;
    }
}