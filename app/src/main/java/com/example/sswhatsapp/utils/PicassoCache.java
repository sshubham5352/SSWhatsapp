package com.example.sswhatsapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.sswhatsapp.R;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PicassoCache {

    /**
     * Static Picasso Instance
     */
    private static Picasso picassoInstance = null;

    /**
     * PicassoCache Constructor
     *
     * @param context application Context
     */
    private PicassoCache(Context context) {

        Downloader downloader = new OkHttp3Downloader(context, Integer.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(downloader);

        picassoInstance = builder.build();
    }

    /**
     * Get Singleton Picasso Instance
     *
     * @param context application Context
     * @return Picasso instance
     */
    public static Picasso getPicassoInstance(Context context) {

        if (picassoInstance == null) {

            new PicassoCache(context);
            return picassoInstance;
        }

        return picassoInstance;
    }

    //util methods
    public static Bitmap getBitmap(Context context, Resources resources, String imgUrl, int placeHolder) {
        try {
            return PicassoCache.getPicassoInstance(context).load(imgUrl).get();
        } catch (IOException e) {
            //
        }
        return BitmapFactory.decodeResource(resources, placeHolder);
    }
}
