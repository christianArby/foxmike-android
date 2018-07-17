package com.foxmike.android.interfaces;

import com.foxmike.android.models.Studio;
import com.foxmike.android.models.StudioBranch;

import java.util.ArrayList;

/**
 * Created by chris on 2018-07-08.
 */

public interface OnStudioBranchesFoundListener {
    void OnStudioBranchesFound(ArrayList<StudioBranch> studioBranches);
}
