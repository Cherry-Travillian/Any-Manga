package io.github.calvinmikael.anymanga;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class WebViewFragmentPagerAdapter extends FragmentPagerAdapter {
    private final String mTabTitles[] = new String[] { "KissManga", "MangaPark", "Webtoon"};

    public WebViewFragmentPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return mTabTitles.length;
    }

    @Override
    public Fragment getItem(int page) {
        return WebViewFragment.newInstance(page + 1);
    }

    @Override
    public CharSequence getPageTitle(int page) {
        // Generate title based on item page
        return mTabTitles[page];
    }
}