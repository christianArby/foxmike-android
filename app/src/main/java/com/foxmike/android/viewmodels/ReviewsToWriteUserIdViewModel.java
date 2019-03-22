package com.foxmike.android.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.foxmike.android.utils.FirebaseQueryLiveDataFromChildren;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chris on 2019-03-22.
 */

public class ReviewsToWriteUserIdViewModel extends ViewModel {
    private static final DatabaseReference REF =
            FirebaseDatabase.getInstance().getReference().child("reviewsToWrite").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    private final FirebaseQueryLiveDataFromChildren liveData = new FirebaseQueryLiveDataFromChildren(REF);

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}