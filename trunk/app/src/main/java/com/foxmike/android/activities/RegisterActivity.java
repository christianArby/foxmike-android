package com.foxmike.android.activities;
//Checked
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.foxmike.android.R;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.foxmike.android.R.layout.activity_register;

public class RegisterActivity extends AppCompatActivity {

    private EditText mFirstNameField;
    private EditText mLastNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private CircleImageView mRegisterImageButton;
    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference rootDbRef;
    private StorageReference mStorageImage;
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

        Button mRegisterBtn;

        progressBar = findViewById(R.id.progressBar_cyclic);
        mFirstNameField = findViewById(R.id.setupFirstNameField);
        mLastNameField = findViewById(R.id.setupLastNameField);
        mEmailField= findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mRegisterImageButton = findViewById(R.id.registerImageBtn);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        rootDbRef = FirebaseDatabase.getInstance().getReference();
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        // Setup image button to choose image from gallery
        mRegisterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }

    /* Function to register user in auth and database */
    private void startRegister() {

        final MyProgressBar myProgressBar = new MyProgressBar(progressBar,this);
        // get input
        final String firstName = mFirstNameField.getText().toString().trim();
        final String lastName = mLastNameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();



        // if all input has been filled in create user
        if(!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && mImageUri != null){

            myProgressBar.startProgressBar();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        // Sign in success,  update Realtime Db with the signed-in user's information, when finished start MainActivity

                        currentUserID = mAuth.getCurrentUser().getUid();

                        // ----------------------------------- NEW
                        numberOfTriedUserNames =0;
                        startRangeCeiling = 10000;
                        addUserWithRandomUserName();

                    } else {
                        myProgressBar.stopProgressBar();
                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        Toast.makeText(RegisterActivity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }

                private void addUserWithRandomUserName() {
                    Random random = new Random();
                    randomPIN = random.nextInt(startRangeCeiling);
                    PINString = String.valueOf(randomPIN);
                    if (numberOfTriedUserNames>0) {
                        userName = "@"+firstName.toLowerCase()+lastName.toLowerCase()+PINString;
                    } else {
                        userName = "@"+firstName.toLowerCase()+lastName.toLowerCase();
                    }

                    //add the user
                    rootDbRef.child("usernames").child(userName).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(currentUserID);
                                return Transaction.success(mutableData);
                            }

                            return Transaction.abort();
                        }

                        @Override
                        public void onComplete(DatabaseError firebaseError, boolean commited, DataSnapshot dataSnapshot) {
                            if (commited) {
                                mDatabaseUsers.child(currentUserID).child("userName").setValue(userName);
                                mDatabaseUsers.child(currentUserID).child("firstName").setValue(firstName);
                                mDatabaseUsers.child(currentUserID).child("lastName").setValue(lastName);
                                mDatabaseUsers.child(currentUserID).child("fullName").setValue(firstName + " " + lastName);
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                mDatabaseUsers.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        SetOrUpdateUserImage setOrUpdateUserImage = new SetOrUpdateUserImage();
                                        setOrUpdateUserImage.setOnUserImageSetListener(new SetOrUpdateUserImage.OnUserImageSetListener() {
                                            @Override
                                            public void onUserImageSet() {
                                                myProgressBar.stopProgressBar();
                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(mainIntent);
                                            }
                                        });
                                        setOrUpdateUserImage.setOrUpdateUserImages(RegisterActivity.this,mImageUri,currentUserID);
                                    }
                                });

                            } else {
                                Toast.makeText(RegisterActivity.this, "Username exists", Toast.LENGTH_SHORT).show();
                                numberOfTriedUserNames++;
                                if (numberOfTriedUserNames>2) {
                                    startRangeCeiling = startRangeCeiling*10;
                                }
                                if (numberOfTriedUserNames<10) {
                                    addUserWithRandomUserName();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Failed Registration", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    // ----------------------------------- NEW END
                }

            });
        }
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
