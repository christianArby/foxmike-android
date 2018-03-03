package com.foxmike.androidapp.kungsbrostrand;

import android.app.Activity;

/**
 * Created by chris on 2017-10-21.
 */

public interface OnWeekdayChangedListener {
    void OnWeekdayChanged(int week, String weekdayKey, Boolean weekdayBoolean, Activity activity);
}
