package com.cs446.articleshare;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;


public class Util {
    static final String EXCERPT = "com.cs446.articleshare.excerpt";
    static final String IMAGEURI = "com.cs446.articleshare.imageurl";

    // Clipboard util

    public static String getTextFromClipboard(Activity activity, ClipboardManager clipboard) {
        if (clipboard.getPrimaryClip() == null) {
            Toast.makeText(activity,
                    activity.getString(R.string.empty_clipboard_toast),
                    Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        if (item == null) {
            Toast.makeText(activity,
                    activity.getString(R.string.empty_clipboard_toast),
                    Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
        CharSequence text = item.getText();
        if (text == null) {
            Toast.makeText(activity,
                    activity.getString(R.string.clipboard_no_text),
                    Toast.LENGTH_SHORT)
                    .show();
            return null;
        }

        return text.toString();
    }

    static boolean clipboardEmpty(ClipboardManager clipboard) {
        ClipData primaryClip = clipboard.getPrimaryClip();
        return primaryClip == null
                || primaryClip.getItemAt(0) == null
                || primaryClip.getItemAt(0).getText() == null
                || primaryClip.getItemAt(0).getText().toString().trim().isEmpty();
    }

    // Bitmap util

    static Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {
        final int MAX_IMAGE_HEIGHT = 1920;

        int height = Math.min(MAX_IMAGE_HEIGHT, totalHeight);
        float percent = height / (float) totalHeight;

        Bitmap canvasBitmap = Bitmap.createBitmap(
                (int) (totalWidth * percent),
                (int) (totalHeight * percent),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(canvasBitmap);

        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);

        canvas.save();
        canvas.scale(percent, percent);
        view.draw(canvas);
        canvas.restore();

        return canvasBitmap;
    }
}
