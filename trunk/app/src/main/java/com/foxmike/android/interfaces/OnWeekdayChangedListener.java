package com.foxmike.android.interfaces;

import android.app.Activity;

public interface OnWeekdayChangedListener {
    void OnWeekdayChanged(int week, String weekdayKey, Boolean weekdayBoolean, Activity activity);
}
