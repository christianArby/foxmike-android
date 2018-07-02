package com.foxmike.android.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.foxmike.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class AboutUserFragment extends Fragment {

    private TextInputLayout aboutMeTIL;
    private TextInputEditText aboutMeET;
    private Button nextBtn;
    private OnAboutMeInteractionListener onAboutMeInteractionListener;

    public AboutUserFragment() {
        // Required empty public constructor
    }

    public static AboutUserFragment newInstance(String param1, String param2) {
        AboutUserFragment fragment = new AboutUserFragment();
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
        View view = inflater.inflate(R.layout.fragment_about_user, container, false);

        aboutMeTIL = view.findViewById(R.id.aboutMeTIL);
        aboutMeET = view.findViewById(R.id.aboutMeET);
        nextBtn = view.findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(aboutMeET.getText().toString().trim())) {
                    aboutMeTIL.setError(getString(R.string.describe_yourself));
                } else {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("aboutMe").setValue(aboutMeET.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            onAboutMeInteractionListener.onAboutMeInteraction();
                        }
                    });
                }


            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAboutMeInteractionListener) {
            onAboutMeInteractionListener = (OnAboutMeInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onAboutMeInteractionListener = null;
    }

    public interface OnAboutMeInteractionListener {
        void onAboutMeInteraction();
    }
}
