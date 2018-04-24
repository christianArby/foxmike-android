package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.models.User;
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

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private ValueEventListener currentUserListener;
    private FirebaseAuth mAuth;
    private LinearLayout list;
    private View profile;

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

        final TextView userNameTV = profile.findViewById(R.id.nameProfilePublicTV);
        final TextView userAboutMeTV = profile.findViewById(R.id.aboutMeProfilePublicTV);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        currentUserListener = usersDbRef.child(currentFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userDb = dataSnapshot.getValue(User.class);
                userNameTV.setText(userDb.getFullName());
                userAboutMeTV.setText(userDb.getAboutMe());
                setCircleImage(userDb.image,(CircleImageView) profile.findViewById(R.id.profilePublicIV));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if (currentUserListener!=null) {
            usersDbRef.child(currentFirebaseUser.getUid()).removeEventListener(currentUserListener);
        }
    }
}