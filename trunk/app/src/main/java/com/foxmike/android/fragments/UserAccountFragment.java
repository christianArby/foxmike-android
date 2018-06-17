package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.activities.CreateStripeAccountActivity;
import com.foxmike.android.activities.MainHostActivity;
import com.foxmike.android.activities.MainPlayerActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
import com.foxmike.android.activities.PayoutPreferencesActivity;
import com.foxmike.android.activities.WelcomeActivity;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

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
    private ValueEventListener currentUserListener;
    private String currentUserID;
    private DatabaseReference rootDbRef;
    private LinearLayout list;
    private LinearLayout payoutMethodContainer;
    private View profile;
    private TextView addPaymentMethod;
    private TextView addPayoutMethod;
    private FrameLayout progressBackground;
    private View view;

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
        view = inflater.inflate(R.layout.fragment_user_account, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        /* Inflate the LinearLayout list (in fragment_user_account) with the layout user_profile_info */
        list = view.findViewById(R.id.list1);
        profile = inflater.inflate(R.layout.user_profile_info,list,false);
        list.addView(profile);
        final ProgressBar progressBar = view.findViewById(R.id.progressBar_cyclic);
        final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());

        final TextView fullNameTV = profile.findViewById(R.id.profileTV);
        final TextView userNameTV = profile.findViewById(R.id.userNameTV);
        TextView editProfileTV = profile.findViewById(R.id.edit_session_question);
        progressBackground = view.findViewById(R.id.progressBackground);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Find and set the clickable LinearLayout switchModeLL and write the trainerMode status to the database */
        final TextView switchModeTV = view.findViewById(R.id.switchModeTV);
        addPaymentMethod = profile.findViewById(R.id.addPaymentMethodTV);
        addPayoutMethod = profile.findViewById(R.id.addPayoutMethodTV);
        payoutMethodContainer = profile.findViewById(R.id.payoutMethodContainer);
        switchModeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myProgressBar.startProgressBar();
                progressBackground.setVisibility(View.VISIBLE);
                final DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

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
                            if (user.getStripeAccountId()!=null) {
                                usersDbRef.child(mAuth.getCurrentUser().getUid()).child("trainerMode").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent intent = new Intent(getActivity(), MainHostActivity.class);
                                        getActivity().startActivity(intent);
                                    }
                                });
                            } else {
                                myProgressBar.stopProgressBar();
                                Intent createIntent = new Intent(getContext(),CreateStripeAccountActivity.class);
                                startActivityForResult(createIntent, 1);
                                progressBackground.setVisibility(View.GONE);
                            }

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

        rootDbRef = FirebaseDatabase.getInstance().getReference();

        currentUserListener = rootDbRef.child("users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                fullNameTV.setText(user.getFullName());
                userNameTV.setText(user.getUserName());
                setCircleImage(user.image,(CircleImageView) profile.findViewById(R.id.profileIV));
                if (user.trainerMode) {
                    switchModeTV.setText(R.string.switch_to_participant_mode_text);
                    payoutMethodContainer.setVisibility(View.VISIBLE);
                } else {
                    if (user.getStripeAccountId()!=null) {
                        switchModeTV.setText(R.string.switch_to_host_mode_text);
                        payoutMethodContainer.setVisibility(View.GONE);
                    } else {
                        switchModeTV.setText(R.string.become_trainer);
                        payoutMethodContainer.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

        fullNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.OnUserAccountFragmentInteraction("edit");
                }
            }
        });

        addPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
            }
        });

        addPayoutMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent payoutPreferencesIntent = new Intent(getActivity(),PayoutPreferencesActivity.class);
                startActivity(payoutPreferencesIntent);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent intent = new Intent(getActivity(), MainHostActivity.class);
            getActivity().startActivity(intent);
            getActivity().finish();
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        if (currentUserListener!=null) {
            rootDbRef.child("users").child(currentUserID).removeEventListener(currentUserListener);
        }
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
