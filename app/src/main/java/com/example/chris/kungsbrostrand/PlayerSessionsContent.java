package com.example.chris.kungsbrostrand;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class verifies that data which should be visualized in PlayerSessionsFragment has been downloaded.
 * In order to populate the view in PlayerSessionsFragment in the correct order there is a need to get this verified.
 * When all the data has been downloaded it is sent to the listener PlayerSessionsContentReadyListener.
 */

class PlayerSessionsContent {

    private HashMap<String,Boolean> userActivityContent;
    private ArrayList<Session> sessionsAttending = new ArrayList<Session>();

    public void getPlayerSessionsContent(final OnPlayerSessionsContentReadyListener onPlayerSessionsContentReadyListener){
        /* Create hashmap to later be able to check if all content has been downloaded from database */
        userActivityContent = new HashMap<String,Boolean>();
        userActivityContent.put("allSessionsAttendingFound",false);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        /* Get the currents user's information from the database */
        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not attending any sessions set that the sessionsAttending content has beeen found*/
                if (user.sessionsAttending.size()==0){
                    userActivityContent.put("allSessionsAttendingFound",true);
                    /*If all content has been found or downloaded from database send the data to the listener OnPlayerSessionsContentReady */
                    if (testIfAllContentFound()){
                        onPlayerSessionsContentReadyListener.OnPlayerSessionsContentReady(sessionsAttending);
                    }
                }

                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        sessionsAttending = sessions;
                        userActivityContent.put("allSessionsAttendingFound",true);
                        if (testIfAllContentFound()){
                            onPlayerSessionsContentReadyListener.OnPlayerSessionsContentReady(sessionsAttending);
                        }
                    }
                },user.sessionsAttending);

            }
        });
    }

    private boolean testIfAllContentFound(){
        int n = 0;

        for (HashMap.Entry<String, Boolean> entry : this.userActivityContent.entrySet())
        {
            if (entry.getValue()){
                n++;
            }
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }

        return n == userActivityContent.size();
    }
}


// OLD CLASS which collected both host and player sessions
/*class PlayerSessionsContent {

    private HashMap<String,Boolean> userActivityContent;
    private ArrayList<Session> sessionsAttending = new ArrayList<Session>();
    private ArrayList<Session> sessionsHosting = new ArrayList<Session>();

    public void getPlayerSessionsContent(final OnPlayerSessionsContentReadyListener onPlayerSessionsContentReadyListener){
        *//* Create hashmap to later be able to check if all content has been downloaded from database *//*
        userActivityContent = new HashMap<String,Boolean>();
        userActivityContent.put("allSessionsAttendingFound",false);
        userActivityContent.put("allSessionsHostingFound",false);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        *//* Get the currents user's information from the database *//*
        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                *//* If user is not attending any sessions set that the sessionsAttending content has beeen found*//*
                if (user.sessionsAttending.size()==0){
                    userActivityContent.put("allSessionsAttendingFound",true);
                    *//*If all content has been found or downloaded from database send the data to the listener OnPlayerSessionsContentReady *//*
                    if (testIfAllContentFound()){
                        onPlayerSessionsContentReadyListener.OnPlayerSessionsContentReady(sessionsAttending,sessionsHosting,user.name,user.image);
                    }
                }

                if (user.sessionsHosting.size()==0){
                    userActivityContent.put("allSessionsHostingFound",true);
                    if (testIfAllContentFound()){
                        onPlayerSessionsContentReadyListener.OnPlayerSessionsContentReady(sessionsAttending,sessionsHosting,user.name,user.image);
                    }
                }

                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        sessionsAttending = sessions;
                        userActivityContent.put("allSessionsAttendingFound",true);
                        if (testIfAllContentFound()){
                            onPlayerSessionsContentReadyListener.OnPlayerSessionsContentReady(sessionsAttending,sessionsHosting,user.name,user.image);
                        }
                    }
                },user.sessionsAttending);


                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        sessionsHosting = sessions;
                        userActivityContent.put("allSessionsHostingFound",true);
                        if (testIfAllContentFound()){
                            onPlayerSessionsContentReadyListener.OnPlayerSessionsContentReady(sessionsAttending,sessionsHosting,user.name,user.image);
                        }
                    }
                },user.sessionsHosting);

            }
        });
    }

    private boolean testIfAllContentFound(){
        int n = 0;

        for (HashMap.Entry<String, Boolean> entry : this.userActivityContent.entrySet())
        {
            if (entry.getValue()){
                n++;
            }
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }

        return n == userActivityContent.size();
    }
}*/
