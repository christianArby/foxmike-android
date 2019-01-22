package com.foxmike.android.interfaces;

import java.util.HashMap;

/**
 * Created by chris on 2018-09-27.
 */

public interface AdvertisementListener {
    void OnCancelAdvertisement(String advertisementName, String advertisementId, String imageUrl,String sessionId, Long advertisementTimestamp, HashMap<String, String> participantsIds, String accountId);
}
