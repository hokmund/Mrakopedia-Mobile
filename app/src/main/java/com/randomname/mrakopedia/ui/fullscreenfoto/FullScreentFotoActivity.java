package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.nineoldandroids.animation.ObjectAnimator;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FullScreentFotoActivity extends AppCompatActivity {

    public static String IMAGE_ARRAY_KEY = "image_link_key";
    public static String SELECTED_IMAGE_KEY = "selected_image_key";

    private static final String FULL_SCREEN_FRAGMENT_HOST = "full_screen_fragment_host";

    private static final String TOOLBAR_IS_SHOWED_KEY = "toolbar_is_showed_key";

    private boolean toolbarIsShowed;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screent_foto);
        ButterKnife.bind(this);
        ArrayList<String> imagesArray = getIntent().getStringArrayListExtra(IMAGE_ARRAY_KEY);
        int selectedPosition = getIntent().getIntExtra(SELECTED_IMAGE_KEY, 0);

        if (getSupportFragmentManager().getFragments() == null) {
            Fragment frag = getSupportFragmentManager().findFragmentByTag(FULL_SCREEN_FRAGMENT_HOST);

            if (frag != null) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putStringArrayList(FullScreenPhotoFragmentHost.PHOTOS_ARRAY_KEY, imagesArray);
            bundle.putInt(FullScreenPhotoFragmentHost.POSITION_KEY, selectedPosition);
            FullScreenPhotoFragmentHost fullScreenPhotoFragmentHost = FullScreenPhotoFragmentHost.getInstance(bundle);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fullScreenPhotoFragmentHost, FULL_SCREEN_FRAGMENT_HOST);
            fragmentTransaction.commit();
        }

        if (savedInstanceState != null) {
            toolbarIsShowed = savedInstanceState.getBoolean(TOOLBAR_IS_SHOWED_KEY);
        } else {
            toolbarIsShowed = true;
        }

        initToolbar();

        if (!toolbarIsShowed) {
            final ViewTreeObserver observer= toolbar.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ObjectAnimator animation = ObjectAnimator.ofFloat(toolbar, "translationY", -toolbar.getHeight());
                    animation.setDuration(0).start();

                    if (Build.VERSION.SDK_INT < 16) {
                        toolbar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TOOLBAR_IS_SHOWED_KEY, toolbarIsShowed);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        if (Utils.checkForLollipop()) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        TextView titleTextView = null;

        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);

            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setFocusable(true);
            titleTextView.setFocusableInTouchMode(true);
            titleTextView.requestFocus();
            titleTextView.setSingleLine(true);
            titleTextView.setSelected(true);
            titleTextView.setMarqueeRepeatLimit(-1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showHideToolbar() {
        if (toolbarIsShowed) {
            hideToolbar();
            toolbarIsShowed = false;
        } else {
            showToolbar();
            toolbarIsShowed = true;
        }
    }

    private void hideToolbar() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(toolbar, "translationY", -toolbar.getHeight());
        animator.setInterpolator(new AccelerateInterpolator(2));
        animator.start();
    }

    private void showToolbar() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(toolbar, "translationY", 0);
            animator.setInterpolator(new AccelerateInterpolator(2));
            animator.start();
    }
}
