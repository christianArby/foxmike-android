package com.foxmike.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.foxmike.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class CreateTrainerAboutMeFragment extends Fragment {

    public static final String TAG = CreateTrainerAboutMeFragment.class.getSimpleName();

    private TextInputLayout aboutMeTIL;
    private TextInputEditText aboutMeET;
    private Button nextBtn;
    private boolean infoIsValid = true;
    private OnCreateTrainerAboutMeListener onCreateTrainerAboutMeListener;
    private long mLastClickTime = 0;

    public CreateTrainerAboutMeFragment() {
        // Required empty public constructor
    }

    public static CreateTrainerAboutMeFragment newInstance(String param1, String param2) {
        CreateTrainerAboutMeFragment fragment = new CreateTrainerAboutMeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_trainer_about_me, container, false);

        aboutMeTIL = view.findViewById(R.id.aboutMeTIL);
        aboutMeET = view.findViewById(R.id.aboutMeET);
        nextBtn = view.findViewById(R.id.nextBtn);

        aboutMeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length()<51) {
                    aboutMeTIL.setError(getString(R.string.please_write_a_longer_description));
                    infoIsValid = false;
                } else {
                    infoIsValid = true;
                    aboutMeTIL.setError(null);
                }

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (TextUtils.isEmpty(aboutMeET.getText().toString().trim())) {
                    aboutMeTIL.setError(getString(R.string.describe_yourself));
                } else {
                    if (infoIsValid) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("aboutMe").setValue(aboutMeET.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                onCreateTrainerAboutMeListener.onCreateTrainerAboutMe();
                            }
                        });
                    }
                }
            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateTrainerAboutMeListener) {
            onCreateTrainerAboutMeListener = (OnCreateTrainerAboutMeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateTrainerAboutMeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCreateTrainerAboutMeListener = null;
    }

    public interface OnCreateTrainerAboutMeListener {
        void onCreateTrainerAboutMe();
    }
}
