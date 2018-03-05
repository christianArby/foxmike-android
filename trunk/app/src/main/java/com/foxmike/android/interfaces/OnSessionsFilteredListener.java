package com.foxmike.android.interfaces;

import com.foxmike.android.models.Session;

import java.util.ArrayList;

/**
 * Created by chris on 2017-10-21.
 */

public interface OnSessionsFilteredListener {

    void OnSessionsFiltered(ArrayList<Session> sessions);
}
