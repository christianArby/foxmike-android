package com.foxmike.android.fragments;
// Checked
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserFoundListener;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.foxmike.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
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
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private ValueEventListener currentUserListener;
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
        final EditText userFirstNameET = profile.findViewById(R.id.firstNameProfilePublicEditET);
        final EditText userLastNameET = profile.findViewById(R.id.lastNameProfilePublicEditET);
        final EditText userAboutMeET = profile.findViewById(R.id.aboutMeProfilePublicEditET);
        final EditText userNameET = profile.findViewById(R.id.userNameProfilePublicEditET);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        profileImageButton = profile.findViewById(R.id.profilePublicEditIB);
        Button updateBtn= profile.findViewById(R.id.updateProfileBtn);
        // setup the imagebutton with the users profile image

        currentUserListener = usersDbRef.child(currentFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userFirstNameET.setText(user.getFirstName());
                userLastNameET.setText(user.getLastName());
                userAboutMeET.setText(user.getAboutMe());
                userNameET.setText(user.getUserName());
                setImageButton(user.image,profileImageButton);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userNameET.addTextChangedListener(new TextWatcher() {
            boolean _ignore = false;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (_ignore)
                    return;

                _ignore = true; // prevent infinite loop

                if (editable.toString().equals("")) {
                    userNameET.setText("@");
                    userNameET.setSelection(userNameET.getText().length());
                } else if (!editable.toString().substring(0,1).equals("@")) {
                    userNameET.setText("@" + editable.toString().toLowerCase());
                    userNameET.setSelection(userNameET.getText().length());
                } else {
                    int pos = userNameET.getSelectionStart();
                    userNameET.setText(editable.toString().toLowerCase());
                    userNameET.setSelection(pos);
                }

                _ignore = false; // release, so the TextWatcher start to listen again.
            }
        });

        // Setup the update button and when clicked update the user information and picture in database
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
                myProgressBar.startProgressBar();

                // Define characters not allowed in username
                char[] notContain = {'.', '#', '$', '[', ']'};
                String cannotContain = getString(R.string.userName_cannotContain_text);

                String userName = userNameET.getText().toString();
                // check input so that username only contains vaild characters
                if (containsAny(userName,notContain)) {
                    myProgressBar.stopProgressBar();
                    Toast.makeText(getActivity(), cannotContain, Toast.LENGTH_SHORT).show();
                } else {

                    // check if username already exists in database
                    rootDbRef.child("usernames").child(userName).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(currentFirebaseUser.getUid());
                                return Transaction.success(mutableData);
                            }

                            return Transaction.abort();
                        }

                        @Override
                        public void onComplete(DatabaseError firebaseError, boolean commited, DataSnapshot dataSnapshot) {

                            // if commited, username did not exist, write to database
                            if (commited) {

                                usersDbRef.child(currentFirebaseUser.getUid()).child("firstName").setValue(userFirstNameET.getText().toString());
                                usersDbRef.child(currentFirebaseUser.getUid()).child("lastName").setValue(userLastNameET.getText().toString());
                                usersDbRef.child(currentFirebaseUser.getUid()).child("aboutMe").setValue(userAboutMeET.getText().toString());
                                usersDbRef.child(currentFirebaseUser.getUid()).child("userName").setValue(userNameET.getText().toString());
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

                            // else dismiss and tell the user the username is already taken
                            } else {
                                myProgressBar.stopProgressBar();
                                Toast.makeText(getActivity(), "The username is already taken", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentUserListener!=null) {
            usersDbRef.child(currentFirebaseUser.getUid()).removeEventListener(currentUserListener);
        }
    }

    public static boolean containsAny(String str, char[] searchChars) {
        if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            for (int j = 0; j < searchChars.length; j++) {
                if (searchChars[j] == ch) {
                    return true;
                }
            }
        }
        return false;
    }
}
