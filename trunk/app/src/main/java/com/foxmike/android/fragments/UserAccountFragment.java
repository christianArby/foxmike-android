package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.activities.LoginActivity;
import com.foxmike.android.activities.MainActivity;
import com.foxmike.android.activities.MainHostActivity;
import com.foxmike.android.activities.MainPlayerActivity;
import com.foxmike.android.activities.WelcomeActivity;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
        switchModeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

                userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
                        User user = dataSnapshot.getValue(User.class);
                        if (user.trainerMode) {
                            usersDbRef.child(mAuth.getCurrentUser().getUid()).child("trainerMode").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(getActivity(), MainPlayerActivity.class);
                                    getActivity().startActivity(intent);
                                }
                            });
                        } else {
                            usersDbRef.child(mAuth.getCurrentUser().getUid()).child("trainerMode").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(getActivity(), MainHostActivity.class);
                                    getActivity().startActivity(intent);
                                }
                            });
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
                    switchModeTV.setText(R.string.switch_to_participant_mode_text);
                } else {
                    switchModeTV.setText(R.string.switch_to_host_mode_text);
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


        TextView logOutView = view.findViewById(R.id.logOutTV);
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
        Intent welcomeIntent = new Intent(getActivity(),WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(welcomeIntent);
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
