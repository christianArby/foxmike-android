package com.foxmike.android.activities;
//Checked
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.foxmike.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabaseUsers;
    private String deviceToken;
    private String currentUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FloatingActionButton mLoginBtn;

        mLoginEmailField = findViewById(R.id.loginEmailField);
        mLoginPasswordField = findViewById(R.id.loginPasswordField);
        mLoginBtn = findViewById(R.id.loginBtn);

        mAuth = FirebaseAuth.getInstance();
        deviceToken = FirebaseInstanceId.getInstance().getToken();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);

        mProgress = new ProgressDialog(this);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkLogin();
            }
        });

    }

    private void checkLogin() {

        String email = mLoginEmailField.getText().toString().trim();
        String password = mLoginPasswordField.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgress.setMessage("Checking Login ...");
            mProgress.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mProgress.dismiss();
                        currentUserID = mAuth.getCurrentUser().getUid();
                        checkIfUserExistsInDb();
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "Error Login", Toast.LENGTH_LONG).show();
                    }
                }
            });


        }

    }

    private void checkIfUserExistsInDb() {

        mDatabaseUsers.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDatabaseUsers.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                        }
                    });

                } else {
                    mDatabaseUsers.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent setupIntent = new Intent(LoginActivity.this,SetupAccountActivity.class);
                            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(setupIntent);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}