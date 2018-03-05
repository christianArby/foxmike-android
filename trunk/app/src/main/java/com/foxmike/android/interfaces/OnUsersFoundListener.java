package com.foxmike.android.interfaces;

import com.foxmike.android.models.User;

import java.util.ArrayList;

/**
 * Created by chris on 2017-11-16.
 */

public interface OnUsersFoundListener {

    void OnUsersFound(ArrayList<User> participants);
}
