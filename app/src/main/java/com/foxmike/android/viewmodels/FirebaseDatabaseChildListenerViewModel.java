package com.foxmike.android.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.foxmike.android.utils.FirebaseQueryLiveDataFromChildren;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

/**
 * Created by chris on 2019-04-01.
 */

public class FirebaseDatabaseChildListenerViewModel extends ViewModel {
    private HashMap<DatabaseReference, LiveData<DataSnapshot>> mLiveDataMap = new HashMap<>();

    public LiveData<DataSnapshot> getDataSnapshotLiveData(DatabaseReference ref) {
        if (!mLiveDataMap.containsKey(ref)) {
            // We don't have an existing LiveData for this ref
            // so create a new one
            mLiveDataMap.put(ref, new FirebaseQueryLiveDataFromChildren(
                    ref));
        }
        return mLiveDataMap.get(ref);
    }
}
