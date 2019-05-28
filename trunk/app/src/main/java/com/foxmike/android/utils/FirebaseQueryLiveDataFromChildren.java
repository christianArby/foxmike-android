package com.foxmike.android.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by chris on 2019-03-22.
 */

public class FirebaseQueryLiveDataFromChildren extends LiveData<DataSnapshot> {
    private static final String LOG_TAG = "FirebaseQueryLiveData";

    private final Query query;
    private final MyChildEventListener listener = new MyChildEventListener();

    public FirebaseQueryLiveDataFromChildren(Query query) {
        this.query = query;
    }

    public FirebaseQueryLiveDataFromChildren(DatabaseReference ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
        Log.d(LOG_TAG, "onActive");
        query.addChildEventListener(listener);
    }

    @Override
    protected void onInactive() {
        Log.d(LOG_TAG, "onInactive");
        query.removeEventListener(listener);
    }

    private class MyChildEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            setValue(dataSnapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(LOG_TAG, "Can't listen to query " + query, databaseError.toException());
        }
    }
}
