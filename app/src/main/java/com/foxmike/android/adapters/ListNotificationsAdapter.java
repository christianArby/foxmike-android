package com.foxmike.android.adapters;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.fragments.NotificationsFragment;
import com.foxmike.android.models.FoxmikeNotification;
import com.foxmike.android.models.InAppNotification;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2019-05-06.
 */

public class ListNotificationsAdapter extends RecyclerView.Adapter<ListNotificationsAdapter.NotificationsViewHolder> {

    private NotificationsFragment.OnNotificationClickedListener onNotificationClickedListener;
    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private HashMap<String, InAppNotification> notificationHashMap = new HashMap<>();
    private HashMap<String, String> stringHashMap = new HashMap<>();
    private DataSnapshot unreadNotifications;
    private int unreadColor;
    private int readColor;

    private SortedList<FoxmikeNotification> foxmikeNotificationSortedList;

    public ListNotificationsAdapter(HashMap<String, String> stringHashMap, DataSnapshot unreadNotifications, int readColor, int unreadColor, NotificationsFragment.OnNotificationClickedListener onNotificationClickedListener) {

        this.onNotificationClickedListener = onNotificationClickedListener;
        this.stringHashMap = stringHashMap;
        this.unreadNotifications = unreadNotifications;
        this.unreadColor = unreadColor;
        this.readColor = readColor;

        foxmikeNotificationSortedList = new SortedList<FoxmikeNotification>(FoxmikeNotification.class, new SortedList.Callback<FoxmikeNotification>() {
            @Override
            public int compare(FoxmikeNotification foxmikeNotification, FoxmikeNotification t21) {
                return foxmikeNotification.getTimestamp().compareTo(t21.getTimestamp());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(FoxmikeNotification foxmikeNotification, FoxmikeNotification t21) {
                return t21.getTimestamp().equals(foxmikeNotification.getTimestamp());
            }

            @Override
            public boolean areItemsTheSame(FoxmikeNotification foxmikeNotification, FoxmikeNotification t21) {
                return t21.getTimestamp().equals(foxmikeNotification.getTimestamp());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }


    //conversation helpers
    public void addAll(List<FoxmikeNotification> foxmikeNotifications) {
        foxmikeNotificationSortedList.beginBatchedUpdates();
        for (int i = 0; i < foxmikeNotifications.size(); i++) {
            foxmikeNotificationSortedList.add(foxmikeNotifications.get(i));
        }
        foxmikeNotificationSortedList.endBatchedUpdates();
    }

    //conversation helpers
    public void add(FoxmikeNotification foxmikeNotification) {
        foxmikeNotificationSortedList.beginBatchedUpdates();
        foxmikeNotificationSortedList.add(foxmikeNotification);
        foxmikeNotificationSortedList.endBatchedUpdates();
    }

    public FoxmikeNotification get(int position) {
        return foxmikeNotificationSortedList.get(position);
    }

    public void clear() {
        foxmikeNotificationSortedList.beginBatchedUpdates();
        //remove items at end, to avoid unnecessary array shifting
        while (foxmikeNotificationSortedList.size() > 0) {
            foxmikeNotificationSortedList.removeItemAt(foxmikeNotificationSortedList.size() - 1);
        }
        foxmikeNotificationSortedList.endBatchedUpdates();
    }




    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_layout, viewGroup, false);
        return new NotificationsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder notificationsViewHolder, int i) {
        if (notificationHashMap.containsKey(foxmikeNotificationSortedList.get(i).getNotificationId())) {
            InAppNotification inAppNotification = notificationHashMap.get(foxmikeNotificationSortedList.get(i).getNotificationId());
            notificationsViewHolder.setNotificationImage(inAppNotification.getNotificationThumbnail());
            notificationsViewHolder.setNotificationText(inAppNotification.getNotificationText());
        }

        notificationsViewHolder.setNotificationClickedListener(foxmikeNotificationSortedList.get(i));
        notificationsViewHolder.setNotificationTime(TextTimestamp.textShortDateAndTime(foxmikeNotificationSortedList.get(i).getTimestamp()));

        if (unreadNotifications!=null) {
            if (unreadNotifications.hasChild(foxmikeNotificationSortedList.get(i).getNotificationId())) {
                notificationsViewHolder.setNotificationUnread(true);
            } else {
                notificationsViewHolder.setNotificationUnread(false);
            }
        } else {
            notificationsViewHolder.setNotificationUnread(false);
        }

    }

    @Override
    public int getItemCount() {
        return foxmikeNotificationSortedList.size();
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public void setNotificationClickedListener(FoxmikeNotification foxmikeNotification) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNotificationClickedListener.OnNotificationClicked(foxmikeNotification);
                    if (unreadNotifications!=null) {
                        if (unreadNotifications.hasChild(foxmikeNotification.getNotificationId())) {
                            FirebaseDatabase.getInstance().getReference().child("unreadNotifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(foxmikeNotification.getNotificationId()).setValue(null);
                        }
                    }
                }
            });
        }

        public NotificationsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setNotificationText(SpannableStringBuilder notificationText) {
            TextView notificationTextTV = (TextView) mView.findViewById(R.id.notification_text);
            notificationTextTV.setText(notificationText);
        }

        public void setNotificationTime(String notificationTime) {
            TextView notificationTimeTV = (TextView) mView.findViewById(R.id.notification_time);
            notificationTimeTV.setText(notificationTime);
        }

        public void setNotificationImage(String thumb_image) {
            CircleImageView notificationImageIV = (CircleImageView) mView.findViewById(R.id.notification_image);
            Glide.with(notificationImageIV.getContext()).load(thumb_image).into(notificationImageIV);
        }

        public void setNotificationUnread(boolean unread) {
            if (unread) {
                mView.setBackgroundColor(unreadColor);
            } else {
                mView.setBackgroundColor(readColor);
            }
        }
    }

    public void updateUnreadNotifications(DataSnapshot unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
        this.notifyDataSetChanged();
    }

    public void updateNotificationHashMap(HashMap<String, InAppNotification> notificationHashMap) {
        this.notificationHashMap = notificationHashMap;
    }
}
