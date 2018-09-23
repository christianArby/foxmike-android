package com.foxmike.android.interfaces;

import com.foxmike.android.models.Advertisement;

import java.util.ArrayList;

/**
 * Created by chris on 2018-09-13.
 */

public interface OnAdvertisementsFoundListener {
    void OnAdvertisementsFound(ArrayList<Advertisement> advertisements);
}
