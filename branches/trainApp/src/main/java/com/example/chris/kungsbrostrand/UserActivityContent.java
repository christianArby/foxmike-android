package com.example.chris.kungsbrostrand;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2017-07-22.
 */

public class UserActivityContent {

    HashMap<String,Boolean> userActivityContent;

    ArrayList<Session> sessionsAttending = new ArrayList<Session>();
    ArrayList<Session> sessionsHosting = new ArrayList<Session>();
    User user;

    public void getUserActivityContent(final OnUserActivityContentListener onUserActivityContentListener){

        userActivityContent = new HashMap<String,Boolean>();
        userActivityContent.put("sessionsAttending",false);
        userActivityContent.put("sessionsHosting",false);


        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(User user) {

                if (user.sessionsAttending.size()==0){
                    userActivityContent.put("sessionsAttending",true);
                    if (testContent()==true){
                        onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting);
                    }
                }

                if (user.sessionsHosting.size()==0){
                    userActivityContent.put("sessionsHosting",true);
                    if (testContent()==true){
                        onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting);
                    }
                }

                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        sessionsAttending = sessions;
                        userActivityContent.put("sessionsAttending",true);
                        if (testContent()==true){
                            onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting);
                        }
                    }
                },user.sessionsAttending);


                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        sessionsHosting = sessions;
                        userActivityContent.put("sessionsHosting",true);
                        if (testContent()==true){
                            onUserActivityContentListener.OnUserActivityContent(sessionsAttending,sessionsHosting);
                        }
                    }
                },user.sessionsHosting);

            }
        });
    }

    public boolean testContent(){
        int n = 0;

        for (HashMap.Entry<String, Boolean> entry : this.userActivityContent.entrySet())
        {
            if (entry.getValue()==true){
                n++;
            }
            System.out.println(entry.getKey() + "/" + entry.getValue());
        }

        if (n==userActivityContent.size()){
            return true;

        }
        return  false;
    }
}
