package com.cs446.articleshare;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs446.articleshare.views.AspectRatioImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class InputActivity extends BaseActivity {

    private static final int REQUEST_PERMISSIONS = 20;

    private GridView gv;
    private GridViewAdapter gvAdapter;
    private TextView selectScreenshot;
    private TextView noScreenshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        initializeViews();
        requestPermission();
    }

    @Override
    public void onPermissionsGranted(final int requestCode) {
        Toast.makeText(this, "Permissions Received.", Toast.LENGTH_LONG).show();
    }

    private void initializeViews() {
        selectScreenshot = (TextView) findViewById(R.id.select_screenshot);
        noScreenshot = (TextView) findViewById(R.id.no_screenshot);
        gv = (GridView) findViewById(R.id.image_grid);
    }

    private void requestPermission() {
        this.requestAppPermissions(new
                        String[]{Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, R.string.app_name
                , REQUEST_PERMISSIONS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScreenshotGallery();
    }

    private void updateScreenshotGallery() {
        gvAdapter = new GridViewAdapter(this);
        gv.setAdapter(gvAdapter);

        if (gvAdapter.isEmpty()) {
            noScreenshot.setVisibility(View.VISIBLE);
            selectScreenshot.setVisibility(View.GONE);
        } else {
            noScreenshot.setVisibility(View.GONE);
            selectScreenshot.setVisibility(View.VISIBLE);
        }
    }


    public static List<String> getCameraImages(Context context) {
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


    class GridViewAdapter extends BaseAdapter {
        private final Context context;
        List<String> urls;

        public GridViewAdapter(Context context) {
            this.context = context;
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
                    .placeholder(R.color.material_grey_300)
                    .error(R.color.material_blue_grey_800)
                    .fit()
                    .centerCrop()
                    .tag(context)
                    .into(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openScreenshot(source);
                }
            });

            return view;
        }

        private AspectRatioImageView getScreenshotThumbnailView() {
            AspectRatioImageView view;
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            float ratio = ((float) size.y) / size.x;

            view = new AspectRatioImageView(context, ratio);
            view.setScaleType(CENTER_CROP);
            return view;
        }

        private void openScreenshot(Uri source) {
            if (source.toString() == null || source.toString().isEmpty()) {
                // TODO show error
                return;
            }

            try {
                Bitmap img = MediaStore.Images.Media.getBitmap(InputActivity.this.getContentResolver(), source);
                if (img == null) {
                    // TODO show error
                    return;
                }
            } catch (IOException e) {
                // TODO show error
                return;
            }

            pushToCropPage(source);
        }

        private void pushToCropPage(Uri source) {
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
    }


}
