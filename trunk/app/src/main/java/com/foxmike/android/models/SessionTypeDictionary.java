package com.foxmike.android.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 2019-05-27.
 */

public class SessionTypeDictionary extends HashMap<String,String>{

    private String unknown;

    public SessionTypeDictionary(int initialCapacity, float loadFactor, String unknown) {
        super(initialCapacity, loadFactor);
        this.unknown = unknown;
    }

    public SessionTypeDictionary(int initialCapacity, String unknown) {
        super(initialCapacity);
        this.unknown = unknown;
    }

    public SessionTypeDictionary(String unknown) {
        this.unknown = unknown;
    }

    public SessionTypeDictionary(Map<? extends String, ? extends String> m, String unknown) {
        super(m);
        this.unknown = unknown;
    }

    @Override
    public String get(Object key) {
        if (super.get(key)==null) {
            return unknown;
        } else {
            return super.get(key);
        }
    }
}
