package com.example.chris.kungsbrostrand;

import java.util.ArrayList;

/**
 * Created by chris on 2017-07-22.
 */

public interface OnPlayerSessionsContentReadyListener {
    void OnPlayerSessionsContentReady(ArrayList<Session> sessionsHostingOrAttending);
}
