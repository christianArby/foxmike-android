package com.foxmike.android.interfaces;

import android.location.Location;

import com.foxmike.android.models.Studio;

import java.util.ArrayList;

/**
 * Created by chris on 2018-07-13.
 */

public interface OnNearStudiosFoundListener {
    void OnNearStudiosFound(ArrayList<Studio> nearSessions, Location location);
}
