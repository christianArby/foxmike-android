package com.foxmike.android.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.adapters.ListReviewsAdapter;
import com.foxmike.android.models.Review;
import com.foxmike.android.models.SessionStars;
import com.foxmike.android.utils.CustomConstraintLayout;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class RatingsAndReviewsActivity extends AppCompatActivity {

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();

    private String sessionId;
    private String sessionName;

    private ValueEventListener sessionStarsListener;

    @BindView(R.id.ratingText) AppCompatTextView ratingTextView;
    @BindView(R.id.ratingNrText) TextView ratingNrTextView;

    @BindView(R.id.fiveStarBar) ProgressBar fiveStarBar;
    @BindView(R.id.fourStarBar) ProgressBar fourStarBar;
    @BindView(R.id.threeStarBar) ProgressBar threeStarBar;
    @BindView(R.id.twoStarBar) ProgressBar twoStarBar;
    @BindView(R.id.oneStarBar) ProgressBar oneStarBar;
    @BindView(R.id.reviewContainer) CustomConstraintLayout reviewContainer;
    @BindView(R.id.noWrittenReviewsYet) TextView noWrittenReviewsYet;

    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindView(R.id.reviewList) RecyclerView reviewList;



    private DatabaseReference reviewsRef = rootDbRef.child("reviews");
    private ListReviewsAdapter listReviewseAdapter;

    private int currentNumberOfReviews = 50;
    private Long currentLastTimestamp = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings_and_reviews);
        ButterKnife.bind(this);


        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Toolbar toolbar = (Toolbar)  findViewById(R.id.post_app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        sessionId = getIntent().getStringExtra("sessionId");
        sessionName = getIntent().getStringExtra("sessionName");

        actionBar.setTitle(sessionName);

        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(rootDbRef.child("sessionStars").child(sessionId));
        firebaseDatabaseLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    return;
                }
                SessionStars sessionStars = dataSnapshot.getValue(SessionStars.class);
                int total = sessionStars.getFive()+sessionStars.getFour()+sessionStars.getThree()+sessionStars.getTwo()+sessionStars.getOne();
                int fivePercentage = Math.round(((float) sessionStars.getFive()/ (float) total)*100);
                int fourPercentage = Math.round(((float) sessionStars.getFour()/ (float) total)*100);
                int threePercentage = Math.round(((float) sessionStars.getThree()/ (float) total)*100);
                int twoPercentage = Math.round(((float) sessionStars.getTwo()/ (float) total)*100);
                int onePercentage = Math.round(((float) sessionStars.getOne()/ (float) total)*100);
                fiveStarBar.setProgress(fivePercentage);
                fourStarBar.setProgress(fourPercentage);
                threeStarBar.setProgress(threePercentage);
                twoStarBar.setProgress(twoPercentage);
                oneStarBar.setProgress(onePercentage);

                int totalStars = (sessionStars.getFive()*5 + sessionStars.getFour()*4 + sessionStars.getThree()*3 + sessionStars.getTwo()*2 + sessionStars.getOne());
                float currentRating = (float) totalStars/total;

                int textSize1 = getResources().getDimensionPixelSize(R.dimen.text_Size_rating_big);
                int textSize2 = getResources().getDimensionPixelSize(R.dimen.text_Size_rating_total);

                String text1 = String.format("%.1f", currentRating);
                String text2 = "/5,0";

                SpannableString span1 = new SpannableString(text1);
                span1.setSpan(new AbsoluteSizeSpan(textSize1), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);

                SpannableString span2 = new SpannableString(text2);
                span2.setSpan(new AbsoluteSizeSpan(textSize2), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
                CharSequence finalText = TextUtils.concat(span1, span2);
                ratingTextView.setText(finalText);

                if (total==0) {
                    ratingNrTextView.setText(R.string.new_session_no_reviews_linebreak);
                } else if (total==1) {
                    ratingNrTextView.setText(getString(R.string.based_on) + total + "\n" + getString(R.string.rating));
                } else {
                    ratingNrTextView.setText(getString(R.string.based_on) + total + "\n" + getString(R.string.ratings));
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RatingsAndReviewsActivity.this);
        linearLayoutManager.setStackFromEnd(true);

        reviewList.setNestedScrollingEnabled(false);

        reviewList.setLayoutManager(linearLayoutManager);
        ((SimpleItemAnimator) reviewList.getItemAnimator()).setSupportsChangeAnimations(false);
        reviewList.setItemAnimator(null);

        listReviewseAdapter = new ListReviewsAdapter();
        reviewList.setAdapter(listReviewseAdapter);


        loadData(currentLastTimestamp);

        NestedScrollView scroller = (NestedScrollView) findViewById(R.id.scrollView);

        if (scroller != null) {
            scroller.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY == Math.abs( v.getMeasuredHeight() - v.getChildAt(0).getMeasuredHeight() )) {
                        //------------------- SCROLL IS AT BOTTOM ----------------------------//
                        progressBar.setVisibility(View.VISIBLE);
                        loadData(currentLastTimestamp);
                    }
                }
            });
        }
    }

    private void loadData(Long lastTimestamp) {
        Query keysQuery;
        if (lastTimestamp.equals(0L)) {
            keysQuery = rootDbRef.child("sessionReviews").child(sessionId).orderByValue().limitToLast(currentNumberOfReviews);
        } else {
            keysQuery = rootDbRef.child("sessionReviews").child(sessionId).orderByValue().endAt(lastTimestamp-1).limitToLast(currentNumberOfReviews);
        }

        // Listener to keep data in cache in sync with database
        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(RatingsAndReviewsActivity.this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(keysQuery);
        firebaseDatabaseLiveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                // Dummy listener
            }
        });


        keysQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    progressBar.setVisibility(View.GONE);
                    if (listReviewseAdapter.getItemCount()==0) {
                        noWrittenReviewsYet.setVisibility(View.VISIBLE);
                    }
                } else {

                    if (dataSnapshot.getChildrenCount()<currentNumberOfReviews) {
                        progressBar.setVisibility(View.GONE);
                    }
                    HashMap<String,Long> reviewKeys = (HashMap<String,Long>) dataSnapshot.getValue();

                    ArrayList<Task<?>> tasks = new ArrayList<>();

                    for (String key: reviewKeys.keySet()) {
                        if (currentLastTimestamp.equals(0L)) {
                            currentLastTimestamp = reviewKeys.get(key);
                        } else {
                            if (currentLastTimestamp>=reviewKeys.get(key)) {
                                currentLastTimestamp = reviewKeys.get(key);
                            }
                        }
                        TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
                        Task dbTask = dbSource.getTask();
                        DatabaseReference ref = rootDbRef.child("reviews").child(key);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dbSource.setResult(dataSnapshot);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                dbSource.setException(databaseError.toException());
                            }
                        });
                        tasks.add(dbTask);
                    }
                    Tasks.whenAll(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ArrayList<Review> reviews = new ArrayList<>();
                                for (Task finishedTask: tasks) {
                                    DataSnapshot dataSnapshot = (DataSnapshot) finishedTask.getResult();
                                    Review review = dataSnapshot.getValue(Review.class);
                                    reviews.add(review);
                                }
                                Collections.sort(reviews);
                                listReviewseAdapter.addData(reviews);
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
