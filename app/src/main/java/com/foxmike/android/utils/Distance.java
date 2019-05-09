package com.foxmike.android.utils;

import java.util.HashMap;

/**
 * Created by chris on 2019-02-01.
 */

public class Distance {
    public static HashMap<String, Integer> DISTANCE_INTEGERS_SE = new HashMap<>();
    public static HashMap<Integer, String> DISTANCE_STRINGS_SE = new HashMap<>();

    static {
        DISTANCE_INTEGERS_SE.put("Min", 0);
        DISTANCE_INTEGERS_SE.put("1 km", 1);
        DISTANCE_INTEGERS_SE.put("3 km", 3);
        DISTANCE_INTEGERS_SE.put("8 km", 8);
        DISTANCE_INTEGERS_SE.put("16 km", 16);
        DISTANCE_INTEGERS_SE.put("4 mil", 40);
        DISTANCE_INTEGERS_SE.put("6 mil", 60);
        DISTANCE_INTEGERS_SE.put("100 mil", 1000);
        DISTANCE_INTEGERS_SE.put("Max", 10000);

        DISTANCE_STRINGS_SE.put(0, "Min");
        DISTANCE_STRINGS_SE.put(1, "1 km");
        DISTANCE_STRINGS_SE.put(3, "3 km");
        DISTANCE_STRINGS_SE.put(8, "8 km");
        DISTANCE_STRINGS_SE.put(16, "16 km");
        DISTANCE_STRINGS_SE.put(40, "4 mil");
        DISTANCE_STRINGS_SE.put(60, "6 mil");
        DISTANCE_STRINGS_SE.put(1000, "100 mil");
        DISTANCE_STRINGS_SE.put(10000, "Max");
    }
}
