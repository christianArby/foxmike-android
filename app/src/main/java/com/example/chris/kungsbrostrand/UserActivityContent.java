package com.example.chris.kungsbrostrand;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2017-07-22.
 */

class UserActivityContent {

    private HashMap<String,Boolean> userActivityContent;

    private ArrayList<Session> sessionsAttending = new ArrayList<Session>();
    private ArrayList<Session> sessionsHosting = new ArrayList<Session>();
    User user;

    public void getUserActivityContent(final OnUserActivityContentListener onUserActivityContentListener){

        userActivityContent = new HashMap<String,Boolean>();
        userActivityContent.put("sessionsAttending",false);
        userActivityContent.put("sessionsHosting",false);


        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {

                if (user.sessionsAttending.size()==0){
                    userActivityContent.put("sessionsAttending",true);
                    if (testContent()){
                        onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting,user.name,user.image);
                    }
                }

                if (user.sessionsHosting.size()==0){
                    userActivityContent.put("sessionsHosting",true);
                    if (testContent()){
                        onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting,user.name,user.image);
                    }
                }

                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        sessionsAttending = sessions;
                        userActivityContent.put("sessionsAttending",true);
                        if (testContent()){
                            onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting,user.name,user.image);
                        }
                    }
                },user.sessionsAttending);


                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        sessionsHosting = sessions;
                        userActivityContent.put("sessionsHosting",true);
                        if (testContent()){
                            onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting,user.name,user.image);
                        }
                    }
                },user.sessionsHosting);

            }
        });
    }

    private boolean testContent(){
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
