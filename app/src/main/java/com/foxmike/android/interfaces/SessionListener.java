package com.foxmike.android.interfaces;

import com.foxmike.android.models.Session;

/**
 * Created by chris on 2018-09-22.
 */

public interface SessionListener {
    void OnEditSession(String sessionID);
    void OnEditSession(String sessionID , Session session);
    void OnCancelBookedSession(Long bookingTimestamp, Long advertisementTimestamp, String advertisementId, String participantId, String chargeId, String accountId);
    void OnBookSession(String sessionId, Long advertisementTimestamp, String hostId, String stripeCustomerId, int amount, String currency, boolean dontShowBookingText);
}
