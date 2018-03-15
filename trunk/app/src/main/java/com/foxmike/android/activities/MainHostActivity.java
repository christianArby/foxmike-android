package com.foxmike.android.activities;
//Checked
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.foxmike.android.R;
import com.foxmike.android.fragments.AllUsersFragment;
import com.foxmike.android.fragments.CreateOrEditSessionFragment;
import com.foxmike.android.fragments.DisplaySessionFragment;
import com.foxmike.android.interfaces.OnHostSessionChangedListener;
import com.foxmike.android.interfaces.OnNewMessageListener;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.fragments.HostSessionsFragment;
import com.foxmike.android.fragments.InboxFragment;
import com.foxmike.android.fragments.MapsFragment;
import com.foxmike.android.fragments.UserAccountFragment;
import com.foxmike.android.fragments.UserProfileFragment;
import com.foxmike.android.fragments.UserProfilePublicEditFragment;
import com.foxmike.android.fragments.UserProfilePublicFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainHostActivity extends AppCompatActivity implements
        OnSessionClickedListener,
        UserAccountFragment.OnUserAccountFragmentInteractionListener,
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener,
        HostSessionsFragment.OnCreateSessionClickedListener,
        OnUserClickedListener,
        OnNewMessageListener,
        DisplaySessionFragment.OnEditSessionListener,
        DisplaySessionFragment.OnBookSessionListener,
        DisplaySessionFragment.OnCancelBookedSessionListener,
        OnHostSessionChangedListener{

    private FragmentManager fragmentManager;
    private UserAccountFragment hostUserAccountFragment;
    private MapsFragment hostMapsFragment;
    private DisplaySessionFragment hostDisplaySessionFragment;
    private InboxFragment hostInboxFragment;
    private HostSessionsFragment hostSessionsFragment;
    private UserProfileFragment hostUserProfileFragment;
    private UserProfilePublicFragment hostUserProfilePublicFragment;
    private UserProfilePublicEditFragment hostUserProfilePublicEditFragment;
    private CreateOrEditSessionFragment createOrEditSessionFragment;
    private AllUsersFragment hostAllUsersFragment;
    private AHBottomNavigation bottomNavigation;
    private DatabaseReference userDbRef;
    private FirebaseAuth mAuth;
    private DatabaseReference rootDbRef;
    private HashMap<DatabaseReference, ValueEventListener> listenerMap = new HashMap<DatabaseReference, ValueEventListener>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_host);

        bottomNavigation = findViewById(R.id.bottom_navigation_host);

        mAuth = FirebaseAuth.getInstance();
        userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        fragmentManager = getSupportFragmentManager();

        // Setup bottom navigation
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_host_items);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
        bottomNavigation.setAnimation(null);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setAccentColor(getResources().getColor(R.color.secondaryColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.primaryLightColor));

        // Create instances of fragments
        hostUserAccountFragment = UserAccountFragment.newInstance();
        hostSessionsFragment = HostSessionsFragment.newInstance();
        hostInboxFragment = InboxFragment.newInstance();
        hostAllUsersFragment = AllUsersFragment.newInstance();

        // Add fragments to container and hide them
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (null == fragmentManager.findFragmentByTag("xMainHostUserAccountFragment")) {
            transaction.add(R.id.container_main_host, hostUserAccountFragment,"xMainHostUserAccountFragment");
            transaction.hide(hostUserAccountFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainHostSessionsFragment")) {
            transaction.add(R.id.container_main_host, hostSessionsFragment,"xMainHostSessionsFragment");
            transaction.hide(hostSessionsFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainHostInboxFragment")) {
            transaction.add(R.id.container_main_host, hostInboxFragment,"xMainHostInboxFragment");
            transaction.hide(hostInboxFragment);
        }
        if (null == fragmentManager.findFragmentByTag("xMainHostAllUsersFragment")) {
            transaction.add(R.id.container_main_host, hostAllUsersFragment,"xMainHostAllUsersFragment");
            transaction.hide(hostAllUsersFragment);
        }

        transaction.commit();
        fragmentManager.executePendingTransactions();

        // Listen to bottom navigation and switch to corresponding fragment
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(hostInboxFragment);
                            return true;
                        }
                    case 1:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(hostSessionsFragment);
                            return true;
                        }
                    case 2:
                        if (!wasSelected) {
                            cleanMainActivityAndSwitch(hostUserAccountFragment);
                            return true;
                        }
                }
                return false;
            }
        });

        // Set 'start page' / default fragment
        cleanMainActivityAndSwitch(hostAllUsersFragment);

        // Add listener to current user's chats to see if there are any chats the user has unread messages in, if so set notification to bottom bar
        ValueEventListener chatsListener = rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren()) {
                    int nrOfUnreadChats = 0;
                    for (DataSnapshot chatID: dataSnapshot.getChildren()) {
                        Boolean read = (Boolean) chatID.getValue();
                        if (!read) {
                            nrOfUnreadChats++;
                        }
                        if (nrOfUnreadChats>0) {
                            bottomNavigation.setNotification(Integer.toString(nrOfUnreadChats),0);
                        } else {
                            bottomNavigation.setNotification("",0);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        listenerMap.put(rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).child("chats"), chatsListener);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (hostUserAccountFragment.isVisible() | hostSessionsFragment.isVisible() | hostInboxFragment.isVisible() | hostAllUsersFragment.isVisible()){
                    bottomNavigation.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /* Method to hide all fragments in main container except fragment passed as argument */
    private void cleanMainActivityAndSwitch(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment frag:fragmentList) {
            if (frag.getTag()!=null) {
                if (frag.getTag().substring(0,5).equals("xMain")) {
                    if (frag.isVisible()) {
                        transaction.hide(frag);
                    }
                }
            }

        }
        transaction.show(fragment);
        transaction.commit();
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    /* Method to hide all fragments in main container and fill the other container with fullscreen fragment */
    private void cleanMainFullscreenActivityAndSwitch(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment frag:fragmentList) {
            if (frag.getTag()!=null) {
                if (frag.getTag().substring(0,5).equals("xMain")) {
                    if (frag.isVisible()) {
                        transaction.hide(frag);
                    }
                }
            }
        }
        bottomNavigation.setVisibility(View.GONE);
        if (addToBackStack) {
            transaction.replace(R.id.container_fullscreen_main_host, fragment).addToBackStack(null).commit();
        } else {
            transaction.replace(R.id.container_fullscreen_main_host, fragment).commit();
        }
    }

    /* Listener, when session is clicked display session*/
    @Override
    public void OnSessionClicked(double sessionLatitude, double sessionLongitude) {
        hostDisplaySessionFragment = DisplaySessionFragment.newInstance(sessionLatitude,sessionLongitude);
        cleanMainFullscreenActivityAndSwitch(hostDisplaySessionFragment, true);
    }
    /* Listener, when edit "button" in account is clicked show user profile */
    @Override
    public void OnUserAccountFragmentInteraction(String type) {
        if (type.equals("edit")) {
            hostUserProfileFragment = UserProfileFragment.newInstance();
            cleanMainFullscreenActivityAndSwitch(hostUserProfileFragment,true);
        }
    }

    /* Listener, when edit "button" in user profile is clicked show edit user profile */
    @Override
    public void onUserProfileFragmentInteraction() {
        hostUserProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        cleanMainFullscreenActivityAndSwitch(hostUserProfilePublicEditFragment,true);
    }
    /* Listener, when finished editing restart this activity */
    @Override
    public void OnUserProfilePublicEditFragmentInteraction() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    /* Listener, create session button FAB is clicked switch to maps fragment in order for user to pick location */
    @Override
    public void OnCreateSessionClicked() {
        Bundle bundle = new Bundle();
        bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
        hostMapsFragment = MapsFragment.newInstance();
        hostMapsFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(hostMapsFragment, true);
    }
    /* Listener, when a user is clicked, get the others user ID and start User profile fragment */
    @Override
    public void OnUserClicked(String otherUserID) {
        Bundle bundle = new Bundle();
        bundle.putString("otherUserID", otherUserID);
        hostUserProfilePublicFragment = UserProfilePublicFragment.newInstance();
        hostUserProfilePublicFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(hostUserProfilePublicFragment, true);
    }

    @Override
    public void OnNewMessage() {
        // TODO should this be used?
    }


    @Override
    public void OnEditSession(String sessionID) {
        Bundle bundle = new Bundle();
        bundle.putString("sessionID", sessionID);
        createOrEditSessionFragment = CreateOrEditSessionFragment.newInstance();
        createOrEditSessionFragment.setArguments(bundle);
        cleanMainFullscreenActivityAndSwitch(createOrEditSessionFragment, false);
    }

    /* When back button is pressed while in main host activity override function and replace with following */
    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            // /super.onBackPressed();
            //additional code
        } else {

            // TODO Add Newsfeed fragment here later when exist
            if (!hostUserAccountFragment.isVisible()&&!hostSessionsFragment.isVisible()&&!hostInboxFragment.isVisible()&&!hostAllUsersFragment.isVisible()){
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    // Before starting activity, make sure user is signed-in, otherwise start login activity
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null) {
            //User is signed out
            Intent loginIntent = new Intent(MainHostActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
    }

    // When activity is destroyed, remove all listeners
    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Map.Entry<DatabaseReference, ValueEventListener> entry : listenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }

    }

    @Override
    public void OnHostSessionChanged() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag("xMainHostSessionsFragment")!=null) {
            HostSessionsFragment hs = (HostSessionsFragment) fragmentManager.findFragmentByTag("xMainHostSessionsFragment");
            hs.loadPages(true);
        }

        transaction.replace(R.id.container_fullscreen_main_host,hostDisplaySessionFragment);
        transaction.commit();
    }

    @Override
    public void OnBookSession(String sessionID) {
        // Not applicable in Host environment
    }

    @Override
    public void OnCancelBookedSession(String sessionID) {
        // Not applicable in Host environment
    }
}