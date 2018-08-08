package com.foxmike.android.fragments;


import android.app.ActivityOptions;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


public class CreateOrEditStudioFragment extends Fragment {

    private final DatabaseReference mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private TextView mLocation;
    private TextInputLayout mStudioTypeTIL;
    private TextInputLayout mLocationTIL;
    private TextInputLayout mDescriptionTIL;
    private TextInputLayout mStudioNameTIL;
    private TextInputEditText mStudioName;
    private TextInputEditText mStudioType;
    private EditText mDescription;
    private Button mCreateStudioBtn;
    private ListView lv;
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageSessionImage;
    private int studioExist;
    private long mSessionTimestamp;
    private ImageButton mStudioImageButton;
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
    private boolean infoIsValid = true;
    private Studio mUpdatedStudio;
    private String accountCurrency;
    private static final String STUDIOS = "studiosTEST";
    private static final String SESSIONS = "sessionsTEST";
    private final DatabaseReference mGeofireDbRef = FirebaseDatabase.getInstance().getReference().child("geofireTEST");
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 123;
    private Place place;
    private TextView chooseImageError;



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
            existingStudio = (Studio) bundle.getSerializable("studio");
        }
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_create_or_edit_studio, container, false);

        /* Set and inflate "create session" layout*/
        View createStudio;
        LinearLayout createStudioContainer = view.findViewById(R.id.create_studio_container);
        createStudio = inflater.inflate(R.layout.create_or_edit_studio, createStudioContainer,false);
        // Setup views
        mLocation = createStudio.findViewById(R.id.locationTV);
        mStudioName = createStudio.findViewById(R.id.studioNameET);
        mStudioType = createStudio.findViewById(R.id.studioTypeET);
        mStudioTypeTIL = createStudio.findViewById(R.id.studioTypeTIL);
        mStorageSessionImage = FirebaseStorage.getInstance().getReference().child("studio_images");
        mProgress = new ProgressDialog(getActivity());
        mCreateStudioBtn = createStudio.findViewById(R.id.createStudioBtn);
        mStudioImageButton = createStudio.findViewById(R.id.studioImageBtn);
        progressBar = createStudio.findViewById(R.id.progressBar_cyclic);
        mDescription = createStudio.findViewById(R.id.descriptionET);
        mLocationTIL = createStudio.findViewById(R.id.locationTIL);
        mDescriptionTIL = createStudio.findViewById(R.id.descriptionTIL);
        mStudioNameTIL = createStudio.findViewById(R.id.studioNameTIL);
        chooseImageError = createStudio.findViewById(R.id.imageErrorText);

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
        mStudioImageButton.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams mParams;
                mParams = (RelativeLayout.LayoutParams) mStudioImageButton.getLayoutParams();
                mParams.height = mStudioImageButton.getWidth()*getResources().getInteger(R.integer.heightOfStudioImageNumerator)/getResources().getInteger(R.integer.heightOfStudioImageDenominator);
                mStudioImageButton.setLayoutParams(mParams);
                mStudioImageButton.postInvalidate();
            }
        });

        /*When imagebutton is clicked start gallery in phone to let user choose photo/image*/
        mStudioImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageError.setVisibility(View.GONE);

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
                mLocationTIL.setError(null);
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        mLocationTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        /** When item is clicked create a dialog with the specified title and string array */
        mStudioTypeTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStudioTypeTIL.setError(null);
                createDialog(getString(R.string.choose_session_type), R.array.sessionType_array,mStudioType);
            }
        });
        mStudioType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStudioTypeTIL.setError(null);
                createDialog(getString(R.string.choose_session_type), R.array.sessionType_array,mStudioType);
            }
        });

        mStudioNameTIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStudioNameTIL.setError(null);
            }
        });
        mStudioName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStudioName.setError(null);
            }
        });

        mDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length()<51) {
                    mDescriptionTIL.setError(getString(R.string.please_write_a_longer_description));
                    infoIsValid = false;
                } else {
                    infoIsValid = true;
                    mDescriptionTIL.setError(null);
                }

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


            mStudioImageButton.setScaleType(ImageView.ScaleType.CENTER);
        }

        // ----------------OnCreateOrEditStudioBtnClicked: Get the info from UI and create or update studio-----------------------------------------------
        mCreateStudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (studioExist==1) {
                    mProgress.setMessage(getString(R.string.updating_training_group));
                } else {
                    mProgress.setMessage(getString(R.string.creating_training_group));
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

        mLocation.setText(existingStudio.getLocation());
        setImage(existingStudio.getImageUrl(),mStudioImageButton);
        mStudioName.setText(existingStudio.getStudioName());
        mStudioType.setText(existingStudio.getStudioType());
        mCreateStudioBtn.setText(R.string.update_training_group);
        mDescription.setText(existingStudio.getDescription());
        mLocation.setText(existingStudio.getLocation());
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

        final Studio studio = new Studio();

        /**If studio exists get the existing session id */
        if (studioExist==1) {
            mStudioId = existingStudioId;
            studio.setSessions(existingStudio.getSessions());
            studio.setLatitude(existingStudio.getLatitude());
            studio.setLongitude(existingStudio.getLongitude());
        }
        /**If session not exists create a new random session key*/
        else {
            mStudioId = rootDbRef.child(STUDIOS).push().getKey();
            if (place==null) {
                mLocationTIL.setError(getString(R.string.please_choose_location));
                infoIsValid = false;
            } else {
                studio.setLatitude(place.getLatLng().latitude);
                studio.setLongitude(place.getLatLng().longitude);
            }

        }

        studio.setStudioName(mStudioName.getText().toString());
        studio.setStudioType(mStudioType.getText().toString());
        studio.setDescription(mDescription.getText().toString());
        studio.setLongitude(0);
        studio.setLatitude(0);
        studio.setHostId(currentFirebaseUser.getUid());
        studio.setLocation(mLocation.getText().toString());

        if (TextUtils.isEmpty(studio.getStudioName())) {
            mStudioNameTIL.setError(getString(R.string.please_choose_name));
            infoIsValid = false;
        }

        if (TextUtils.isEmpty(studio.getStudioType())) {
            mStudioTypeTIL.setError(getString(R.string.please_choose_type));
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
                chooseImageError.setVisibility(View.VISIBLE);
                mProgress.dismiss();
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

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                place = PlaceAutocomplete.getPlace(getActivity(), data);
                mLocation.setText(place.getName());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(getResources().getInteger(R.integer.heightOfStudioImageDenominator), getResources().getInteger(R.integer.heightOfStudioImageNumerator))
                    .start(fragment.getContext(), fragment);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();
                mStudioImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mStudioImageButton.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    /**Method setImage scales the chosen image*/
    private void setImage(String image, ImageView imageView) {
        mStudioImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
