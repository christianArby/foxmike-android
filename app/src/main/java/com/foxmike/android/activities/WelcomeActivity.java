package com.foxmike.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
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
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.viewmodels.MaintenanceViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
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
    private TextView policyAgreementTV;
    private LinearLayout maintenanceView;
    private DatabaseReference maintenanceRef;
    private ValueEventListener maintenanceListener;
    private ImageView foxmikeIcon;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
        policyAgreementTV = findViewById(R.id.policyAgreement);
        policyAgreementTV.setMovementMethod(LinkMovementMethod.getInstance());
        maintenanceView = findViewById(R.id.maintenance);
        foxmikeIcon = findViewById(R.id.foxmikeIcon);

        int drawableResourceId = this.getResources().getIdentifier("foxmike_icon_transparent", "drawable", this.getPackageName());
        Glide.with(this).load(drawableResourceId).into(foxmikeIcon);

        version = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionNameTV.setText(version);
        //versionNameTV.setVisibility(View.GONE);

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
                loginWithFB();
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

    private void loginWithFB() {
        LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                // Logged in to Facebook success
                // -- getting stuff from fb ----
                String accessToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Get facebook data from login, to be used in AddUserToDatabaseWithUniqueUsername later on
                        Bundle bFacebookData = getFacebookData(object);
                        firstName = bFacebookData.getString("first_name");
                        lastName = bFacebookData.getString("last_name");
                        imageURL = bFacebookData.getString("profile_pic");
                        email = bFacebookData.getString("email");
                        if (email==null) {
                            alertDialogOk(getResources().getString(R.string.registration_failed), getResources().getString(R.string.email_missing_facebook), true);
                            return;
                        }
                        if (lastName == null) {
                            lastName = firstName;
                        }
                        // if firstName == null is in checkifuserexistsindb

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
                Toast.makeText(WelcomeActivity.this, "Something went wrong, please try again later. Error code 101", Toast.LENGTH_LONG).show();
                myProgressBar.stopProgressBar();
                facebookLoginButton.setEnabled(true);
            }
            @Override
            public void onError(FacebookException error) {
                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                        loginWithFB();
                        return;
                    }
                }
                Toast.makeText(WelcomeActivity.this, "Something went wrong, please try again later.", Toast.LENGTH_LONG).show();
                myProgressBar.stopProgressBar();
                facebookLoginButton.setEnabled(true);
            }
        });
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
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "facebook");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                    checkIfUserExistsInDb("facebook");
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

        if (email==null) {
            alertDialogOk(getResources().getString(R.string.registration_failed), getResources().getString(R.string.missing_email_google), true);
            return;
        }

        if (lastName == null) {
            lastName = firstName;
        }

        // if firstName == null is in checkifuserexistsindeb

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.METHOD, "google");
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                            checkIfUserExistsInDb("google");
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
    public void checkIfUserExistsInDb(String signUpMethod) {
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    registrationFinished();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.METHOD, signUpMethod);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);


                    User user = new User();
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setFullName(firstName + " " + lastName);
                    user.setImage(imageURL);
                    user.setThumb_image(imageURL);

                    if (firstName==null) {
                        Intent setupIntent = new Intent(WelcomeActivity.this,SetupAccountActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                        return;
                    }

                    AddUserToDatabase addUserToDatabase = new AddUserToDatabase();
                    addUserToDatabase.AddUserToDatabaseWithUniqueUsername(WelcomeActivity.this, user);
                    addUserToDatabase.setOnUserAddedToDatabaseListener(new AddUserToDatabase.OnUserAddedToDatabaseListener() {
                        @Override
                        public void OnUserAddedToDatabase() {
                            friendsData.put("currentUserId", mAuth.getCurrentUser().getUid());
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

        // check if maintenance
        MaintenanceViewModel maintenanceViewModel = ViewModelProviders.of(this).get(MaintenanceViewModel.class);
        LiveData<DataSnapshot> maintenanceLiveData = maintenanceViewModel.getDataSnapshotLiveData();
        maintenanceLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    if ((boolean) dataSnapshot.getValue()) {
                        maintenanceView.setVisibility(View.VISIBLE);
                    }
                } else {
                    maintenanceView.setVisibility(View.GONE);
                }
            }
        });
    }

    public void alertDialogOk(String title, String message, boolean canceledOnTouchOutside) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);
        // Add the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.show();
    }
}
