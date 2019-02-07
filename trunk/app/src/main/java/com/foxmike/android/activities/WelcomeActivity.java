package com.foxmike.android.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.foxmike.android.models.User;
import com.foxmike.android.utils.AddUserToDatabase;
import com.foxmike.android.utils.CheckVersion;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private DatabaseReference mDatabase;
    private String email;
    public AuthCredential  credential;
    private GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 1;
    private Button googleSignInButton;
    private FirebaseFunctions mFunctions;
    private HashMap<String, Object> friendsData = new HashMap<>();
    private View view;
    private TextView versionNameTV;
    String version;
    private long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mFunctions = FirebaseFunctions.getInstance();

        loginTV = findViewById(R.id.loginTV);
        createAccountBtn = findViewById(R.id.createAccountBtn);
        facebookLoginButton = findViewById(R.id.continueWithFacebookButton);
        progressBar = findViewById(R.id.progressBar_cyclic);
        myProgressBar = new MyProgressBar(progressBar,this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        view = findViewById(R.id.welcomeParentView);
        versionNameTV = findViewById(R.id.versionName);

        version = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionNameTV.setText(version);

        getWindow().setStatusBarColor(getResources().getColor(R.color.foxmikePrimaryColor));

        // ----------------- E-mail and password login ------------------------
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        // ----------------- Facebook login -----------------------------------
        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

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
                                // Get facebook data from login, to be used in AddUserToDatabaseWithUniqueUsername later on
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

        // -------------------Google Sign-in--------------------------------------
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
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                myProgressBar.startProgressBar();
                googleSignInButton.setEnabled(false);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // ----------------- Register -------------------------------------------
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent registerIntent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
        // ----------------------------------------------------------------------
    }
    // --------------- Handle Facebook result -----------------
    // Sign in to Firebase with Facebook token
    private void handleFacebookAccessToken(AccessToken token) {
        mAuth = FirebaseAuth.getInstance();
        credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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

    // ------------------- Handle GOOGLE SIGN IN result ----------------
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
    // Sign in to Firebase with Google token
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        // Save data from Google in variables to be used in AddUserToDatabaseWithUniqueUsername later on
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

    // Check if user exists in database
    public void checkIfUserExistsInDb() {
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    registrationFinished();
                } else {
                    User user = new User();
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setFullName(firstName + " " + lastName);
                    user.setImage(imageURL);
                    user.setThumb_image(imageURL);

                    AddUserToDatabase addUserToDatabase = new AddUserToDatabase();
                    addUserToDatabase.AddUserToDatabaseWithUniqueUsername(WelcomeActivity.this, user);
                    addUserToDatabase.setOnUserAddedToDatabaseListener(new AddUserToDatabase.OnUserAddedToDatabaseListener() {
                        @Override
                        public void OnUserAddedToDatabase() {
                            FirebaseDatabase.getInstance().getReference().child("foxmikeUID").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue()!=null) {
                                        String foxmikeUID = dataSnapshot.getValue().toString();
                                        if (!mAuth.getCurrentUser().getUid().equals(foxmikeUID)) {

                                            String currentDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                                            friendsData.put("currentUserId", mAuth.getCurrentUser().getUid());
                                            friendsData.put("foxmikeUID", foxmikeUID);
                                            friendsData.put("currentDate", currentDate);

                                            makeUserFriendWithFoxmike(friendsData).addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    // If not succesful, show error
                                                    if (!task.isSuccessful()) {
                                                        Exception e = task.getException();
                                                        myProgressBar.stopProgressBar();
                                                        showSnackbar("An error occurred." + e.getMessage());
                                                        return;
                                                    }
                                                    // Show the string passed from the Firebase server if task/function call on server is successful
                                                    String result = task.getResult();
                                                    if (result.equals("success")) {
                                                        registrationFinished();
                                                    } else {
                                                        showSnackbar(result);
                                                    }
                                                }
                                            });

                                        } else {
                                            registrationFinished();
                                        }
                                    } else {
                                        registrationFinished();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    });


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void registrationFinished() {
        myProgressBar.stopProgressBar();
        facebookLoginButton.setEnabled(true);
        Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
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

    // Function makeUserFriendWithFoxmike
    private Task<String> makeUserFriendWithFoxmike(Map<String, Object> friendsData) {
        // Call the function and extract the operation from the result which is a String
        return mFunctions
                .getHttpsCallable("makeUserFriendWithFoxmike")
                .call(friendsData)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("operationResult");
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckVersion.checkVersion(this);
    }
}
