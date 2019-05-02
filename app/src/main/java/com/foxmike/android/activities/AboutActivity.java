package com.foxmike.android.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.viewmodels.MaintenanceViewModel;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
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

import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class AboutActivity extends AppCompatActivity {

    private TextView versionTV;
    private String version;
    private long mLastAdminClickTime = 0;
    private long mLastAdminClickCounter = 0;
    private DatabaseReference maintenanceRef;
    private ValueEventListener maintenanceListener;
    private TextView openSourceTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        version = "?";


        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        versionTV = findViewById(R.id.versionName);
        openSourceTV = findViewById(R.id.openSourceTV);

        openSourceTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the user selects an option to see the licenses:
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_notices));
                startActivity(new Intent(AboutActivity.this, OssLicensesMenuActivity.class));
            }
        });

        versionTV.setText(version);

        versionTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLastAdminClickCounter==8) {
                    FirebaseDatabase.getInstance().getReference().child("foxmikeUID").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(dataSnapshot.getValue())) {
                                Intent adminNot = new Intent(AboutActivity.this, WriteAdminNotification.class);
                                startActivity(adminNot);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                if (SystemClock.elapsedRealtime() - mLastAdminClickTime < 2000) {
                    mLastAdminClickCounter++;
                    return;
                }
                mLastAdminClickCounter = 0;
                mLastAdminClickTime = SystemClock.elapsedRealtime();
            }
        });
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLastAdminClickCounter==8) {
                    Toast.makeText(AboutActivity.this, "erasing account...", Toast.LENGTH_LONG).show();
                    eraseAccount(FirebaseAuth.getInstance().getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<HashMap<String, Object>>() {
                        @Override
                        public void onComplete(@NonNull Task<HashMap<String, Object>> task) {
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                Log.w(TAG, "retrieve:onFailure", e);
                                Toast.makeText(AboutActivity.this, getString(R.string.bad_internet), Toast.LENGTH_LONG).show();
                                return;
                            }
                            // If successful, extract
                            HashMap<String, Object> result = task.getResult();
                            if (result.get("operationResult").toString().equals("success")) {
                                Toast.makeText(AboutActivity.this, "Notification sent", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                HashMap<String, Object> error = (HashMap<String, Object>) result.get("err");
                                Toast.makeText(AboutActivity.this, error.get("message").toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                if (SystemClock.elapsedRealtime() - mLastAdminClickTime < 2000) {
                    mLastAdminClickCounter++;
                    return;
                }
                mLastAdminClickCounter = 0;
                mLastAdminClickTime = SystemClock.elapsedRealtime();
            }
        });
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
                        Intent welcomeIntent = new Intent(AboutActivity.this,WelcomeActivity.class);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(welcomeIntent);
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            }
        });
    }

    private Task<HashMap<String, Object>> eraseAccount(String userId) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("eraseUser")
                .call(userId)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }
}
