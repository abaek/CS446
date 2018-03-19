package com.cs446.articleshare;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cs446.articleshare.fragments.ColourPickerFragment;
import com.cs446.articleshare.fragments.Source;
import com.cs446.articleshare.fragments.SourcePickerFragment;
import com.cs446.articleshare.tasks.BingSearchResults;
import com.cs446.articleshare.tasks.WebSearchAsyncTask;
import com.cs446.articleshare.views.MaxHeightScrollView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hold1.pagertabsindicator.PagerTabsIndicator;

import java.io.ByteArrayOutputStream;

import static com.cs446.articleshare.Util.EXCERPT;
import static com.cs446.articleshare.Util.getBitmapFromView;

public class CustomizeActivity
        extends AppCompatActivity
        implements ColourPickerFragment.ColourReceiver, SourcePickerFragment.OnSourceUpdateListener
{
    public static final String IMAGE = "IMAGE";
    public static final String URL = "URL";
    private static final double MAX_SCROLL_VIEW_HEIGHT = 0.5; // as percentage of screen height
    private static final int NUM_RESULTS = 3;
    // TODO don't use hard-coded string
    public static final String DEFAULT_COLOUR = "#009688"; // teal

    private WebSearchAsyncTask currentTask = null;

    private LinearLayout backgroundView;
    private TextView titleView;
    private TextView websiteView;

    private TextView contentPreview;
    private MaxHeightScrollView scrollView;
    private PagerTabsIndicator tabs;
    private PagerAdapter mPagerAdapter;
    private MenuItem nextItem;

    private SourcePickerFragment sourcePickerFragment;

    private String excerpt = null;
    private String selectedUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);

        backgroundView = (LinearLayout) findViewById(R.id.background);
        titleView = (TextView) findViewById(R.id.title);
        websiteView = (TextView) findViewById(R.id.website);

        // TODO temp values
        titleView.setText("WEBPAGE TITLE HERE");
        websiteView.setText("WEBPAGE DOMAIN HERE");

        scrollView = (MaxHeightScrollView) findViewById(R.id.preview_scroll);
        initializeScrollView(scrollView);

        contentPreview = (TextView) findViewById(R.id.content_preview);
        formatSerifText(contentPreview);
        initializeHighlightBehaviour(contentPreview);
        contentPreview.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
                menu.clear();
                mode.getMenuInflater().inflate(R.menu.highlight, menu);
                return true;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.done:
                        share();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });

        setColour(Color.parseColor(DEFAULT_COLOUR));

        initPager();
    }

    @Override
    public void onResume() {
        super.onResume();
        String newExcerpt = getExcerptFromIntent();
        if(newExcerpt != null && !newExcerpt.equals(excerpt)) {
            excerpt = newExcerpt;
            contentPreview.setText(excerpt);
            performSearch(excerpt);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentTask != null) currentTask.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.highlight, menu);
        nextItem = menu.getItem(0);
        nextItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                share();
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void performSearch(String query) {
        titleView.setText(getString(R.string.loading));
        websiteView.setText(getString(R.string.loading));


        WebSearchAsyncTask searchTask = new WebSearchAsyncTask(query, NUM_RESULTS, new WebSearchAsyncTask.WebSearchAsyncTaskCallback() {
            @Override
            public void onComplete(Object o, Error error) {
                if (sourcePickerFragment == null) {
                    throw new RuntimeException("source picker fragment was null");
                }
                BingSearchResults webSearchResults = (BingSearchResults) o;
                JsonObject resultsJson = new JsonParser()
                        .parse(webSearchResults.getJson())
                        .getAsJsonObject();
                sourcePickerFragment.onSourceUpdated(resultsJson, error);
            }
        });

        if (currentTask != null) currentTask.cancel(true);
        currentTask = searchTask;
        searchTask.execute();
    }

    private void initPager() {
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        tabs = (PagerTabsIndicator) findViewById(R.id.tabs);
        tabs.setViewPager(mPager);
    }

    private void initializeScrollView(MaxHeightScrollView v) {
        // set scroll view (content preview's parent) to max height
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        v.setMaxHeight((int) (MAX_SCROLL_VIEW_HEIGHT * height));
    }

    public void setColour(int colour) {
        backgroundView.setBackgroundColor(colour);

        // set highlight colour to a lighter version of colour
        String hexColour = String.format("#%06X", (0xFFFFFF & colour));
        String highlightColour = "#40" + hexColour.substring(1);
        contentPreview.setHighlightColor(Color.parseColor(highlightColour));
    }

    private void formatSerifText(TextView v) {
        final float TEXT_SIZE = 16;
        v.setTypeface(Typeface.SERIF);
        v.setTextColor(Color.BLACK);
        v.setTextSize(TEXT_SIZE);
    }

    private void initializeHighlightBehaviour(final TextView v) {
        v.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                setColour(Color.parseColor(DEFAULT_COLOUR));
            }

            public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
                return true;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.done:
                        share();
                        return true;
                }
                return false;
            }
        });

        final GestureDetector gestureDetector = new GestureDetector(
                this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        v.clearFocus();
                        return true;
                    }
                });
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private String getExcerptFromIntent() {
        Intent intent = getIntent();
        String intentAction = intent.getAction();

        boolean fromInternal = intentAction != null && intentAction.equals(Intent.ACTION_DEFAULT);
        boolean fromExternal = intentAction != null && intentAction.equals(Intent.ACTION_SEND);

        if(fromExternal) {
            return intent.getStringExtra(Intent.EXTRA_TEXT).trim();
        } else if(fromInternal){
            return intent.getStringExtra(EXCERPT);
        }

        // TODO throw an exception instead?
        Log.e("CustomizeActivity", "Unexpected intent, excerpt String returned null");
        return null;
    }

    private void share() {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(URL, selectedUrl);

        Bitmap image = getBitmapFromView(
                backgroundView,
                backgroundView.getHeight(),
                backgroundView.getWidth()
        );
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        intent.putExtra(IMAGE, byteArray);
        startActivity(intent);
    }

    @Override
    public void onColourUpdate(int colour) {
        setColour(colour);
    }

    @Override
    public void onSourceSelected(final Source source) {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                final ObjectAnimator animScrollToTop =
                        ObjectAnimator.ofInt(scrollView, "scrollY", scrollView.getBottom());
                animScrollToTop.setDuration(500);
                animScrollToTop.start();

                String title = source.getTitle();
                if(title == null || title.isEmpty()){
                    titleView.setVisibility(View.GONE);
                }else{
                    titleView.setText(title);
                    titleView.setVisibility(View.VISIBLE);
                }
                websiteView.setVisibility(View.VISIBLE);
                websiteView.setText(source.getDisplayUrl());
                selectedUrl = source.getUrl();
            }
        });
        nextItem.setEnabled(true);
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        // TODO don't hardcode positions

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return getString(ColourPickerFragment.title());
                case 1:
                    return getString(SourcePickerFragment.title());
                default:
                    throw new RuntimeException(
                            "Unexpected position for ScreenSlidePagerAdapter getPageTitle"
                    );
            }
        }

        @Override
        public Fragment getItem(int position) {
            // TODO
            switch(position) {
                case 0:
                    return ColourPickerFragment.newInstance();
                case 1:
                    sourcePickerFragment = SourcePickerFragment.newInstance();
                    return sourcePickerFragment;
                default:
                    throw new RuntimeException(
                            "Unexpected position for ScreenSlidePagerAdapter getItem"
                    );
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
