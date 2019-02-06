package com.foxmike.android.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.utils.CheckVersion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AboutActivity extends AppCompatActivity {

    private TextView versionTV;
    private String version;
    private long mLastAdminClickTime = 0;
    private long mLastAdminClickCounter = 0;

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
        CheckVersion.checkVersion(this);
    }
}
