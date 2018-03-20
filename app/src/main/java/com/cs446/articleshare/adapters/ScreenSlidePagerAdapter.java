package com.cs446.articleshare.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cs446.articleshare.fragments.ColourPickerFragment;
import com.cs446.articleshare.fragments.SourcePickerFragment;

public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
    public static final int COLOUR_ITEM = 0;
    public static final int SOURCE_ITEM = 1;

    private SourcePickerFragment sourcePickerFragment = null;

    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public CharSequence getPageTitle(int position) {
        switch(position) {
            case COLOUR_ITEM:
                return ColourPickerFragment.title();
            case SOURCE_ITEM:
                return SourcePickerFragment.title();
            default:
                throw new RuntimeException(
                        "Unexpected position for ScreenSlidePagerAdapter getPageTitle"
                );
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case COLOUR_ITEM:
                return ColourPickerFragment.newInstance();
            case SOURCE_ITEM:
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

    public SourcePickerFragment getSourcePickerFragment() {
        return sourcePickerFragment;
    }
}
