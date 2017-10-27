package com.example.chris.kungsbrostrand;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by chris on 2017-10-21.
 */

public interface OnSessionsFilteredListener {

    void OnSessionsFiltered(ArrayList<Session> sessions, Location location);
}
