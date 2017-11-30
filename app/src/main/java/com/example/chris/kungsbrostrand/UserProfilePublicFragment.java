package com.example.chris.kungsbrostrand;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfilePublicFragment extends Fragment {

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;
    private OnUserProfilePublicFragmentInteractionListener mListener;

    private LinearLayout list;
    private LatLng sessionLatLng;
    private View profile;


    public UserProfilePublicFragment() {
        // Required empty public constructor
    }

    public static UserProfilePublicFragment newInstance() {
        UserProfilePublicFragment fragment = new UserProfilePublicFragment();
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
        View view = inflater.inflate(R.layout.fragment_user_profile_public, container, false);

        mAuth = FirebaseAuth.getInstance();

        /* Inflate the LinearLayout list (in fragment_user_profile_public) with the layout user_profile_info */
        list = view.findViewById(R.id.list_user_profile_public);
        profile = inflater.inflate(R.layout.user_profile_public_info,list,false);
        list.addView(profile);

        final TextView userNameTV = profile.findViewById(R.id.nameProfilePublicTV);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        ImageView editIconIV = view.findViewById(R.id.editIconIV);

        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(User user) {
                userNameTV.setText(user.getName());
                setCircleImage(user.image,(CircleImageView) profile.findViewById(R.id.profilePublicIV));
            }
        });


        editIconIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.OnUserProfilePublicFragmentInteraction();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UserProfilePublicFragment.OnUserProfilePublicFragmentInteractionListener) {
            mListener = (UserProfilePublicFragment.OnUserProfilePublicFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserProfilePublicFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnUserProfilePublicFragmentInteractionListener {
        void OnUserProfilePublicFragmentInteraction();
    }

}
