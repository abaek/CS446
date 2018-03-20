package com.cs446.articleshare.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cs446.articleshare.R;
import com.cs446.articleshare.views.AspectRatioImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class ScreenshotGridViewAdapter extends BaseAdapter {
    private final Context context;
    private Display display;
    private ScreenshotGridViewAdapter.Callback callback;
    private List<String> urls;

    public ScreenshotGridViewAdapter(Context context, Display display, ScreenshotGridViewAdapter.Callback callback) {
        this.context = context;
        this.display = display;
        this.callback = callback;
        urls = getCameraImages(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AspectRatioImageView view = (AspectRatioImageView) convertView;
        if (view == null) {
            view = getScreenshotThumbnailView();
        }

        // Get the image URL for the current position.
        final Uri source = getItem(position);

        Picasso.with(context)
                .load(source)
                .placeholder(R.color.md_teal_800)
                .error(R.color.md_blue_grey_800)
                .fit()
                .centerCrop()
                .tag(context)
                .into(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onImageClicked(source);
            }
        });

        return view;
    }

    private static List<String> getCameraImages(Context context) {
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};

        final Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                MediaStore.Images.Media.DATA + " like ? ",
                new String[]{"%/Screenshots/%"},
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
        if (cursor == null) {
            return new ArrayList<>();
        }
        ArrayList<String> result = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private AspectRatioImageView getScreenshotThumbnailView() {
        AspectRatioImageView view;
        Point size = new Point();
        display.getSize(size);
        float ratio = ((float) size.y) / size.x;

        view = new AspectRatioImageView(context, ratio);
        view.setScaleType(CENTER_CROP);
        return view;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Uri getItem(int position) {
        return Uri.fromFile(new File(urls.get(position)));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface Callback {
        void onImageClicked(Uri imageUri);
    }

}
