package com.foxmike.android.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.foxmike.android.utils.FirebaseQueryLiveData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

/**
 * Created by chris on 2019-03-22.
 */

public class FirebaseDatabaseViewModel extends AndroidViewModel {
    private HashMap<DatabaseReference, LiveData<DataSnapshot>> mLiveDataMap = new HashMap<>();

    public FirebaseDatabaseViewModel(@NonNull final Application application) {
        super(application);
    }

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