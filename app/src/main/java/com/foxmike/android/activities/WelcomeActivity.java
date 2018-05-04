package com.foxmike.android.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.foxmike.android.R;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.utils.SetOrUpdateUserImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WelcomeActivity extends AppCompatActivity {

    private TextView loginTV;
    private Button createAccountBtn;
    private Button loginButton;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private MyProgressBar myProgressBar;
    private String firstName;
    private String lastName;
    private String imageURL;
    private int numberOfTriedUserNames;
    private int startRangeCeiling;
    private DatabaseReference mDatabase;
    private int randomPIN;
    private String PINString;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        loginTV = findViewById(R.id.loginTV);
        createAccountBtn = findViewById(R.id.createAccountBtn);
        loginButton = findViewById(R.id.continueWithFacebookButton);
        progressBar = findViewById(R.id.progressBar_cyclic);

        myProgressBar = new MyProgressBar(progressBar,this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        mCallbackManager = CallbackManager.Factory.create();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myProgressBar.startProgressBar();
                loginButton.setEnabled(false);

                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {



                        // -- getting stuff from fb ----
                        String accessToken = loginResult.getAccessToken().getToken();
                        Log.i("accessToken", accessToken);

                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("LoginActivity", response.toString());
                                // Get facebook data from login
                                Bundle bFacebookData = getFacebookData(object);
                                firstName = bFacebookData.getString("first_name");
                                lastName = bFacebookData.getString("last_name");
                                imageURL = bFacebookData.getString("profile_pic");
                                handleFacebookAccessToken(loginResult.getAccessToken());
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
                        request.setParameters(parameters);
                        request.executeAsync();
                        // -- end --

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }
                });
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {

        mAuth = FirebaseAuth.getInstance();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            myProgressBar.stopProgressBar();
                            loginButton.setEnabled(true);
                            checkIfUserExistsInDb();
                        } else {
                            myProgressBar.stopProgressBar();
                            loginButton.setEnabled(true);
                            // If sign in fails, display a message to the user. task.getException()
                            Toast.makeText(WelcomeActivity.this, "Error Login", Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }

    private void checkIfUserExistsInDb() {



        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Intent mainIntent = new Intent(WelcomeActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                } else {
                    numberOfTriedUserNames =0;
                    startRangeCeiling = 10000;
                    addUserWithRandomUserName();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private Bundle getFacebookData(JSONObject object) {

        try {
            Bundle bundle = new Bundle();
            String id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=400&height=400");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
            if (object.has("birthday"))
                bundle.putString("birthday", object.getString("birthday"));
            if (object.has("location"))
                bundle.putString("location", object.getJSONObject("location").getString("name"));

            return bundle;
        }
        catch(JSONException e) {
            Log.d("min app","Error parsing JSON");
        }
        return null;
    }

    private void addUserWithRandomUserName() {
        Random random = new Random();
        randomPIN = random.nextInt(startRangeCeiling);
        PINString = String.valueOf(randomPIN);
        if (numberOfTriedUserNames>0) {
            userName = "@"+firstName.toLowerCase()+lastName.toLowerCase()+PINString;
        } else {
            userName = "@"+firstName.toLowerCase()+lastName.toLowerCase();
        }

        //add the user
        mDatabase.child("usernames").child(userName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(mAuth.getCurrentUser().getUid());
                    return Transaction.success(mutableData);
                }

                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean commited, DataSnapshot dataSnapshot) {
                if (commited) {

                    Map userMap = new HashMap();
                    userMap.put("userName",userName);
                    userMap.put("firstName",firstName);
                    userMap.put("lastName",lastName);
                    userMap.put("fullName",firstName + " " + lastName);
                    userMap.put("image", imageURL);
                    userMap.put("thumb_image", imageURL);
                    mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            myProgressBar.stopProgressBar();
                            Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                        }
                    });

                } else {
                    Toast.makeText(WelcomeActivity.this, "Username exists", Toast.LENGTH_SHORT).show();
                    numberOfTriedUserNames++;
                    if (numberOfTriedUserNames>2) {
                        startRangeCeiling = startRangeCeiling*10;
                    }
                    if (numberOfTriedUserNames<10) {
                        addUserWithRandomUserName();
                    } else {
                        Toast.makeText(WelcomeActivity.this, "Failed Registration", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // ----------------------------------- NEW END
    }
}
