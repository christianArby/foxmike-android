package com.foxmike.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foxmike.android.R;
import com.foxmike.android.activities.CreateTrainerActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.foxmike.android.models.CreditCard.BRAND_CARD_RESOURCE_MAP;


public class CreateTrainerDepositionFragment extends Fragment {

    public static final String TAG = CreateTrainerDepositionFragment.class.getSimpleName();

    private View mainView;
    private DatabaseReference rootDbRef;
    private FirebaseAuth mAuth;
    @BindView(R.id.paymentMethod)
    AppCompatTextView paymentMethodTV;
    @BindView(R.id.addPaymentMethodTV) AppCompatTextView addPaymentMethodTV;
    @BindView(R.id.makeDepositionBtn)
    AppCompatButton makeDepositionBtn;
    @BindView(R.id.addDepostionLaterBtn) AppCompatButton addDepostionLaterBtn;
    @BindView(R.id.dotProgressBarContainer) DotProgressBar dotProgressBarContainer;
    private HashMap defaultSourceMap;
    private boolean hasPaymentMethod;
    private OnCreateTrainerDepositionListener onCreateTrainerDepositionListener;


    //private OnDepositionFragmentInteractionListener onDepositionFragmentInteractionListener;

    public CreateTrainerDepositionFragment() {
        // Required empty public constructor
    }
    public static CreateTrainerDepositionFragment newInstance() {
        CreateTrainerDepositionFragment fragment = new CreateTrainerDepositionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        getDefaultSourceMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDefaultSourceMap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_create_trainer_deposition, container, false);
        ButterKnife.bind(this, mainView);


        return mainView;
    }



    private void getDefaultSourceMap () {
        try {
            CreateTrainerActivity createTrainerActivity = (CreateTrainerActivity) getActivity();
            Disposable subscription = createTrainerActivity.subject.subscribe(new Consumer<HashMap>() {
                @Override
                public void accept(HashMap hashMap) throws Exception {
                    if (hashMap.get("brand")!=null) {
                        defaultSourceMap = hashMap;
                        hasPaymentMethod = true;
                        setupUI(hasPaymentMethod);
                    } else {
                        hasPaymentMethod = false;
                        setupUI(hasPaymentMethod);
                    }
                }
            });
            return;
        } catch (RuntimeException e){
            hasPaymentMethod = false;
            setupUI(hasPaymentMethod);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI(hasPaymentMethod);
    }

    private void setupUI(boolean hasPaymentSystem) {

        if (isAdded()) {
            if (hasPaymentSystem) {
            paymentMethodTV.setVisibility(View.VISIBLE);
            addPaymentMethodTV.setVisibility(View.GONE);
            dotProgressBarContainer.setVisibility(View.GONE);

            makeDepositionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_primary));

            String last4 = defaultSourceMap.get("last4").toString();
            paymentMethodTV.setText("**** " + last4);
            String cardBrand = defaultSourceMap.get("brand").toString();
            int resourceId = BRAND_CARD_RESOURCE_MAP.get(cardBrand);
            paymentMethodTV.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);

            makeDepositionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // MAKE DEPOSITION
                }
            });

        } else {
            paymentMethodTV.setVisibility(View.GONE);
            addPaymentMethodTV.setVisibility(View.VISIBLE);
            dotProgressBarContainer.setVisibility(View.GONE);

            makeDepositionBtn.setBackground(getResources().getDrawable(R.drawable.square_button_gray));

            makeDepositionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ADD PAYMENT METHOD
                    Intent paymentPreferencesIntent = new Intent(getActivity().getApplicationContext(),PaymentPreferencesActivity.class);
                    startActivity(paymentPreferencesIntent);
                }
            });

        }

        addDepostionLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MOVE ON
                onCreateTrainerDepositionListener.OnCreateTrainerDeposition();
            }
        });

        }



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateTrainerDepositionListener) {
            onCreateTrainerDepositionListener = (OnCreateTrainerDepositionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateTrainerDepositionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCreateTrainerDepositionListener = null;
    }
    public interface OnCreateTrainerDepositionListener {

        void OnCreateTrainerDeposition();
    }
}
