package com.foxmike.android.interfaces;

import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnSessionsAndAdvertisementsFilteredListener {

    void OnSessionsAndAdvertisementsFiltered(HashMap<String, Session> sessions, ArrayList<Advertisement> advertisements);
}
