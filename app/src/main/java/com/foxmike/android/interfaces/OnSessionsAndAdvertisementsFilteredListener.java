package com.foxmike.android.interfaces;

import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;

import java.util.ArrayList;

public interface OnSessionsAndAdvertisementsFilteredListener {

    void OnSessionsAndAdvertisementsFiltered(ArrayList<Session> sessions, ArrayList<Advertisement> advertisements);
}
