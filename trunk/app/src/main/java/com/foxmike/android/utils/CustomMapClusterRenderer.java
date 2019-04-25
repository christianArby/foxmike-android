package com.foxmike.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.foxmike.android.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by chris on 2019-04-25.
 */

public class CustomMapClusterRenderer <T extends ClusterItem> extends DefaultClusterRenderer<T> {

    private Context context;


    public CustomMapClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;


    }



    @Override
    protected void onBeforeClusterItemRendered(T item,
                                               MarkerOptions markerOptions) {

        Drawable locationDrawable = context.getResources().getDrawable(R.mipmap.baseline_location_on_black_36);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(locationDrawable.getIntrinsicWidth(), locationDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        locationDrawable.setBounds(0, 0, locationDrawable.getIntrinsicWidth(), locationDrawable.getIntrinsicHeight());
        locationDrawable.draw(canvas);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));

    }

}
