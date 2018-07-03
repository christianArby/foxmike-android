package com.foxmike.android.interfaces;

import android.location.Location;
import com.foxmike.android.models.Session;
import java.util.ArrayList;

public interface OnNearSessionsFoundListener {

    void OnNearSessionsFound(ArrayList<Session> nearSessions, Location location);

    void OnLocationNotFound();

}
