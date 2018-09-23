package com.foxmike.android.interfaces;

public interface OnSessionClickedListener {
    void OnSessionClicked(String sessionId);
    void OnSessionClicked(String sessionId, Long representingAdTimestamp);
}
