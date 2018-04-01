package com.foxmike.android.models;

import java.util.Map;

/**
 * Created by chris on 2018-04-01.
 */

public class UserTest {

    private Map<String,User> userMap;

    public UserTest(Map<String, User> userMap) {
        this.userMap = userMap;
    }

    public UserTest() {
    }

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, User> userMap) {
        this.userMap = userMap;
    }
}
