package com.foxmike.android.models;

/**
 * Created by chris on 2018-07-08.
 */

public class StudioBranch {
    private String studioID;
    private Studio studio;

    public StudioBranch(String studioID, Studio studio) {
        this.studioID = studioID;
        this.studio = studio;
    }

    public StudioBranch() {
    }

    public String getStudioID() {
        return studioID;
    }

    public void setStudioID(String studioID) {
        this.studioID = studioID;
    }

    public Studio getStudio() {
        return studio;
    }

    public void setStudio(Studio studio) {
        this.studio = studio;
    }
}
