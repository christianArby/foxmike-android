package com.foxmike.android.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.Random;

/**
 * Created by chris on 2018-07-01.
 */

public class AddUserToDatabase {

    private FirebaseAuth mAuth;
    private int numberOfTriedUserNames;
    private int startRangeCeiling = 10000;
    private DatabaseReference mDatabase;
    private int randomPIN;
    private String PINString;
    private String userName;

    private OnUserAddedToDatabaseListener onUserAddedToDatabaseListener;

    public AddUserToDatabase() {
        this.numberOfTriedUserNames = 0;
    }

    // Add user to Firabase database
    public void AddUserToDatabaseWithUniqueUsername(final Activity activity, final User user) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Random random = new Random();
        randomPIN = random.nextInt(startRangeCeiling);
        PINString = String.valueOf(randomPIN);
        if (this.numberOfTriedUserNames>0) {
            userName = "@"+user.getFirstName().toLowerCase()+user.getLastName().toLowerCase()+PINString;
        } else {
            userName = "@"+user.getFirstName().toLowerCase()+user.getLastName().toLowerCase();
        }
        //add the user
        mDatabase.child("usernames").child(userName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(mAuth.getCurrentUser().getUid());
                    return Transaction.success(mutableData);
                }

                return Transaction.abort();
            }
            @Override
            public void onComplete(DatabaseError firebaseError, boolean commited, DataSnapshot dataSnapshot) {
                if (commited) {
                    user.setUserName(userName);
                    user.setUserId(mAuth.getCurrentUser().getUid());

                    mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            onUserAddedToDatabaseListener.OnUserAddedToDatabase();
                        }
                    });
                } else {
                    Toast.makeText(activity, R.string.username_exists_text, Toast.LENGTH_SHORT).show();
                    AddUserToDatabase.this.numberOfTriedUserNames++;
                    if (AddUserToDatabase.this.numberOfTriedUserNames>2) {
                        startRangeCeiling = startRangeCeiling*10;
                    }
                    if (AddUserToDatabase.this.numberOfTriedUserNames<10) {
                        AddUserToDatabaseWithUniqueUsername(activity, user);
                    } else {
                        Toast.makeText(activity, R.string.failed_registration_text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // ----------------------------------- END
    }

    public void setOnUserAddedToDatabaseListener(OnUserAddedToDatabaseListener onUserAddedToDatabaseListener) {
        this.onUserAddedToDatabaseListener = onUserAddedToDatabaseListener;
    }

    public interface OnUserAddedToDatabaseListener {
        void OnUserAddedToDatabase();
    }




}
