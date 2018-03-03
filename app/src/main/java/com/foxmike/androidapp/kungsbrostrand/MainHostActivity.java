package com.foxmike.androidapp.kungsbrostrand;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainHostActivity extends AppCompatActivity implements
        OnSessionClickedListener,
        UserAccountFragment.OnUserAccountFragmentInteractionListener,
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener,
        HostSessionsFragment.OnCreateSessionClickedListener,
        OnUserClickedListener,
        OnNewMessageListener{

    private FragmentManager fragmentManager;
    private UserAccountFragment hostUserAccountFragment;
    private MapsFragment hostMapsFragment;
    private DisplaySessionFragment hostDisplaySessionFragment;
    private InboxFragment hostInboxFragment;
    private HostSessionsFragment hostSessionsFragment;
    private UserProfileFragment hostUserProfileFragment;
    private UserProfilePublicFragment hostUserProfilePublicFragment;
    private UserProfilePublicEditFragment hostUserProfilePublicEditFragment;
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

        mAuth = FirebaseAuth.getInstance();
        userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        rootDbRef = FirebaseDatabase.getInstance().getReference();

        bottomNavigation = findViewById(R.id.bottom_navigation_host);

        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_host_items);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
        bottomNavigation.setAnimation(null);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);


        fragmentManager = getSupportFragmentManager();

        hostUserAccountFragment = UserAccountFragment.newInstance();
        hostSessionsFragment = HostSessionsFragment.newInstance();
        hostInboxFragment = InboxFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
        hostMapsFragment = MapsFragment.newInstance();
        hostMapsFragment.setArguments(bundle);
        hostUserProfileFragment = UserProfileFragment.newInstance();
        hostUserProfilePublicEditFragment = UserProfilePublicEditFragment.newInstance();
        hostAllUsersFragment = AllUsersFragment.newInstance();

        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (null == fragmentManager.findFragmentByTag("hostUserAccountFragment")) {
            transaction.add(R.id.container_main_host, hostUserAccountFragment,"hostUserAccountFragment");
            transaction.hide(hostUserAccountFragment);
        }

        if (null == fragmentManager.findFragmentByTag("hostSessionsFragment")) {
            transaction.add(R.id.container_main_host, hostSessionsFragment,"hostSessionsFragment");
            transaction.hide(hostSessionsFragment);
        }

        if (null == fragmentManager.findFragmentByTag("hostMapsFragment")) {
            transaction.add(R.id.container_main_host, hostMapsFragment,"hostMapsFragment");
            transaction.hide(hostMapsFragment);
        }

        if (null == fragmentManager.findFragmentByTag("hostInboxFragment")) {
            transaction.add(R.id.container_main_host, hostInboxFragment,"hostInboxFragment");
            transaction.hide(hostInboxFragment);
        }

        if (null == fragmentManager.findFragmentByTag("hostUserProfileFragment")) {
            transaction.add(R.id.container_main_host, hostUserProfileFragment,"hostUserProfileFragment");
            transaction.hide(hostUserProfileFragment);
        }

        if (null == fragmentManager.findFragmentByTag("hostUserProfilePublicEditFragment")) {
            transaction.add(R.id.container_main_host, hostUserProfilePublicEditFragment,"hostUserProfilePublicEditFragment");
            transaction.hide(hostUserProfilePublicEditFragment);
        }

        if (null == fragmentManager.findFragmentByTag("hostAllUsersFragment")) {
            transaction.add(R.id.container_main_host, hostAllUsersFragment,"hostAllUsersFragment");
            transaction.hide(hostAllUsersFragment);
        }

        transaction.commit();

        fragmentManager.executePendingTransactions();


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

        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setAccentColor(getResources().getColor(R.color.secondaryColor));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.primaryLightColor));
        cleanMainActivityAndSwitch(hostAllUsersFragment);

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
    }

    // TODO cleanMainActivity is probably useless once Newsfeed fragment has been created, delete this functionality then
    private void cleanMainActivity() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (hostDisplaySessionFragment!=null) {
            transaction.remove(hostDisplaySessionFragment);
        }
        transaction.hide(hostInboxFragment);
        transaction.hide(hostMapsFragment);
        transaction.hide(hostUserAccountFragment);
        transaction.hide(hostUserProfileFragment);
        if (hostUserProfilePublicFragment!=null) {
            transaction.hide(hostUserProfilePublicFragment);
        }
        transaction.hide(hostUserProfilePublicEditFragment);
        transaction.hide(hostSessionsFragment);
        transaction.commit();
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    private void cleanMainActivityAndSwitch(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (hostDisplaySessionFragment!=null) {
            if (hostDisplaySessionFragment.isVisible()) {
                transaction.hide(hostDisplaySessionFragment).addToBackStack("hostDisplaySessionFragment");
            }
        }
        if (hostInboxFragment.isVisible()) {
            transaction.hide(hostInboxFragment).addToBackStack("hostInboxFragment");
        }
        if (hostMapsFragment.isVisible()) {
            transaction.hide(hostMapsFragment).addToBackStack("hostMapsFragment");
        }
        if (hostUserAccountFragment.isVisible()) {
            transaction.hide(hostUserAccountFragment).addToBackStack("hostUserAccountFragment");
        }
        if (hostUserProfileFragment.isVisible()) {
            transaction.hide(hostUserProfileFragment).addToBackStack("hostUserProfileFragment");
        }
        if (hostUserProfilePublicEditFragment.isVisible()) {
            transaction.hide(hostUserProfilePublicEditFragment);
        }
        if (hostSessionsFragment.isVisible()) {
            transaction.hide(hostSessionsFragment).addToBackStack("hostSessionsFragment");
        }
        if (hostAllUsersFragment.isVisible()) {
            transaction.hide(hostAllUsersFragment).addToBackStack("hostAllUsersFragment");
        }

        transaction.show(fragment).addToBackStack("fragment");
        transaction.commit();
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnSessionClicked(double sessionLatitude, double sessionLongitude) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (hostDisplaySessionFragment!=null) {
            transaction.remove(hostDisplaySessionFragment);
        }

        hostDisplaySessionFragment = DisplaySessionFragment.newInstance(sessionLatitude,sessionLongitude);
        hostDisplaySessionFragment.show(transaction,"hostDisplaySessionFragment");
    }

    @Override
    public void OnUserAccountFragmentInteraction(String type) {

        if (type.equals("edit")) {
            cleanMainActivityAndSwitch(hostUserProfileFragment);
            bottomNavigation.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserProfileFragmentInteraction() {
        cleanMainActivityAndSwitch(hostUserProfilePublicEditFragment);
        bottomNavigation.setVisibility(View.GONE);
    }

    @Override
    public void OnUserProfilePublicEditFragmentInteraction() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            // /super.onBackPressed();
            //additional code
        } else {
            if (hostUserProfilePublicFragment.isVisible()) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
            if (hostMapsFragment.isVisible()) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
            // TODO Add Newsfeed fragment here later when exist
            if (!hostUserAccountFragment.isVisible()&&!hostSessionsFragment.isVisible()&&!hostInboxFragment.isVisible()&&!hostAllUsersFragment.isVisible()){
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void OnCreateSessionClicked() {
        cleanMainActivityAndSwitch(hostMapsFragment);
        bottomNavigation.setVisibility(View.GONE);
    }


    @Override
    public void OnUserClicked(String otherUserID) {
        if (fragmentManager.findFragmentByTag("hostUserProfilePublicFragment") != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragmentManager.findFragmentByTag("hostUserProfilePublicFragment"));
            transaction.commitNow();

            Bundle bundle = new Bundle();
            bundle.putString("otherUserID", otherUserID);
            hostUserProfilePublicFragment = UserProfilePublicFragment.newInstance();
            hostUserProfilePublicFragment.setArguments(bundle);

            FragmentTransaction transaction2 = fragmentManager.beginTransaction();
            transaction2.add(R.id.container_main_host, hostUserProfilePublicFragment,"hostUserProfilePublicFragment");
            transaction2.hide(hostUserProfilePublicFragment);
            transaction2.commitNow();

            cleanMainActivityAndSwitch(hostUserProfilePublicFragment);
        } else {

            Bundle bundle = new Bundle();
            bundle.putString("otherUserID", otherUserID);
            hostUserProfilePublicFragment = UserProfilePublicFragment.newInstance();
            hostUserProfilePublicFragment.setArguments(bundle);

            FragmentTransaction transaction3 = fragmentManager.beginTransaction();
            transaction3.add(R.id.container_main_host, hostUserProfilePublicFragment,"hostUserProfilePublicFragment");
            transaction3.hide(hostUserProfilePublicFragment);
            transaction3.commitNow();

            cleanMainActivityAndSwitch(hostUserProfilePublicFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null) {
            //User is signed out
            Intent loginIntent = new Intent(MainHostActivity.this, com.foxmike.androidapp.kungsbrostrand.LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
    }

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
    public void OnNewMessage() {

    }
}
