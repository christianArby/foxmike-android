package com.foxmike.android.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SwitchModeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        setContentView(R.layout.activity_switch_mode);

        TextView switchText = findViewById(R.id.switchText);

        boolean trainerMode =  getIntent().getBooleanExtra("trainerMode", false);

        if (trainerMode) {
            switchText.setText(R.string.switching_to_participant_mode);
        } else {
            switchText.setText(R.string.switching_to_trainer_mode);
        }

        mAuth = FirebaseAuth.getInstance();
        usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");


        final DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
                User user = dataSnapshot.getValue(User.class);
                if (user.trainerMode) {
                    usersDbRef.child(mAuth.getCurrentUser().getUid()).child("trainerMode").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(SwitchModeActivity.this, MainPlayerActivity.class);
                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SwitchModeActivity.this).toBundle());
                        }
                    });
                } else {
                    usersDbRef.child(mAuth.getCurrentUser().getUid()).child("trainerMode").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(SwitchModeActivity.this, MainHostActivity.class);
                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SwitchModeActivity.this).toBundle());
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
