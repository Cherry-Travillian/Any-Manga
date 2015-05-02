/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Calvin Mikael
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.calvinmikael.anymanga;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.astuetz.PagerSlidingTabStrip;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;


public class WebViewFragment extends Fragment implements ObservableScrollViewCallbacks {
    private ProgressBar mProgressBar;
    private ObservableWebView mWebView;
    private View mCustomView;
    private int mOriginalSystemUiVisibility;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private PagerSlidingTabStrip mTabStrip;
    public static final String mSavedPage = "mSavedPage";
    private int mPage;

    public static WebViewFragment newInstance(int page) {
        Bundle bundle = new Bundle();
        bundle.putInt(mSavedPage, page);
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(mSavedPage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        mWebView = (ObservableWebView) view.findViewById(R.id.webView);
        mWebView.setScrollViewCallbacks(this);
        WebSettings settings = mWebView.getSettings();

        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!mProgressBar.isShown()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mProgressBar.isShown()) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // if a view already exists then immediately terminate the new one
                if (mCustomView != null) {
                    onHideCustomView();
                    return;
                }

                // Save the current state
                mCustomView = view;
                mOriginalSystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();

                // Save the custom view callback
                mCustomViewCallback = callback;

                // Add the custom view to the view hierarchy
                FrameLayout decorView = (FrameLayout) getActivity().getWindow().getDecorView();
                decorView.addView(mCustomView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                mTabStrip = (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs);
                mTabStrip.setVisibility(View.GONE);

                // Go fullscreen
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_FULLSCREEN);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }

            @Override
            public void onHideCustomView() {
                // Remove the custom view
                FrameLayout decorView = (FrameLayout) getActivity().getWindow().getDecorView();
                decorView.removeView(mCustomView);
                mCustomView = null;

                mTabStrip.setVisibility(View.VISIBLE);
                // Restore the original form
                getActivity().getWindow().getDecorView()
                        .setSystemUiVisibility(mOriginalSystemUiVisibility);

                // Call the custom view callback
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;
            }
        });
        // The back button must be handled within the mWebView for the
        // mWebView to have back behavior based on the current mPage
        // if back behavior is not handled with this listener then
        // back behavior will be entirely dependent on the first mPage
        mWebView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    mWebView.goBack();
                    return true;
                }

                return false;
            }
        });

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            if (mPage == 1) {
                mWebView.loadUrl(getString(R.string.website_kissmanga));
            } else if (mPage == 2) {
                mWebView.loadUrl(getString(R.string.website_mangapark));
            } else if (mPage == 3) {
                mWebView.loadUrl(getString(R.string.website_line_webtoon));
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onScrollChanged(int i, boolean b, boolean b2) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        mTabStrip = (PagerSlidingTabStrip) getActivity().findViewById(R.id.tabs);
        if (scrollState == ScrollState.UP) {
            if ((mTabStrip != null && mTabStrip.isShown())) {
                mTabStrip.setVisibility(View.GONE);
            }
        } else if (scrollState == ScrollState.DOWN) {
            if ((mTabStrip != null && !mTabStrip.isShown())) {
                mTabStrip.setVisibility(View.VISIBLE);
            }
        }
    }
}
