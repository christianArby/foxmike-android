package com.foxmike.android.activities;
//Checked

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.AddUserToDatabase;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
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
    private TextView imageErrorTextTV;
    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference rootDbRef;
    private ProgressBar progressBar;
    private String currentUserID;
    private Uri mImageUri = null;
    private String userName;

    private int randomPIN;
    private String PINString;
    private int numberOfTriedUserNames;
    private int startRangeCeiling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_register);

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

        imageErrorTextTV.setText(null);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        rootDbRef = FirebaseDatabase.getInstance().getReference();

        // Setup image button to choose image from gallery
        mRegisterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        final MyProgressBar myProgressBar = new MyProgressBar(progressBar,this);
        // get input
        final String firstName = mFirstNameField.getText().toString().trim();
        final String lastName = mLastNameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

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
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Register success,  create random user name and save user in Realtime database
                        currentUserID = mAuth.getCurrentUser().getUid();
                        // ----------------------------------- NEW -------------------------

                        final User user = new User();
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setFullName(firstName + " " + lastName);

                        final UserPublic userPublic = new UserPublic(firstName,lastName,"");

                        SetOrUpdateUserImage setOrUpdateUserImage = new SetOrUpdateUserImage();
                        setOrUpdateUserImage.setOrUpdateUserImages(RegisterActivity.this,mImageUri,currentUserID);
                        setOrUpdateUserImage.setOnUserImageSetListener(new SetOrUpdateUserImage.OnUserImageSetListener() {
                            @Override
                            public void onUserImageSet(Map imageUrlHashMap) {

                                user.setImage(imageUrlHashMap.get("image").toString());
                                user.setThumb_image(imageUrlHashMap.get("thumb_image").toString());

                                AddUserToDatabase addUserToDatabase = new AddUserToDatabase();
                                addUserToDatabase.AddUserToDatabaseWithUniqueUsername(RegisterActivity.this, user, userPublic);
                                addUserToDatabase.setOnUserAddedToDatabaseListener(new AddUserToDatabase.OnUserAddedToDatabaseListener() {
                                    @Override
                                    public void OnUserAddedToDatabase() {
                                        rootDbRef.child("foxmikeUID").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue()!=null) {
                                                    String foxmikeUID = dataSnapshot.getValue().toString();
                                                    if (!currentUserID.equals(foxmikeUID)) {
                                                        final String currentDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                                                        Map friendsMap = new HashMap();
                                                        friendsMap.put("friends/" + currentUserID + "/" + foxmikeUID + "/date", currentDate);
                                                        friendsMap.put("friends/" + foxmikeUID + "/" + currentUserID + "/date", currentDate);
                                                        FirebaseDatabase.getInstance().getReference().updateChildren(friendsMap, (databaseError, databaseReference) -> {
                                                            registerFinished(myProgressBar);
                                                        });
                                                    } else {
                                                        registerFinished(myProgressBar);
                                                    }
                                                } else {
                                                    registerFinished(myProgressBar);
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
                    } else {
                        myProgressBar.stopProgressBar();
                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        Toast.makeText(RegisterActivity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
