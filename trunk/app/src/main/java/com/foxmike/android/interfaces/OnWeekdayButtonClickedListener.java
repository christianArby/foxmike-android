package com.foxmike.android.interfaces;

import java.util.HashMap;

public interface OnWeekdayButtonClickedListener {
    void OnWeekdayButtonClicked(int week, int button, HashMap<Integer,Boolean> toggleHashMap);
}
