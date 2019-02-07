package com.foxmike.android.fragments;


import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.foxmike.android.R;
import com.foxmike.android.activities.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class LinkWithFacebookFragment extends DialogFragment {

    private TextView mLinkAccountText;
    private EditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private TextView resetText;
    private static final String ARG_EMAIL= "email";
    private String mEmail;
    private FirebaseAuth mAuth;
    private long mLastClickTime = 0;


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
                                                welcomeActivity.checkIfUserExistsInDb();
                                            } else {
                                                Toast.makeText(getActivity(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
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
