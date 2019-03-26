package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * This Fragment shows the current user's profile (without buttons to add friend or send message as in UserProfilePublicFragment)
 */
public class UserProfileFragment extends Fragment {

    public static final String TAG = UserProfileFragment.class.getSimpleName();

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private ValueEventListener currentUserListener;
    private FirebaseAuth mAuth;
    private LinearLayout list;
    private View profile;
    private long mLastClickTime = 0;
    private FloatingActionButton editIcon;

    private OnUserProfileFragmentInteractionListener onUserProfileFragmentInteractionListener;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        /* Inflate the LinearLayout list (in fragment_user_profile_public) with the layout user_profile_info */
        list = view.findViewById(R.id.list_user_profile);
        profile = inflater.inflate(R.layout.user_profile_public_info,list,false);
        list.addView(profile);
        editIcon = view.findViewById(R.id.editIconIV);

        final TextView fullNameTV = profile.findViewById(R.id.nameProfilePublicTV);
        final TextView userAboutMeTV = profile.findViewById(R.id.aboutMeProfilePublicTV);
        final TextView userNameTV = profile.findViewById(R.id.userNameProfilePublicTV);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();


        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        currentUserListener = usersDbRef.child(currentFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userDb = dataSnapshot.getValue(User.class);
                fullNameTV.setText(userDb.getFullName());
                userAboutMeTV.setText(userDb.getAboutMe());
                userNameTV.setText(userDb.getUserName());

                setCircleImage(userDb.getImage(), (CircleImageView) profile.findViewById(R.id.profilePublicIV));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onUserProfileFragmentInteractionListener.onUserProfileFragmentInteraction();
            }
        });
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onUserProfileFragmentInteractionListener.onUserProfileFragmentInteraction();
            }
        });
        return  view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserProfileFragmentInteractionListener) {
            onUserProfileFragmentInteractionListener = (OnUserProfileFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onUserProfileFragmentInteractionListener = null;
    }

    public interface OnUserProfileFragmentInteractionListener {
        void onUserProfileFragmentInteraction();
    }

    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity)getActivity()).setSupportActionBar(null);
        if (currentUserListener!=null) {
            usersDbRef.child(currentFirebaseUser.getUid()).removeEventListener(currentUserListener);
        }
    }
}
