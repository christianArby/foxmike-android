package com.example.chris.kungsbrostrand;

import java.util.ArrayList;

/**
 * Created by chris on 2017-07-22.
 */

public interface OnUserActivityContentReadyListener {
    void OnUserActivityContentReady(ArrayList<Session> sessionsAttending, ArrayList<Session> sessionsHosting, String userProfileName, String userProfileImageURL);
}
