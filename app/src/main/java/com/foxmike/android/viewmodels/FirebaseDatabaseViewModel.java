package com.foxmike.android.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.foxmike.android.utils.FirebaseQueryLiveData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

/**
 * Created by chris on 2019-03-22.
 */

public class FirebaseDatabaseViewModel extends ViewModel {
    private HashMap<DatabaseReference, LiveData<DataSnapshot>> mLiveDataMap = new HashMap<>();

    public LiveData<DataSnapshot> getDataSnapshotLiveData(DatabaseReference ref) {
        if (!mLiveDataMap.containsKey(ref)) {
            // We don't have an existing LiveData for this ref
            // so create a new one
            mLiveDataMap.put(ref, new FirebaseQueryLiveData(
                    ref));
        }
        return mLiveDataMap.get(ref);
    }
}