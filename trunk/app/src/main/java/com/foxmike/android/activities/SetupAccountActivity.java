package com.foxmike.android.activities;
//Checked

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.foxmike.android.models.UserImageUrlMap;
import com.foxmike.android.utils.AddUserToDatabase;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupAccountActivity extends AppCompatActivity {

    private CircleImageView mSetupImageButton;
    private EditText mFirstNameField;
    private EditText mLastNameField;
    private Uri mImageUri = null;
    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private ProgressBar progressBar;
    private String currentUserID;
    private View mainView;

    private FirebaseFunctions mFunctions;
    private HashMap<String, Object> friendsData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);

        mFunctions = FirebaseFunctions.getInstance();

        getWindow().setStatusBarColor(getResources().getColor(R.color.foxmikePrimaryColor));

        Button mSubmitBtn;

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");

        mSetupImageButton = findViewById(R.id.setupImageButton);
        mFirstNameField = findViewById(R.id.setupFirstNameField);
        mLastNameField = findViewById(R.id.setupLastNameField);
        mSubmitBtn = findViewById(R.id.setupSubmitBtn);
        progressBar = findViewById(R.id.progressBar_cyclic);
        mainView = findViewById(R.id.mainView);

        // Setup image button to choose image from gallery
        mSetupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        //Set submit button to start function startSetupAccount
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });
    }
    /* Function to update database with user information and then when finished send user to MainActivity */
    private void startSetupAccount(){
        final MyProgressBar myProgressBar = new MyProgressBar(progressBar, this);
        myProgressBar.startProgressBar();
        final String firstName = mFirstNameField.getText().toString().trim();
        final String lastName = mLastNameField.getText().toString().trim();

        currentUserID = mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && mImageUri != null){

            final User user = new User();

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setFullName(firstName + " " + lastName);

            SetOrUpdateUserImage setOrUpdateUserImage = new SetOrUpdateUserImage();
            setOrUpdateUserImage.setOrUpdateUserImages(SetupAccountActivity.this, mImageUri, currentUserID, new SetOrUpdateUserImage.OnUserImageSetListener() {
                @Override
                public void onUserImageSet(UserImageUrlMap userImageUrlMap) {
                    user.setImage(userImageUrlMap.getUserImageUrl());
                    user.setThumb_image(userImageUrlMap.getUserThumbImageUrl());

                    AddUserToDatabase addUserToDatabase = new AddUserToDatabase();
                    addUserToDatabase.AddUserToDatabaseWithUniqueUsername(SetupAccountActivity.this, user);
                    addUserToDatabase.setOnUserAddedToDatabaseListener(new AddUserToDatabase.OnUserAddedToDatabaseListener() {
                        @Override
                        public void OnUserAddedToDatabase() {
                            FirebaseDatabase.getInstance().getReference().child("foxmikeUID").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue()!=null) {
                                        String foxmikeUID = dataSnapshot.getValue().toString();
                                        if (!mAuth.getCurrentUser().getUid().equals(foxmikeUID)) {




                                            String currentDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                                            friendsData.put("currentUserId", mAuth.getCurrentUser().getUid());
                                            friendsData.put("foxmikeUID", foxmikeUID);
                                            friendsData.put("currentDate", currentDate);
                                            makeUserFriendWithFoxmike(friendsData).addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    // If not succesful, show error
                                                    if (!task.isSuccessful()) {
                                                        Exception e = task.getException();
                                                        myProgressBar.stopProgressBar();
                                                        showSnackbar("An error occurred." + e.getMessage());
                                                        return;
                                                    }
                                                    // Show the string passed from the Firebase server if task/function call on server is successful
                                                    String result = task.getResult();
                                                    if (result.equals("success")) {
                                                        finishSetup(myProgressBar);
                                                    } else {
                                                        showSnackbar(result);
                                                    }
                                                }
                                            });
                                        } else {
                                            finishSetup(myProgressBar);
                                        }
                                    } else {
                                        finishSetup(myProgressBar);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void finishSetup(MyProgressBar myProgressBar) {
        myProgressBar.stopProgressBar();
        Intent mainIntent = new Intent(SetupAccountActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
    }

    // when gallery intent has been opened and an image clicked start crop image activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        // when image has been cropped set image to button and to variable mImageUri
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mSetupImageButton.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()==null) {
            Intent mainIntent = new Intent(SetupAccountActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }
    }

    // Function makeUserFriendWithFoxmike
    private Task<String> makeUserFriendWithFoxmike(Map<String, Object> friendsData) {
        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("makeUserFriendWithFoxmike")
                .call(friendsData)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("operationResult");
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainView, message, Snackbar.LENGTH_SHORT).show();
    }
}
