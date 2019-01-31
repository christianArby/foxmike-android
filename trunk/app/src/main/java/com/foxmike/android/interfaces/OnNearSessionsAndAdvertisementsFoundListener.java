package com.foxmike.android.interfaces;

import android.location.Location;

import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnNearSessionsAndAdvertisementsFoundListener {

    void OnNearSessionsFound(ArrayList<Session> nearSessions, HashMap<String,Advertisement> nearAdvertisements, Location location);

    void OnLocationNotFound();

}
