package com.foxmike.android.viewmodels;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.foxmike.android.models.Session;
import com.foxmike.android.utils.FirebaseQueryLiveData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chris on 2019-03-22.
 */

public class SessionViewModel extends ViewModel {

    private class Deserializer implements Function<DataSnapshot, Session> {
        @Override
        public Session apply(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(Session.class);
        }
    }

    @NonNull
    public LiveData<Session> getSessionLiveData(String sessionId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("sessions").child(sessionId);
        FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(ref);
        LiveData<Session> sessionLiveData = Transformations.map(liveData, new Deserializer());
        return sessionLiveData;
    }
}
