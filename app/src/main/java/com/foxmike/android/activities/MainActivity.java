package com.foxmike.android.activities;
//Checked

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    boolean locationPermission;
    private MyFirebaseDatabase myFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String deviceToken;
    private FirebaseUser currentUser;
    private DatabaseReference rootDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check location permissions (different functions depending on Android version)
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermission=true;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationPermission = checkLocationPermission();
        }
        // If user has location permission enabled check if user is logged in
        if (locationPermission) {
            checkUserStatus();
        }
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        locationPermission=true;
                        recreate();
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                    this.finishAffinity();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }

    public void checkUserStatus() {

        mAuth = FirebaseAuth.getInstance();
        deviceToken = FirebaseInstanceId.getInstance().getToken();
        currentUser = mAuth.getCurrentUser();
        rootDbRef = FirebaseDatabase.getInstance().getReference();

        if (currentUser !=null) {
            // User is signed in
            rootDbRef.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user==null) {
                        Intent welcomeIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(welcomeIntent);
                        finish();
                        /*Intent setupAccount = new Intent(MainActivity.this, SetupAccountActivity.class);
                        startActivity(setupAccount);*/
                    } else {
                        //-------------------------THE DOOR OPENS--------------------------------------//
                        if (user.trainerMode) {
                            rootDbRef.child("users").child(currentUser.getUid()).child("device_token").child(deviceToken).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent mainHost = new Intent(MainActivity.this, MainHostActivity.class);
                                    mainHost.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainHost);
                                }
                            });
                        } else {
                            rootDbRef.child("users").child(currentUser.getUid()).child("device_token").child(deviceToken).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent mainPlayer = new Intent(MainActivity.this, MainPlayerActivity.class);
                                    mainPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainPlayer);
                                }
                            });
                        }
                        //---------------------------------------------------------------//
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            /*myFirebaseDatabase = new MyFirebaseDatabase();
            myFirebaseDatabase.getCurrentUser(new OnUserFoundListener() {
                @Override
                public void OnUserFound(User user) {
                    if (user==null) {
                        Intent setupAccount = new Intent(MainActivity.this, SetupAccountActivity.class);
                        //mainPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(setupAccount);
                    } else {
                        //-------------------------THE DOOR OPENS--------------------------------------//
                        if (user.trainerMode) {
                            mDatabase.child("users").child(currentUser.getUid()).child("device_token").child(deviceToken).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent mainHost = new Intent(MainActivity.this, MainHostActivity.class);
                                    mainHost.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainHost);
                                }
                            });
                        } else {
                            mDatabase.child("users").child(currentUser.getUid()).child("device_token").child(deviceToken).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent mainPlayer = new Intent(MainActivity.this, MainPlayerActivity.class);
                                    mainPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainPlayer);
                                }
                            });
                        }
                        //---------------------------------------------------------------//
                    }
                }
            });*/

            /*//--------------------- Set presence of current user---------------------------------
            String currentUserID = mAuth.getCurrentUser().getUid();

            // since I can connect from multiple devices, we store each connection instance separately
            // any time that connectionsRef's value is null (i.e. has no children) I am offline
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myConnectionsRef = database.getReference("presence/"+currentUserID+"/connections");

            // stores the timestamp of my last disconnect (the last time I was seen online)
            final DatabaseReference lastOnlineRef = database.getReference("/presence/"+currentUserID+"/lastOnline");

            final DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        DatabaseReference con = myConnectionsRef.push();

                        // when this device disconnects, remove it
                        con.onDisconnect().removeValue();

                        // when I disconnect, update the last time I was seen online
                        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                        // add this device to my connections list
                        // this value could contain info about the device or a timestamp too
                        con.setValue(Boolean.TRUE);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Listener was cancelled at .info/connected");
                }
            });

            // ---------------------------------------------------------------------------------------*/

        } else {
            //User is signed out
            Intent welcomeIntent = new Intent(MainActivity.this, WelcomeActivity.class);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(welcomeIntent);
            finish();
        }

    }
}
