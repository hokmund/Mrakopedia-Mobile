package com.randomname.mrakopedia.ui.pagesummary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.randomname.mrakopedia.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PageSummaryActivity extends AppCompatActivity {

    public static final String PAGE_NAME_EXTRA = "pageNameExtra";
    private static final String PAGE_FRAGMENT_TAG = "summaryFragmentTag";

    private PageSummaryFragment fragment;
    private boolean isSelectedMode = false;

    public RecyclerView.OnScrollListener toolbarHideListener;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.copy_toolbar)
    Toolbar copyToolbar;
    @Bind(R.id.toolbarWrapper)
    RelativeLayout toolbarWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_summary);
        ButterKnife.bind(this);

        String pageTitle = getIntent().getStringExtra(PAGE_NAME_EXTRA);

        if (pageTitle != null) {
            setPageSummaryFragment(pageTitle);
        }

        initToolbar(pageTitle);

        toolbarHideListener = new RecyclerView.OnScrollListener() {

            // Keeps track of the overall vertical offset in the list
            int verticalOffset;

            // Determines the scroll UP/DOWN direction
            boolean scrollingUp;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (scrollingUp) {
                        if (verticalOffset > toolbarWrapper.getHeight()) {
                            toolbarAnimateHide();
                        } else {
                            toolbarAnimateShow(verticalOffset);
                        }
                    } else {
                        if (toolbarWrapper.getTranslationY() < toolbarWrapper.getHeight() * -0.6 && verticalOffset > toolbarWrapper.getHeight()) {
                            toolbarAnimateHide();
                        } else {
                            toolbarAnimateShow(verticalOffset);
                        }
                    }
                }
            }

            @Override
            public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                verticalOffset += dy;
                scrollingUp = dy > 0;
                int toolbarYOffset = (int) (dy - toolbarWrapper.getTranslationY());
                toolbarWrapper.animate().cancel();
                if (scrollingUp) {
                    if (toolbarYOffset < toolbarWrapper.getHeight()) {
                        toolbarWrapper.setTranslationY(-toolbarYOffset);
                    } else {
                        toolbarWrapper.setTranslationY(-toolbarWrapper.getHeight());
                    }
                } else {
                    if (toolbarYOffset < 0) {
                        toolbarWrapper.setTranslationY(0);
                    } else {
                        toolbarWrapper.setTranslationY(-toolbarYOffset);
                    }
                }
            }
        };
    }

    private void toolbarAnimateShow(final int verticalOffset) {
        toolbarWrapper.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
    }

    private void toolbarAnimateHide() {
        toolbarWrapper.animate()
                .translationY(-toolbar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
    }

    public void startSelection() {
        setAlphaAnimation(copyToolbar, false);
        setAlphaAnimation(toolbar, true);

        isSelectedMode = true;
    }

    public void stopSelection() {
        setAlphaAnimation(copyToolbar, true);
        setAlphaAnimation(toolbar, false);


        fragment.cancelSelection();
        isSelectedMode = false;
    }

    private void setAlphaAnimation(final View view, final boolean hide) {
        float from = 0f;
        float to = 0f;

        if (hide) {
            from = 1f;
        } else {
            to = 1f;
        }

        AlphaAnimation alphaAnimation = new AlphaAnimation(from, to);
        alphaAnimation.setDuration(300);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(hide ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.clearAnimation();
        view.setAnimation(alphaAnimation);
        view.animate();

    }

    @Override
    public void onBackPressed() {
        if (isSelectedMode) {
            stopSelection();
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.copy_btn)
    public void copyBtnClick() {
        fragment.copySelectedText();
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

    private void initToolbar(String categoryTitle) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (categoryTitle != null) {
            getSupportActionBar().setTitle(categoryTitle);
        } else {
            getSupportActionBar().setTitle("");
        }

        copyToolbar.setTitle("Копирование");
        copyToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        copyToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelection();
            }
        });
    }

    private void setPageSummaryFragment(String pageTitle) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(PAGE_FRAGMENT_TAG);

        if (frag != null) {
            fragment = (PageSummaryFragment) frag;
            return;
        }

        fragment = PageSummaryFragment.getInstance(pageTitle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, PAGE_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}
