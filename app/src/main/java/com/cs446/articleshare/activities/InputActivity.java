package com.cs446.articleshare.activities;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs446.articleshare.App;
import com.cs446.articleshare.R;
import com.cs446.articleshare.Util;
import com.cs446.articleshare.adapters.ScreenshotGridViewAdapter;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;

public class InputActivity extends AppCompatActivity {
    private GridView gv;
    private TextView selectScreenshot;
    private TextView noScreenshot;
    private Button pasteButton;

    private ClipboardManager clipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        initializeViews();

        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    private void initializeViews() {
        selectScreenshot = (TextView) findViewById(R.id.select_screenshot);
        noScreenshot = (TextView) findViewById(R.id.no_screenshot);
        pasteButton = (Button) findViewById(R.id.paste_button);
        gv = (GridView) findViewById(R.id.image_grid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        updateScreenshotGallery();
                    }

                    // TODO show button requesting permission again, on permission denied
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
        } else {
            updateScreenshotGallery();
        }

        if (Util.clipboardEmpty(clipboard)) {
            disablePasteButton();
        } else {
            enablePasteButton();
        }
    }

    private void enablePasteButton() {
        pasteButton.setEnabled(true);
        pasteButton.setText(getString(R.string.paste_instructions));
        pasteButton.setTextColor(Color.WHITE);
    }

    private void disablePasteButton() {
        pasteButton.setEnabled(false);
        pasteButton.setText(getString(R.string.clipboard_empty));
        pasteButton.setTextColor(getResources().getColor(R.color.white_trans_65));
    }

    public void pasteClipboard(View view) {
        String pasteData = Util.getTextFromClipboard(this, clipboard);

        String excerpt;
        if (pasteData != null) {
            excerpt = pasteData.trim();
        } else {
            return;
        }

        if (excerpt.length() <= 0) {
            showEmptyClipboardError();
            return;
        }

        if (App.getInstance().isNetworkAvailable()) {
            pushToCustomizePage(excerpt);
        } else {
            showNoNetworkError();
        }
    }

    private void showEmptyClipboardError() {
        Toast.makeText(getApplicationContext(),
                getString(R.string.empty_clipboard_toast),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void showNoNetworkError() {
        CharSequence text = getString(R.string.no_internet_error);
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void pushToCustomizePage(String excerpt) {
        Intent intent = new Intent(this, CustomizeActivity.class);
        intent.setAction(Intent.ACTION_DEFAULT);
        intent.putExtra(Util.EXCERPT, excerpt);
        startActivity(intent);
    }

    private void updateScreenshotGallery() {
        ScreenshotGridViewAdapter gvAdapter = new ScreenshotGridViewAdapter(
            this,
            getWindowManager().getDefaultDisplay(),
            new ScreenshotGridViewAdapter.Callback() {
                @Override
                public void onImageClicked(Uri imageUri) {
                    startCropper(imageUri);
                }
            });
        gv.setAdapter(gvAdapter);

        if (gvAdapter.isEmpty()) {
            noScreenshot.setVisibility(View.VISIBLE);
            selectScreenshot.setVisibility(View.GONE);
        } else {
            noScreenshot.setVisibility(View.GONE);
            selectScreenshot.setVisibility(View.VISIBLE);
        }
    }

    private void startCropper(Uri imageUri) {
        if (imageUri == null) {
            // TODO show error
            return;
        }
        if (imageUri.toString() == null || imageUri.toString().isEmpty()) {
            // TODO show error
            return;
        }
        CropImage.activity(imageUri).start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
                    Frame imageFrame = new Frame.Builder()
                            .setBitmap(bitmap)
                            .build();

                    SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

                    StringBuilder recognizedText = new StringBuilder();
                    for (int i = 0; i < textBlocks.size(); i++) {
                        TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                        String text = textBlock.getValue();
                        recognizedText.append(text);
                    }
                    pushToCustomizePage(recognizedText.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // TODO handle error
                Exception error = result.getError();
            }
        }
    }
}

