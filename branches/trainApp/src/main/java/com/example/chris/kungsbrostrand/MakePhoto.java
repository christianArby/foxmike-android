/*
package com.example.chris.kungsbrostrand;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

*/
/**
 * Created by chris on 2017-05-26.
 *//*


class MakePhoto extends AppCompatActivity {
    protected ProgressDialog mProgressDialog;

    Uri photoURI;
    Double latitudeDouble;
    Double longitudeDouble;
    DatabaseReference mMarkerDbRef = FirebaseDatabase.getInstance().getReference().child("markers");
    private String mCurrentPhotoPath;
    private String photoName;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;

    public void setLatitudeDouble(Double latitudeDouble) {
        this.latitudeDouble = latitudeDouble;
    }

    public void setLongitudeDouble(Double longitudeDouble) {
        this.longitudeDouble = longitudeDouble;
    }

    // Take photo
    protected void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        ".com.example.chris.kungsbrostrand.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //CameraResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            mProgressDialog.setMessage("Uploading Image ...");
            mProgressDialog.show();

            // filepath and name for photo
            photoName = "Lat" + latitudeDouble.toString() + "Long" + longitudeDouble.toString();
            StorageReference filepath = mStorage.child("Photos").child(photoName); //Use random name if i dont want to override images
            // add photo to database with path from above
*/
/*            Uri uri = data.getData();

            File imagePath = new File(Context.getFilesDir(), "images");
            File newFile = new File(imagePath, "default_image.jpg");*//*



            filepath.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgressDialog.dismiss();
                    // fit the image in imageview
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    Picasso.with(MakePhoto.this).load(downloadUri).fit().centerCrop().into(mImageview);
                    // Add firebase marker to Firebase realtime database
                    String downloadURL = taskSnapshot.getMetadata().getDownloadUrl().toString();
                    FirebaseMarker marker = new FirebaseMarker(downloadURL,latitudeDouble,longitudeDouble);
                    mMarkerDbRef.push().setValue(marker);
                    //
                    Toast.makeText(MakePhoto.this, "Uploading Finished ...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Create image file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  */
/* prefix *//*

                ".jpg",         */
/* suffix *//*

                storageDir      */
/* directory *//*

        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // method get photoURL from database
    protected void getPhotoURL(Double latitude, final Double longitude){
        // find latitude value in child in realtime database and fit in imageview
        mMarkerDbRef.orderByChild("latitude").equalTo(latitude).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FirebaseMarker markerResult = dataSnapshot.getValue(FirebaseMarker.class);
                if(markerResult.longitude==longitude) {
                    Picasso.with(MakePhoto.this).load(markerResult.photoURL).fit().centerCrop().into(mImageview);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
*/
