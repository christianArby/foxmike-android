package com.foxmike.android.activities;
//Checked
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.fragments.PasswordResetFragment;
import com.foxmike.android.utils.MyProgressBar;
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

    private TextInputEditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private TextView resetText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private String currentUserID;
    private TextInputLayout emailTIL;
    private static final String TAG = "EmailPassword";
    private ProgressBar progressBar;
    private MyProgressBar myProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(getResources().getColor(R.color.foxmikePrimaryColor));

        FloatingActionButton mLoginBtn;

        mLoginEmailField = findViewById(R.id.loginEmailField);
        mLoginPasswordField = findViewById(R.id.loginPasswordField);
        mLoginBtn = findViewById(R.id.loginBtn);
        resetText = findViewById(R.id.resetText);
        emailTIL = (TextInputLayout) findViewById(R.id.emailTIL);
        progressBar = findViewById(R.id.progressBar_cyclic);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);

        myProgressBar = new MyProgressBar(progressBar,this);

        resetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mLoginEmailField.getText().toString().trim();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                PasswordResetFragment passwordResetFragment = PasswordResetFragment.newInstance(email);
                passwordResetFragment.show(transaction,"passwordResetFragment");
            }
        });


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        mLoginEmailField.addTextChangedListener(new TextWatcher() {
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

    private void checkLogin() {

        String email = mLoginEmailField.getText().toString().trim();
        String password = mLoginPasswordField.getText().toString().trim();
        if (TextUtils.isEmpty(email) | TextUtils.isEmpty(password)) {
            emailTIL.setError(getString(R.string.please_fill_in_email_and_password_text));
        }

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            myProgressBar.startProgressBar();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        myProgressBar.stopProgressBar();
                        currentUserID = mAuth.getCurrentUser().getUid();
                        checkIfUserExistsInDb();
                    } else {
                        myProgressBar.stopProgressBar();
                        emailTIL.setError(getString(R.string.email_or_password_incorrect_text));
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
                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                } else {
                    Intent setupIntent = new Intent(LoginActivity.this,SetupAccountActivity.class);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setupIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
