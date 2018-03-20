package com.foxmike.android.interfaces;

import com.foxmike.android.models.SessionBranch;

import java.util.ArrayList;

/**
 * Created by chris on 2018-03-19.
 */

public interface OnSessionBranchesFoundListener {
    void OnSessionBranchesFound(ArrayList<SessionBranch> sessionBranches);
}
