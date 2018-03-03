package com.foxmike.androidapp.kungsbrostrand;

import java.util.HashMap;

/**
 * Created by chris on 2017-10-25.
 */

public interface OnWeekdayButtonClickedListener {
    void OnWeekdayButtonClicked(int week, int button, HashMap<Integer,Boolean> toggleHashMap);
}
