package com.foxmike.android.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnHostSessionChangedListener;
import com.foxmike.android.interfaces.OnStudioChangedListener;
import com.foxmike.android.models.Studio;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


public class CreateOrEditStudioFragment extends Fragment {

    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private TextView mLocation;
    private TextInputLayout mDurationTIL;
    private TextInputLayout mMaxParticipantsTIL;
    private TextInputLayout mSessionTypeTIL;
    private TextInputEditText mSessionName;
    private TextInputEditText mSessionType;
    private TextInputEditText mDate;
    private TextInputEditText mTime;
    private TextInputEditText mMaxParticipants;
    private TextInputEditText mDuration;
    private EditText mWhat;
    private EditText mWho;
    private EditText mWhere;
    private EditText mPrice;
    private Button mCreateStudioBtn;
    private final Calendar myCalendar = Calendar.getInstance();
    private ListView lv;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageSessionImage;
    private int studioExist;
    private long mSessionTimestamp;
    private ImageButton mSessionImageButton;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri = null;
    private ProgressDialog mProgress;
    private LatLng clickedLatLng;
    private String existingStudioId;
    private String thisStudioId;
    private Studio thisStudio;
    private String mStudioId;
    private Studio existingStudio;
    private Studio exStudio;
    private GeoFire geoFire;
    private DatabaseReference currentUserDbRef;
    private FirebaseAuth mAuth;
    private MapsFragment mapsFragment;
    private FragmentManager fragmentManager;
    private OnStudioChangedListener onStudioChangedListener;
    static CreateOrEditStudioFragment fragment;
    private ProgressBar progressBar;
    private String accountCountry;
    private boolean payoutsEnabled;
    private boolean infoIsValid;
    private Studio mUpdatedStudio;
    private String accountCurrency;
    private static final String STUDIOS = "studiosTEST";
    private static final String SESSIONS = "sessionsTEST";
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofireTEST");



    private View view;


    public CreateOrEditStudioFragment() {
        // Required empty public constructor
    }

    public static CreateOrEditStudioFragment newInstance() {
        fragment = new CreateOrEditStudioFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            existingStudioId = bundle.getString("StudioId");
            clickedLatLng = bundle.getParcelable("LatLng");
            existingStudio = (Studio) bundle.getSerializable("studio");
            }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_create_or_edit_studio, container, false);

        String test = getResources().getString(R.string.geofire_pathname);

        /* Set and inflate "create session" layout*/
        View createStudio;
        LinearLayout createStudioContainer = view.findViewById(R.id.create_studio_container);
        createStudio = inflater.inflate(R.layout.create_or_edit_studio, createStudioContainer,false);
        // Setup views
        mLocation = createStudio.findViewById(R.id.locationTV);
        mDate = createStudio.findViewById(R.id.dateET);
        mSessionName = createStudio.findViewById(R.id.sessionNameET);
        mSessionType = createStudio.findViewById(R.id.sessionTypeET);
        mSessionTypeTIL = createStudio.findViewById(R.id.sessionTypeTIL);
        mTime = createStudio.findViewById(R.id.timeET);
        mMaxParticipants = createStudio.findViewById(R.id.maxParticipantsET);
        mDuration = createStudio.findViewById(R.id.durationET);
        mDurationTIL = createStudio.findViewById(R.id.durationTIL);
        mMaxParticipantsTIL = createStudio.findViewById(R.id.maxParticipantTIL);
        mWhat = createStudio.findViewById(R.id.whatET);
        mWho = createStudio.findViewById(R.id.whoET);
        mWhere = createStudio.findViewById(R.id.whereET);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("Session_images");
        mProgress = new ProgressDialog(getActivity());
        mCreateStudioBtn = createStudio.findViewById(R.id.createSessionBtn);
        mPrice = createStudio.findViewById(R.id.priceET);
        mSessionImageButton = createStudio.findViewById(R.id.sessionImageBtn);
        progressBar = createStudio.findViewById(R.id.progressBar_cyclic);

        // Add view to create session container
        createStudioContainer.addView(createStudio);

        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        currentUserDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        /* Create Geofire object in order to store latitude and longitude under in Geofire structure */
        geoFire = new GeoFire(mGeofireDbRef);

        // Setup standard aspect ratio of session image
        mSessionImageButton.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams mParams;
                mParams = (RelativeLayout.LayoutParams) mSessionImageButton.getLayoutParams();
                mParams.height = mSessionImageButton.getWidth()*getResources().getInteger(R.integer.heightOfSessionImageNumerator)/getResources().getInteger(R.integer.heightOfSessionImageDenominator);
                mSessionImageButton.setLayoutParams(mParams);
                mSessionImageButton.postInvalidate();
            }
        });

        /*When imagebutton is clicked start gallery in phone to let user choose photo/image*/
        mSessionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                fragment.startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        // Setup location icon click listener
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("MY_PERMISSIONS_REQUEST_LOCATION",99);
                bundle.putBoolean("changeLocation", true);
                mapsFragment = MapsFragment.newInstance();
                mapsFragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (null == fragmentManager.findFragmentByTag("studioMapsFragment")) {
                    transaction.add(R.id.container_studio_maps_fragment, mapsFragment,"studioMapsFragment").addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        /** When item is clicked create a dialog with the specified title and string array */
        mSessionTypeTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(getString(R.string.choose_session_type), R.array.sessionType_array,mSessionType);
            }
        });
        mSessionType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.choose_session_type), R.array.sessionType_array,mSessionType);
            }
        });

        mDurationTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.session_duration), R.array.duration_array,mDuration);
            }
        });
        mDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.session_duration), R.array.duration_array,mDuration);
            }
        });

        mMaxParticipantsTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.nr_participants), R.array.max_participants_array,mMaxParticipants);
            }
        });
        mMaxParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(getString(R.string.nr_participants), R.array.max_participants_array,mMaxParticipants);
            }
        });

        // --------------------------- FIND THE STUDIO OBJECT OR STUDIO ID AND DOWNLOAD OBJECT, THEN CALL FUNTION fillUI() -----------------------------------------------
        studioExist=0;
        if (existingStudioId != null | existingStudio!=null) {
            /**If this activity was started from clicking on an edit studio or returning from mapsfragment the previous activity should have sent a bundle with the studio key or studio object, if so
             * extract the key and fill in the existing values in the view (Edit view). Set the text of the button to "Update studio"*/
            studioExist=1;
            if (existingStudio==null) {
                final DatabaseReference studioDbRef = rootDbRef.child(STUDIOS).child(existingStudioId);
                studioDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        existingStudio = dataSnapshot.getValue(Studio.class);
                        fillUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                fillUI();
            }


        } /* If no bundle or sessionID exists, the method takes for granted that the activity was started by clicking on the map and a bundle with the LatLng object should exist,
          if so extract the LatLng and set the image to the default image (Create view)*/
        else {

            String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
            mLocation.setText(address);
            mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER);
        }

        // ----------------OnCreateOrEditStudioBtnClicked: Get the info from UI and create or update studio-----------------------------------------------
        mCreateStudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studioExist==1) {
                    mProgress.setMessage(getString(R.string.updating_session));
                } else {
                    mProgress.setMessage(getString(R.string.creating_session));
                }

                mProgress.show();
                updateStudioObjectFromUI(new OnStudioUpdatedListener() {
                    @Override
                    public void OnStudioUpdated(final Studio updatedStudio) {
                        mUpdatedStudio = updatedStudio;
                        sendStudio(updatedStudio);
                    }
                });
            }
        });



        return view;
    }

    /**Send session object to database */
    private void sendStudio(Studio sendStudio) {
        rootDbRef.child(STUDIOS).child(mStudioId).setValue(sendStudio);
        geoFire.setLocation(mStudioId, new GeoLocation(sendStudio.getLatitude(), sendStudio.getLongitude()));
        mUserDbRef.child(currentFirebaseUser.getUid()).child("studios").child(mStudioId).setValue(true);
        onStudioChangedListener.OnStudioChanged();
    }

    private void fillUI() {
        clickedLatLng = new LatLng(existingStudio.getLatitude(), existingStudio.getLongitude());
        String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
        mLocation.setText(address);
        setImage(existingStudio.getImageUrl(),mSessionImageButton);
        mSessionName.setText(existingStudio.getSessionName());
        mSessionType.setText(existingStudio.getSessionType());
        mMaxParticipants.setText(existingStudio.getMaxParticipants());
        mDuration.setText(existingStudio.getDuration());
        mWhat.setText(existingStudio.getWhat());
        mWho.setText(existingStudio.getWho());
        mWhere.setText(existingStudio.getWhere());
        mCreateStudioBtn.setText(R.string.update_session);
    }

    // Function retrieveStripeAccount
    private Task<HashMap<String, Object>> retrieveStripeAccount(String accountId) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        return mFunctions
                .getHttpsCallable("retrieveAccount")
                .call(accountId)
                .continueWith(new Continuation<HttpsCallableResult, HashMap<String, Object>>() {
                    @Override
                    public HashMap<String, Object> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        return result;
                    }
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }


    @NonNull
    public void updateStudioObjectFromUI(final OnStudioUpdatedListener onStudioUpdatedListener) {

        infoIsValid = true;
        final Studio studio = new Studio();

        /**If studio exists get the existing session id */
        if (studioExist==1) {
            mStudioId = existingStudioId;
            studio.setSessions(existingStudio.getSessions());
            studio.setPrice(existingStudio.getPrice());
        }
        /**If session not exists create a new random session key*/
        else {
            mStudioId = rootDbRef.child(STUDIOS).push().getKey();
        }

        studio.setSessionName(mSessionName.getText().toString());
        studio.setSessionType(mSessionType.getText().toString());
        studio.setWhat(mWhat.getText().toString());
        studio.setWho(mWho.getText().toString());
        studio.setWhere(mWhere.getText().toString());
        studio.setMaxParticipants(mMaxParticipants.getText().toString());
        studio.setDuration(mDuration.getText().toString());
        studio.setLongitude(clickedLatLng.longitude);
        studio.setLatitude(clickedLatLng.latitude);
        studio.setHostId(currentFirebaseUser.getUid());

        if (TextUtils.isEmpty(studio.getSessionName())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(studio.getSessionType())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(studio.getMaxParticipants())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(studio.getDuration())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(studio.getWhat())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(studio.getWho())) {
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(studio.getWhere())) {
            infoIsValid = false;
        }

        /**If imageUrl exists it means that the user has selected a photo from the gallery, if so create a filepath and send that
         * photo to the Storage database*/
        if(mImageUri != null && infoIsValid){
            StorageReference filepath = mStorageSessionImage.child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    /** When image have been sent to storage database save also the uri (URL) to the session object and send this object to the realtime database and send user back
                     * to the main activity*/
                    studio.setImageUrl(downloadUri);

                    if (infoIsValid){
                        onStudioUpdatedListener.OnStudioUpdated(studio);
                    }   else    {
                        Toast.makeText(getContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
                    }
                    mProgress.dismiss();
                }
            });
        }
        /**If imageUri does not exists it means that the user has NOT selected a photo from the gallery, check if the session is an existing session*/
        else {
            /**If the session is an existing session set the created session object image uri to the existing image uri and send the updated object to the realtime database
             * and send the user back to the main activity*/
            if (studioExist==1) {
                studio.setImageUrl(existingStudio.getImageUrl());
                mProgress.dismiss();

                if (infoIsValid){
                    onStudioUpdatedListener.OnStudioUpdated(studio);
                }   else    {
                    Toast.makeText(getContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
                }

            }
            /**If the session is NOT an existing session tell the user that a photo must be chosen*/
            else {
                mProgress.dismiss();
                Toast.makeText(getContext(), R.string.type_in_necessary_information,Toast.LENGTH_LONG).show();
            }
        }
    }

    /**Method createDialog creates a dialog with a title and a list of strings to choose from.*/
    private void createDialog(String title, int string_array, final EditText mEditText) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_listview, null);
        alertDialogBuilder.setView(convertView);
        alertDialogBuilder.setTitle(title);
        lv = convertView.findViewById(R.id.listView1);
        lv.getDivider().setAlpha(0);
        String[] values = getResources().getStringArray(string_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,values);
        lv.setAdapter(adapter);
        final AlertDialog dlg = alertDialogBuilder.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) lv.getItemAtPosition(position);
                mEditText.setText(itemValue);
                dlg.hide();
            }
        });
    }

    /** When user has selected an image from the gallery get that imageURI and save it in mImageUri and set the image to the imagebutton  */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(getResources().getInteger(R.integer.heightOfSessionImageDenominator), getResources().getInteger(R.integer.heightOfSessionImageNumerator))
                    .start(fragment.getContext(), fragment);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mSessionImageButton.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    /**Method setImage scales the chosen image*/
    private void setImage(String image, ImageView imageView) {
        mSessionImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(image).into(imageView);
    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String returnAddress;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if (addresses.size()!=0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String address2 = addresses.get(0).getAddressLine(1);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                String street = addresses.get(0).getThoroughfare();// Only if available else return NULL

                if (street != null) {

                    if (!street.equals(knownName)) {
                        returnAddress = street + " " + knownName;
                    } else {
                        returnAddress = street;
                    }
                } else {
                    if (addresses.get(0).getLocality()!=null) {
                        returnAddress = addresses.get(0).getLocality() + " " + addresses.get(0).getPremises();
                    } else {
                        returnAddress = "Unknown area";
                    }

                }
            } else {
                returnAddress = "Unknown area";
            }

        } catch (IOException ex) {
            returnAddress = "failed";
        }
        return returnAddress;
    }

    public void updateLocation(LatLng latLng) {
        if (getView()!=null) {
            clickedLatLng = new LatLng(latLng.latitude, latLng.longitude);
            String address = getAddress(clickedLatLng.latitude,clickedLatLng.longitude);
            mLocation.setText(address);
        }
    }

    public interface OnStudioUpdatedListener {
        void OnStudioUpdated(Studio updatedStudio);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStudioChangedListener) {
            onStudioChangedListener = (OnStudioChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStudioChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onStudioChangedListener = null;
    }

}
