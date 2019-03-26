package com.foxmike.android.viewmodels;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.foxmike.android.models.User;
import com.foxmike.android.utils.FirebaseQueryLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by chris on 2019-03-22.
 */

public class CurrentUserViewModel extends ViewModel {
    private static final DatabaseReference REF =
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(REF);

    private final LiveData<User> userLiveData =
            Transformations.map(liveData, new Deserializer());

    private class Deserializer implements Function<DataSnapshot, User> {
        @Override
        public User apply(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(User.class);
        }
    }

    @NonNull
    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }
}