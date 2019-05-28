package com.foxmike.android.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.foxmike.android.utils.FirebaseQueryLiveData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chris on 2019-03-22.
 */

public class MaintenanceViewModel extends ViewModel {
    private static final DatabaseReference REF =
            FirebaseDatabase.getInstance().getReference().child("maintenance");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(REF);

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }
}