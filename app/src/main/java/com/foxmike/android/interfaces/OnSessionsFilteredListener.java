package com.foxmike.android.interfaces;

import com.foxmike.android.models.Session;
import java.util.ArrayList;

public interface OnSessionsFilteredListener {

    void OnSessionsFiltered(ArrayList<Session> sessions, ArrayList<ArrayList<Session>> nearSessionsArrays);
}
