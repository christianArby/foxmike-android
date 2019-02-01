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
        DISTANCE_INTEGERS_SE.put("3 km", 3);
        DISTANCE_INTEGERS_SE.put("8 km", 8);
        DISTANCE_INTEGERS_SE.put("16 km", 16);
        DISTANCE_INTEGERS_SE.put("40 km", 40);
        DISTANCE_INTEGERS_SE.put("80 km", 80);
        DISTANCE_INTEGERS_SE.put("1000 km", 1000);
        DISTANCE_INTEGERS_SE.put("Max", 1000000);

        DISTANCE_STRINGS_SE.put(0, "Min");
        DISTANCE_STRINGS_SE.put(3, "3 km");
        DISTANCE_STRINGS_SE.put(8, "8 km");
        DISTANCE_STRINGS_SE.put(16, "16 km");
        DISTANCE_STRINGS_SE.put(40, "40 km");
        DISTANCE_STRINGS_SE.put(80, "80 km");
        DISTANCE_STRINGS_SE.put(1000, "1000 km");
        DISTANCE_STRINGS_SE.put(1000000, "Max");
    }
}
