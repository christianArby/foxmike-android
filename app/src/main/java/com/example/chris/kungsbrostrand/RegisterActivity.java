package com.example.chris.kungsbrostrand;

import android.app.ProgressDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static com.example.chris.kungsbrostrand.R.layout.activity_register;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private ImageButton mRegisterImageButton;

    private static final int GALLERY_REQUEST = 1;



    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageImage;

    private ProgressDialog mProgress;

    private Uri mImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_register);

        Button mRegisterBtn;

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");


        mProgress = new ProgressDialog(this);

        mNameField = findViewById(R.id.setupNameField);
        mEmailField= findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);
        mRegisterBtn = findViewById(R.id.registerBtn);


        mRegisterImageButton = findViewById(R.id.registerImageBtn);

        mRegisterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);


            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                startRegister();
            }
        });
    }

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && mImageUri != null){

            mProgress.setMessage("Signing up ...");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        final String user_id = mAuth.getCurrentUser().getUid();

                        final DatabaseReference current_user_db = mDatabase.child(user_id);

                        //current_user_db.child("name").setValue(name);
                        //current_user_db.child("image").setValue("default");

                        StorageReference filepath = mStorageImage.child(mImageUri.getLastPathSegment());

                        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                String downloadUri = taskSnapshot.getDownloadUrl().toString();

                                current_user_db.child("name").setValue(name);
                                current_user_db.child("image").setValue(downloadUri, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        mProgress.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainPlayerActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainIntent);

                                    }
                                });



                            }
                        });
                    }

                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(2,1)
                    .start(this);


        }

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
