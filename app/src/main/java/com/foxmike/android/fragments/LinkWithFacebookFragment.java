package com.foxmike.android.fragments;


import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.foxmike.android.R;
import com.foxmike.android.activities.WelcomeActivity;
import com.foxmike.android.utils.MyProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class LinkWithFacebookFragment extends DialogFragment {

    public static final String TAG = LinkWithFacebookFragment.class.getSimpleName();

    private TextView mLinkAccountText;
    private EditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private TextView resetText;
    private static final String ARG_EMAIL= "email";
    private String mEmail;
    private FirebaseAuth mAuth;
    private long mLastClickTime = 0;
    private ProgressBar progressBar;
    private MyProgressBar myProgressBar;


    public LinkWithFacebookFragment() {
        // Required empty public constructor
    }

    public static LinkWithFacebookFragment newInstance(String email) {
        LinkWithFacebookFragment fragment = new LinkWithFacebookFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEmail = getArguments().getString(ARG_EMAIL);
        }
        setStyle(DialogFragment.STYLE_NORMAL,R.style.partscreenDialog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_link_with_facebook, container, false);

        FloatingActionButton mLoginBtn;

        mLoginEmailField = view.findViewById(R.id.loginEmailField);
        mLoginPasswordField = view.findViewById(R.id.loginPasswordField);
        mLoginBtn = view.findViewById(R.id.loginBtn);
        mLinkAccountText = view.findViewById(R.id.linkAccountText);
        resetText = view.findViewById(R.id.resetText);
        progressBar = view.findViewById(R.id.progressBar_cyclic);
        myProgressBar = new MyProgressBar(progressBar, getActivity());

        mLoginEmailField.setText(mEmail);
        mLoginEmailField.setFocusable(false
        );

        mLinkAccountText.setText(R.string.account_exists_link_with_facebook_question_text);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLinkAccountText.setCursorVisible(false);

                myProgressBar.startProgressBar();
                mLastClickTime = SystemClock.elapsedRealtime();
                mAuth = FirebaseAuth.getInstance();
                final WelcomeActivity welcomeActivity = (WelcomeActivity)getActivity();
                final AuthCredential credential = welcomeActivity.getCredential();

                mAuth.signInWithEmailAndPassword(mLoginEmailField.getText().toString().trim(),mLoginPasswordField.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mAuth.getCurrentUser().linkWithCredential(credential)
                                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                welcomeActivity.checkIfUserExistsInDb("email");
                                                myProgressBar.stopProgressBar();
                                            } else {
                                                myProgressBar.stopProgressBar();
                                                Toast.makeText(getActivity(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            myProgressBar.stopProgressBar();
                            Toast.makeText(getActivity(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        resetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                dismiss();
                WelcomeActivity welcomeActivity = (WelcomeActivity)getActivity();
                welcomeActivity.resetPassword(mEmail);
            }
        });

        return view;
    }

}
