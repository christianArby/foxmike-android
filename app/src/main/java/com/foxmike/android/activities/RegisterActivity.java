package com.foxmike.android.activities;
//Checked

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.foxmike.android.models.UserImageUrlMap;
import com.foxmike.android.utils.AddUserToDatabase;
import com.foxmike.android.utils.FoxmikeFont;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.foxmike.android.viewmodels.MaintenanceViewModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.foxmike.android.R.layout.activity_register;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout firstNameTIL;
    private TextInputLayout lastNameTIL;
    private TextInputLayout passwordTIL;
    private TextInputLayout emailTIL;
    private TextInputEditText mFirstNameField;
    private TextInputEditText mLastNameField;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private CircleImageView mRegisterImageButton;
    private ImageView mSmallRegisterImageButton;
    private TextView imageErrorTextTV;
    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference rootDbRef;
    private ProgressBar progressBar;
    private String currentUserID;
    private Uri mImageUri = null;
    private String userName;
    private View mainView;
    private long mLastClickTime = 0;
    private DatabaseReference maintenanceRef;
    private ValueEventListener maintenanceListener;


    private int randomPIN;
    private String PINString;
    private int numberOfTriedUserNames;
    private int startRangeCeiling;

    private FirebaseFunctions mFunctions;
    private HashMap<String, Object> friendsData = new HashMap<>();
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private MyProgressBar myProgressBar;
    private User user;
    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(RegisterActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_register);

        mFunctions = FirebaseFunctions.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        getWindow().setStatusBarColor(getResources().getColor(R.color.foxmikePrimaryColor));

        Button mRegisterBtn;

        progressBar = findViewById(R.id.progressBar_cyclic);
        mFirstNameField = findViewById(R.id.setupFirstNameField);
        mLastNameField = findViewById(R.id.setupLastNameField);
        mEmailField= findViewById(R.id.emailField);
        firstNameTIL = findViewById(R.id.firstNameTIL);
        lastNameTIL = findViewById(R.id.lastNameTIL);
        emailTIL = findViewById(R.id.emailTIL);
        passwordTIL = findViewById(R.id.passwordTIL);
        mPasswordField = findViewById(R.id.passwordField);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mRegisterImageButton = findViewById(R.id.registerImageBtn);
        imageErrorTextTV = findViewById(R.id.imageErrorText);
        mainView = findViewById(R.id.mainView);
        mSmallRegisterImageButton = findViewById(R.id.smallRegisterImageBtn);

        if (FoxmikeFont.hasCustomFont) {
            Typeface customFont = ResourcesCompat.getFont(this, FoxmikeFont.customFontRegular);
            passwordTIL.setTypeface(customFont);
            mPasswordField.setTypeface(customFont);
        }


        imageErrorTextTV.setText(null);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        rootDbRef = FirebaseDatabase.getInstance().getReference();

        // Setup image button to choose image from gallery
        mRegisterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                imageErrorTextTV.setText(null);
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        mSmallRegisterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                imageErrorTextTV.setText(null);
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        // Setup register button to start function startRegister when clicked
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                startRegister();
            }
        });

        mFirstNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                firstNameTIL.setError(null);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mLastNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                lastNameTIL.setError(null);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordTIL.setError(null);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEmailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailTIL.setError(null);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /* Function to register user in auth and database */
    private void startRegister() {

        myProgressBar = new MyProgressBar(progressBar,this);
        // get input
        firstName = mFirstNameField.getText().toString().trim();
        lastName = mLastNameField.getText().toString().trim();
        email = mEmailField.getText().toString().trim();
        password = mPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            firstNameTIL.setError(getString(R.string.cannot_be_blank_text));
        }
        if (TextUtils.isEmpty(lastName)) {
            lastNameTIL.setError(getString(R.string.cannot_be_blank_text));
        }
        if (TextUtils.isEmpty(email)) {
            emailTIL.setError(getString(R.string.cannot_be_blank_text));
        }
        if (TextUtils.isEmpty(password)) {
            passwordTIL.setError(getString(R.string.cannot_be_blank_text));
        }
        if (mImageUri==null) {
            imageErrorTextTV.setText(R.string.please_choose_profile_image_text);
        }

        // if all input has been filled in create user
        if(!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && mImageUri != null){
            myProgressBar.startProgressBar();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Register success,  create random user name and save user in Realtime database
                        currentUserID = mAuth.getCurrentUser().getUid();
                        // ----------------------------------- NEW -------------------------

                        user = new User();
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setFullName(firstName + " " + lastName);

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.METHOD, "email");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

                        SetOrUpdateUserImage setOrUpdateUserImage = new SetOrUpdateUserImage();
                        setOrUpdateUserImage.setOrUpdateUserImages(RegisterActivity.this, mImageUri, currentUserID, getString(R.string.storage_bucket), new SetOrUpdateUserImage.OnUserImageSetListener() {
                            @Override
                            public void onUserImageSet(UserImageUrlMap userImageUrlMap) {

                                user.setImage(userImageUrlMap.getUserImageUrl());
                                user.setThumb_image(userImageUrlMap.getUserThumbImageUrl());

                                AddUserToDatabase addUserToDatabase = new AddUserToDatabase();
                                addUserToDatabase.AddUserToDatabaseWithUniqueUsername(RegisterActivity.this, user);
                                addUserToDatabase.setOnUserAddedToDatabaseListener(new AddUserToDatabase.OnUserAddedToDatabaseListener() {
                                    @Override
                                    public void OnUserAddedToDatabase() {
                                        friendsData.put("currentUserId", mAuth.getCurrentUser().getUid());
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
                                                    registerFinished(myProgressBar);
                                                } else {
                                                    showSnackbar(result);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } else {
                        myProgressBar.stopProgressBar();
                        Toast.makeText(RegisterActivity.this, "Failed Registration: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void registerFinished(MyProgressBar myProgressBar) {
        myProgressBar.stopProgressBar();
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
    }

    // when gallery intent has been opened and an image clicked start crop image activity
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
                mRegisterImageButton.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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

    @Override
    protected void onResume() {
        super.onResume();

        // check if maintenance
        MaintenanceViewModel maintenanceViewModel = ViewModelProviders.of(this).get(MaintenanceViewModel.class);
        LiveData<DataSnapshot> maintenanceLiveData = maintenanceViewModel.getDataSnapshotLiveData();
        maintenanceLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    if ((boolean) dataSnapshot.getValue()) {
                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            }
        });
    }
}
