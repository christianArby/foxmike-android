package com.foxmike.android.fragments;
// Checked

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.foxmike.android.models.UserImageUrlMap;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * This Fragment shows the current user's profile and lets the user change the information
 */
public class UserProfilePublicEditFragment extends Fragment {

    public static final String TAG = UserProfilePublicEditFragment.class.getSimpleName();

    private OnUserProfilePublicEditFragmentInteractionListener mListener;

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private ValueEventListener currentUserListener;
    private CircleImageView profileImageButton;
    private ImageView smallImageBtn;
    private ProgressBar progressBar;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri = null;
    private LinearLayout list;
    private View profile;
    private User currentUser;
    static UserProfilePublicEditFragment fragment;
    private InputMethodManager imm;
    private TextInputLayout aboutMeTIL;
    private boolean infoIsValid;

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
        aboutMeTIL = profile.findViewById(R.id.aboutMeProfilePublicEditTIL);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        profileImageButton = profile.findViewById(R.id.profilePublicEditIB);
        Button updateBtn= profile.findViewById(R.id.updateProfileBtn);
        smallImageBtn = profile.findViewById(R.id.smallImageBtn);

        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        // setup the imagebutton with the users profile image
        // GET CURRENT USER FROM DATABASE
        FirebaseDatabaseViewModel firebaseDatabaseUserViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseUserLiveData = firebaseDatabaseUserViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        firebaseDatabaseUserLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    currentUser = dataSnapshot.getValue(User.class);
                    userFirstNameET.setText(currentUser.getFirstName());
                    userLastNameET.setText(currentUser.getLastName());
                    userAboutMeET.setText(currentUser.getAboutMe());
                    userNameET.setText(currentUser.getUserName());
                    if(mImageUri==null) {
                        setImageButton(currentUser.getImage(), profileImageButton);
                    }

                    infoIsValid = true;

                    if (currentUser.getStripeAccountId()!=null) {
                        if (currentUser.getAboutMe()==null) {
                            aboutMeTIL.setError(getString(R.string.please_write_a_longer_description));
                            infoIsValid = false;
                        } else {
                            if (currentUser.getAboutMe().length()<51) {
                                aboutMeTIL.setError(getString(R.string.please_write_a_longer_description));
                                infoIsValid = false;
                            }
                        }
                    }



                    if (currentUser.getStripeAccountId()!=null) {
                        userAboutMeET.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                if (editable.toString().length()<51) {
                                    aboutMeTIL.setError(getString(R.string.please_write_a_longer_description));
                                    infoIsValid = false;
                                } else {
                                    infoIsValid = true;
                                    aboutMeTIL.setError(null);
                                }

                            }
                        });
                    }


                }
            }
        });

        userNameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (userNameET.getText().length()==0) {
                        userNameET.setText("@");
                    }
                }
            }
        });

        userNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                for (int i =0; i < editable.length(); i++) {

                    // If it is a S position and does not contain a S insert a S
                    if (i==0) {
                        CharSequence s = "@";
                        if (editable.charAt(i)!='@') {
                            editable.insert(i, s);
                        }
                    } else {
                        // If it is NOT a S position and contains a S position remove a character
                        if (editable.charAt(i)=='@') {
                            editable.replace(i,i+1, "");
                        }
                    }
                }
            }
        });

        // Setup the update button and when clicked update the user information and picture in database
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (infoIsValid) {
                    final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());
                    myProgressBar.startProgressBar();

                    // Define characters not allowed in username
                    char[] notContain = {'.', '#', '$', '[', ']'};
                    String cannotContain = getString(R.string.userName_cannotContain_text);

                    String userName = userNameET.getText().toString();
                    // check input so that username only contains vaild characters
                    if (containsAny(userName,notContain)) {
                        myProgressBar.stopProgressBar();
                        Toast.makeText(getActivity().getApplicationContext(), cannotContain, Toast.LENGTH_SHORT).show();
                    } else {

                        // check if user has changed username
                        if (currentUser!=null) {
                            if (userName.equals(currentUser.userName)) {
                                // no changes in userName, write to database...
                                if (currentUser.getStripeAccountId()!=null && TextUtils.isEmpty(userAboutMeET.getText().toString())) {
                                    myProgressBar.stopProgressBar();
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.describe_yourself, Toast.LENGTH_SHORT).show();
                                } else {
                                    writeToDatabase(myProgressBar);
                                }
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
                                            if (currentUser.getStripeAccountId()!=null && TextUtils.isEmpty(userAboutMeET.getText().toString())) {
                                                myProgressBar.stopProgressBar();
                                                Toast.makeText(getActivity().getApplicationContext(), R.string.describe_yourself, Toast.LENGTH_SHORT).show();
                                            } else {
                                                writeToDatabase(myProgressBar);
                                            }
                                            // else dismiss and tell the user the username is already taken
                                        } else {
                                            myProgressBar.stopProgressBar();
                                            Toast.makeText(getActivity().getApplicationContext(), R.string.username_already_taken, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }



                    }

                }


            }

            private void writeToDatabase(final MyProgressBar myProgressBar) {
                usersDbRef.child(currentFirebaseUser.getUid()).child("firstName").setValue(userFirstNameET.getText().toString());
                usersDbRef.child(currentFirebaseUser.getUid()).child("lastName").setValue(userLastNameET.getText().toString());
                usersDbRef.child(currentFirebaseUser.getUid()).child("aboutMe").setValue(userAboutMeET.getText().toString());
                usersDbRef.child(currentFirebaseUser.getUid()).child("userName").setValue(userNameET.getText().toString());

                if(mImageUri!=null) {
                    SetOrUpdateUserImage setOrUpdateUserImage = new SetOrUpdateUserImage();
                    setOrUpdateUserImage.setOrUpdateUserImages(getActivity(), mImageUri, currentFirebaseUser.getUid(), new SetOrUpdateUserImage.OnUserImageSetListener() {
                        @Override
                        public void onUserImageSet(UserImageUrlMap userImageUrlMap) {
                            Map imageUrlHashMap = new HashMap();
                            imageUrlHashMap.put("image", userImageUrlMap.getUserImageUrl());
                            imageUrlHashMap.put("thumb_image", userImageUrlMap.getUserThumbImageUrl());
                            usersDbRef.child(currentFirebaseUser.getUid()).updateChildren(imageUrlHashMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    mListener.OnUserProfilePublicEditFragmentInteraction();
                                    myProgressBar.stopProgressBar();
                                }
                            });

                        }
                    });

                } else {
                    mListener.OnUserProfilePublicEditFragmentInteraction();
                    myProgressBar.stopProgressBar();
                }
            }
        });

        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1,1)
                        .start(getActivity().getApplicationContext(), fragment);
            }
        });
        smallImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1,1)
                        .start(getActivity().getApplicationContext(), fragment);
            }
        });

        return view;
    }

    // Method to set and scale an image into an circular imageView
    private void setImageButton(String image, CircleImageView imageButton) {
        Glide.with(getActivity().getApplicationContext()).load(image).into(imageButton);
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
                    .start(getActivity().getApplicationContext(), fragment);
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
        ((AppCompatActivity)getActivity()).setSupportActionBar(null);
        if (currentUserListener!=null) {
            usersDbRef.child(currentFirebaseUser.getUid()).removeEventListener(currentUserListener);
        }
        hideKeyboard();
        imm = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
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
