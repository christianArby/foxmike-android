package com.foxmike.android.interfaces;

import com.foxmike.android.models.Session;

import java.util.ArrayList;

/**
 * Created by chris on 2017-07-20.
 */

public interface OnSessionsFoundListener {
    void OnSessionsFound(ArrayList<Session> sessions);
}
