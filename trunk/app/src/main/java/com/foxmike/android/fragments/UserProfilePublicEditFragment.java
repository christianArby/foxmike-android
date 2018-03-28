package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.foxmike.android.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
/**
 * This Fragment shows the current user's profile and lets the user change the information
 */
public class UserProfilePublicEditFragment extends Fragment {

    private OnUserProfilePublicEditFragmentInteractionListener mListener;

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private CircleImageView profileImageButton;
    private ProgressBar progressBar;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri = null;
    private LinearLayout list;
    private View profile;
    static UserProfilePublicEditFragment fragment;

    public UserProfilePublicEditFragment() {
        // Required empty public constructor
    }

    public static UserProfilePublicEditFragment newInstance() {
        fragment = new UserProfilePublicEditFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile_public_edit, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar_cyclic);

        /* Inflate the LinearLayout list (in fragment_user_profile_public) with the layout user_profile_info */
        list = view.findViewById(R.id.list_user_profile_public_edit);
        profile = inflater.inflate(R.layout.user_profile_public_edit_info,list,false);
        list.addView(profile);
        final EditText userNameET = profile.findViewById(R.id.nameProfilePublicEditET);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        profileImageButton = profile.findViewById(R.id.profilePublicEditIB);
        Button updateBtn= profile.findViewById(R.id.updateProfileBtn);
        // setup the imagebutton with the users profile image
        myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(User user) {
                userNameET.setText(user.getName());
                setImageButton(user.image,profileImageButton);
            }
        });
        // Setup the update button and when clicked update the user information and picture in database
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
                myProgressBar.startProgressBar();

                usersDbRef.child(currentFirebaseUser.getUid()).child("name").setValue(userNameET.getText().toString());
                if(mImageUri!=null) {
                    SetOrUpdateUserImage setOrUpdateUserImage = new SetOrUpdateUserImage();
                    setOrUpdateUserImage.setOnUserImageSetListener(new SetOrUpdateUserImage.OnUserImageSetListener() {
                        @Override
                        public void onUserImageSet() {
                            mListener.OnUserProfilePublicEditFragmentInteraction();
                            myProgressBar.stopProgressBar();
                        }
                    });
                    setOrUpdateUserImage.setOrUpdateUserImages(getActivity(),mImageUri,currentFirebaseUser.getUid());
                } else {
                    mListener.OnUserProfilePublicEditFragmentInteraction();
                    myProgressBar.stopProgressBar();
                }
            }
        });

        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                fragment.startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        return view;
    }

    // Method to set and scale an image into an circular imageView
    private void setImageButton(String image, CircleImageView imageButton) {
        Glide.with(this).load(image).into(imageButton);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1,1)
                    .start(fragment.getContext(), fragment);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                profileImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                profileImageButton.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener) {
            mListener = (UserProfilePublicEditFragment.OnUserProfilePublicEditFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserProfilePublicEditFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnUserProfilePublicEditFragmentInteractionListener {
        void OnUserProfilePublicEditFragmentInteraction();
    }

}
