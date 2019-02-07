package com.foxmike.android.fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.foxmike.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordResetFragment extends DialogFragment {

    private EditText mLoginEmailField;
    private static final String ARG_EMAIL= "email";
    private String mEmail;
    private FirebaseAuth mAuth;
    private long mLastClickTime = 0;


    public PasswordResetFragment() {
        // Required empty public constructor
    }

    public static PasswordResetFragment newInstance(String email) {
        PasswordResetFragment fragment = new PasswordResetFragment();
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
        View view = inflater.inflate(R.layout.fragment_password_reset, container, false);

        FloatingActionButton mSendBtn;

        mLoginEmailField = view.findViewById(R.id.loginEmailField);
        mSendBtn = view.findViewById(R.id.sendBtn);

        mLoginEmailField.setText(mEmail);

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String email = mLoginEmailField.getText().toString().trim();

                if (!TextUtils.isEmpty(email)) {
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), R.string.a_link_has_been_sent_to_the_email_above_text, Toast.LENGTH_LONG).show();
                                        dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "We failed sending a mail to the mail address above.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Please type in your mail address", Toast.LENGTH_LONG).show();
                }


            }
        });
        return view;
    }

}
