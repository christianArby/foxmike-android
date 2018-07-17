package com.foxmike.android.interfaces;

public interface OnSessionClickedListener {
    void OnSessionClicked(double sessionLatitude, double sessionLongitude);
    void OnSessionClicked(String sessionId);
}
