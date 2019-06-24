package com.foxmike.android.utils;

import java.util.HashMap;

/**
 * Created by chris on 2019-02-01.
 */

public class Price {
    public static HashMap<String, Integer> PRICES_INTEGERS_SE = new HashMap<>();
    public static HashMap<Integer, String> PRICES_STRINGS_SE = new HashMap<>();

    public static HashMap<String, String> CURRENCIES = new HashMap<>();

    public static HashMap<String, HashMap<String, Integer>> PRICES_INTEGERS = new HashMap<>();
    public static HashMap<String, HashMap<Integer, String>> PRICES_STRINGS = new HashMap<>();

    static {
        PRICES_INTEGERS_SE.put("Min", 0);
        PRICES_INTEGERS_SE.put("Gratis", 0);
        PRICES_INTEGERS_SE.put("30 kr", 30);
        PRICES_INTEGERS_SE.put("40 kr", 40);
        PRICES_INTEGERS_SE.put("50 kr", 50);
        PRICES_INTEGERS_SE.put("60 kr", 60);
        PRICES_INTEGERS_SE.put("70 kr", 70);
        PRICES_INTEGERS_SE.put("80 kr", 80);
        PRICES_INTEGERS_SE.put("90 kr", 90);
        PRICES_INTEGERS_SE.put("100 kr", 100);
        PRICES_INTEGERS_SE.put("Max", 1000000);

        PRICES_STRINGS_SE.put(-1, "Min");
        PRICES_STRINGS_SE.put(0, "Gratis");
        PRICES_STRINGS_SE.put(30, "30 kr");
        PRICES_STRINGS_SE.put(40, "40 kr");
        PRICES_STRINGS_SE.put(50, "50 kr");
        PRICES_STRINGS_SE.put(60, "60 kr");
        PRICES_STRINGS_SE.put(70, "70 kr");
        PRICES_STRINGS_SE.put(80, "80 kr");
        PRICES_STRINGS_SE.put(90, "90 kr");
        PRICES_STRINGS_SE.put(100, "100 kr");
        PRICES_STRINGS_SE.put(1000000, "Max");

        CURRENCIES.put("sek", "kr");

        PRICES_INTEGERS.put("sek", PRICES_INTEGERS_SE);
        PRICES_STRINGS.put("sek", PRICES_STRINGS_SE);

    }


}
