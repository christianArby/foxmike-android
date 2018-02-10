package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This Fragment creates the user profile view by using the xml files:
 *      - fragment_user_account.xml
 *      - user_profile_info.xml
 *      - session_small_single_layout.xml_layout.xml
 * The data (used to generate the views) is retrieved by using an object of the class PlayerSessionsContent in order to generate the fill the views with content in the correct order.
 */

public class UserAccountFragment extends Fragment {

    private FirebaseAuth mAuth;
    private OnUserAccountFragmentInteractionListener mListener;

    private LinearLayout list;
    private View profile;

    public UserAccountFragment() {
        // Required empty public constructor
    }

    public static UserAccountFragment newInstance() {
        UserAccountFragment fragment = new UserAccountFragment();
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

        /* Get the view fragment_user_account */
        final View view = inflater.inflate(R.layout.fragment_user_account, container, false);
        mAuth = FirebaseAuth.getInstance();

        /* Inflate the LinearLayout list (in fragment_user_account) with the layout user_profile_info */
        list = view.findViewById(R.id.list1);
        profile = inflater.inflate(R.layout.user_profile_info,list,false);
        list.addView(profile);

        final TextView userNameTV = profile.findViewById(R.id.profileTV);
        TextView editProfileTV = profile.findViewById(R.id.edit_session_question);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Find and set the clickable LinearLayout switchModeLL and write the trainerMode status to the database */
        final TextView switchModeTV = view.findViewById(R.id.switchModeTV);
        final View switchMode = view.findViewById(R.id.switchModeLL);
        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

                userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");

                        User user = dataSnapshot.getValue(User.class);

                        if (user.trainerMode) {
                            //user.setTrainerMode(false);
                            usersDbRef.child(mAuth.getCurrentUser().getUid()).child("trainerMode").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(getActivity(), MainPlayerActivity.class);
                                    getActivity().startActivity(intent);
                                }
                            });

                        } else {
                            //user.setTrainerMode(true);
                            usersDbRef.child(mAuth.getCurrentUser().getUid()).child("trainerMode").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(getActivity(), MainHostActivity.class);
                                    getActivity().startActivity(intent);
                                }
                            });

                            //changeMode(user.trainerMode);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(User user) {
                userNameTV.setText(user.getName());
                setCircleImage(user.image,(CircleImageView) profile.findViewById(R.id.profileIV));
                if (user.trainerMode) {
                    switchModeTV.setText("Switch to participant mode");
                } else {
                    switchModeTV.setText("Switch to trainer mode");
                }
            }
        });

        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.OnUserAccountFragmentInteraction("edit");
                }
            }
        });

        userNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.OnUserAccountFragmentInteraction("edit");
                }
            }
        });


        View logOutView = view.findViewById(R.id.logOutLL);
        logOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    private void logout() {
        Intent loginIntent = new Intent(getActivity(),LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        mAuth.signOut();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserAccountFragmentInteractionListener) {
            mListener = (OnUserAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserAccountFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnUserAccountFragmentInteractionListener {
        void OnUserAccountFragmentInteraction(String type);
    }

}
