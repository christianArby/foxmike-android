package com.foxmike.android.activities;
//Checked
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupAccountActivity extends AppCompatActivity {

    private ImageButton mSetupImageButton;
    private EditText mNameField;
    private Uri mImageUri = null;
    private static final int GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private ProgressBar progressBar;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);

        Button mSubmitBtn;

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");

        mSetupImageButton = findViewById(R.id.setupImageButton);
        mNameField = findViewById(R.id.setupNameField);
        mSubmitBtn = findViewById(R.id.setupSubmitBtn);
        progressBar = findViewById(R.id.progressBar_cyclic);

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
        final String name = mNameField.getText().toString().trim();

        currentUserID = mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name) && mImageUri != null){

            mDatabaseUsers.child(currentUserID).child("name").setValue(name);
            SetOrUpdateUserImage setOrUpdateUserImage = new SetOrUpdateUserImage();
            setOrUpdateUserImage.setOnUserImageSetListener(new SetOrUpdateUserImage.OnUserImageSetListener() {
                @Override
                public void onUserImageSet() {
                    myProgressBar.stopProgressBar();
                    Intent mainIntent = new Intent(SetupAccountActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });
            setOrUpdateUserImage.setOrUpdateUserImages(SetupAccountActivity.this,mImageUri,currentUserID);
        }
    }
    // when gallery intent has been opened and an image clicked start crop image activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(2,1)
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
}
