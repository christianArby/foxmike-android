package com.foxmike.android.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.foxmike.android.R;
import com.foxmike.android.fragments.LinkWithFacebookFragment;
import com.foxmike.android.fragments.PasswordResetFragment;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private Button facebookLoginButton;
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
    private String email;
    public AuthCredential  credential;
    private GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 1;
    private Button googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        loginTV = findViewById(R.id.loginTV);
        createAccountBtn = findViewById(R.id.createAccountBtn);
        facebookLoginButton = findViewById(R.id.continueWithFacebookButton);
        progressBar = findViewById(R.id.progressBar_cyclic);
        myProgressBar = new MyProgressBar(progressBar,this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        getWindow().setStatusBarColor(getResources().getColor(R.color.foxmikePrimaryColor));

        // -------------------Google Sign-in----------------------------------
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        // Set the dimensions of the sign-in button.
        googleSignInButton = findViewById(R.id.googleSignInButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id_for_google))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myProgressBar.startProgressBar();
                googleSignInButton.setEnabled(false);
                signIn();
            }
        });


        // ----------------- E-mail and password login ------------------------
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        // ----------------- Create account ------------------------
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        // ----------------- Facebook login ------------------------
        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myProgressBar.startProgressBar();
                facebookLoginButton.setEnabled(false);

                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        // Logged in to Facebook success
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
                                email = bFacebookData.getString("email");
                                // Continue login process with token
                                handleFacebookAccessToken(loginResult.getAccessToken());
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location");
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

    // Sign in to Firebase with Facebook token
    private void handleFacebookAccessToken(AccessToken token) {

        mAuth = FirebaseAuth.getInstance();

        credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkIfUserExistsInDb();
                        } else {

                            myProgressBar.stopProgressBar();
                            facebookLoginButton.setEnabled(true);
                            // If sign in fails, account already exists, ask user if he/she wants to link the accounts
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            LinkWithFacebookFragment linkWithFacebookFragment = LinkWithFacebookFragment.newInstance(email);
                            linkWithFacebookFragment.show(transaction,"linkWithFacebookFragment");
                        }

                    }
                });
    }

    // Check if user exists in database
    public void checkIfUserExistsInDb() {
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myProgressBar.stopProgressBar();
                    facebookLoginButton.setEnabled(true);
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

    // Handle Facebook and Google sign in result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                myProgressBar.stopProgressBar();
                googleSignInButton.setEnabled(true);
                // Google Sign In failed, update UI appropriately
                Snackbar.make(findViewById(R.id.welcomeParentView), "Google Authentication Failed." + getString(R.string.web_client_id_for_google), Snackbar.LENGTH_SHORT).show();
                Log.w("min app", "Google Authentication Failed." + getString(R.string.web_client_id_for_google), e);
                // ...
            }
        }
    }

    // Function to get Facebook data
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

    // Add user to Firabase database
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
                            facebookLoginButton.setEnabled(true);
                            Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                        }
                    });
                } else {
                    Toast.makeText(WelcomeActivity.this, R.string.username_exists_text, Toast.LENGTH_SHORT).show();
                    numberOfTriedUserNames++;
                    if (numberOfTriedUserNames>2) {
                        startRangeCeiling = startRangeCeiling*10;
                    }
                    if (numberOfTriedUserNames<10) {
                        addUserWithRandomUserName();
                    } else {
                        Toast.makeText(WelcomeActivity.this, R.string.failed_registration_text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // ----------------------------------- END
    }

    public AuthCredential getCredential() {
        return credential;
    }

    // ------------- Reset password -----------------------------
    public void resetPassword (String email) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        PasswordResetFragment passwordResetFragment = PasswordResetFragment.newInstance(email);
        passwordResetFragment.show(transaction,"passwordResetFragment");

    }

    // ------------------- GOOGLE SIGN IN METHODS ----------------
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        firstName = acct.getGivenName();
        lastName = acct.getFamilyName();
        imageURL = acct.getPhotoUrl().toString();
        email = acct.getEmail();
        Log.d("min app", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            checkIfUserExistsInDb();
                        } else {
                            myProgressBar.stopProgressBar();
                            googleSignInButton.setEnabled(true);
                            Snackbar.make(findViewById(R.id.welcomeParentView), "Google Firebase authentication failed.", Snackbar.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    // ------------------- GOOGLE SIGN METHODS ENDS ----------------
}
