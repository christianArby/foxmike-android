package com.foxmike.android.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnUserClickedListener;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.FriendsViewHolder;

public class ListFriendsFirebaseAdapter extends FirebaseRecyclerAdapter<UserPublic, FriendsViewHolder> {

    private long mLastClickTime = 0;
    private Context context;
    private OnUserClickedListener onUserClickedListener;


    public ListFriendsFirebaseAdapter(@NonNull FirebaseRecyclerOptions<UserPublic> options, Context context, OnUserClickedListener onUserClickedListener) {
        super(options);
        this.context = context;
        this.onUserClickedListener = onUserClickedListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendsViewHolder friendsViewHolder, int i, @NonNull UserPublic userPublic) {

        friendsViewHolder.setFirstLetter("no");
        if (i==0) {
            friendsViewHolder.setFirstLetter(userPublic.getFirstName().substring(0,1));
        }
        if (i>0) {
            if (!this.getItem(i-1).getFirstName().substring(0,1).equals(userPublic.getFirstName().substring(0,1))) {
                friendsViewHolder.setFirstLetter(userPublic.getFirstName().substring(0,1));
            }
        }

        friendsViewHolder.setText(userPublic.getUserName(), true);
        friendsViewHolder.setHeading(userPublic.getFullName());
        friendsViewHolder.setUserImage(userPublic.getThumb_image(), context);
        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                onUserClickedListener.OnUserClicked(userPublic.getUserId());
            }
        });


    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_list_single_layout_mini_friend, parent, false);
        return new FriendsViewHolder(view);
    }
}
