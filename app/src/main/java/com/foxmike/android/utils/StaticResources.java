package com.foxmike.android.utils;

import java.util.HashMap;

/**
 * Created by chris on 2019-04-07.
 */

public class StaticResources {
    public static HashMap<String, Integer> DURATION_INTEGERS = new HashMap<>();
    public static HashMap<Integer, String> DURATION_STRINGS = new HashMap<>();
    public static HashMap<String, String> FILTER_SESSION_TYPE_STRINGS = new HashMap<>();

    public static int minDefaultHour = 4;
    public static int minDefaultMinute = 0;
    public static int maxDefaultHour = 23;
    public static int maxDefaultMinute = 45;

    public static HashMap<String, Integer> DEPOSITION_AMOUNT_INTEGERS = new HashMap<>();

    static {
        DURATION_INTEGERS.put("10 min", 10);
        DURATION_INTEGERS.put("20 min", 20);
        DURATION_INTEGERS.put("30 min", 30);
        DURATION_INTEGERS.put("40 min", 40);
        DURATION_INTEGERS.put("50 min", 50);
        DURATION_INTEGERS.put("60 min", 60);
        DURATION_INTEGERS.put("70 min", 70);
        DURATION_INTEGERS.put("80 min", 80);
        DURATION_INTEGERS.put("90 min", 90);
        DURATION_INTEGERS.put("100 min", 100);
        DURATION_INTEGERS.put("110 min", 110);
        DURATION_INTEGERS.put("120 min", 120);

        DURATION_STRINGS.put(10 , "10 min");
        DURATION_STRINGS.put(20, "20 min");
        DURATION_STRINGS.put(30, "30 min");
        DURATION_STRINGS.put(40, "40 min");
        DURATION_STRINGS.put(50, "50 min");
        DURATION_STRINGS.put(60, "60 min");
        DURATION_STRINGS.put(70, "70 min");
        DURATION_STRINGS.put(80, "80 min");
        DURATION_STRINGS.put(90, "90 min");
        DURATION_STRINGS.put(100, "100 min");
        DURATION_STRINGS.put(110, "110 min");
        DURATION_STRINGS.put(120, "120 min");

        DEPOSITION_AMOUNT_INTEGERS.put("SE", 200);

        FILTER_SESSION_TYPE_STRINGS.put("Running", "AAA");
        FILTER_SESSION_TYPE_STRINGS.put("Yoga", "BBB");
        FILTER_SESSION_TYPE_STRINGS.put("Crossfit", "CCC");
        FILTER_SESSION_TYPE_STRINGS.put("Strength", "DDD");
        FILTER_SESSION_TYPE_STRINGS.put("Cardio", "EEE");
        FILTER_SESSION_TYPE_STRINGS.put("Ballsport", "FFF");

    }
}
