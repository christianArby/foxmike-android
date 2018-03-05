package com.foxmike.android.interfaces;

import android.location.Location;

import com.foxmike.android.models.Session;

import java.util.ArrayList;

/**
 * Created by chris on 2018-02-14.
 */

public interface OnNearSessionsFoundListener {

    void OnNearSessionsFound(ArrayList<Session> nearSessions, Location location);
}
