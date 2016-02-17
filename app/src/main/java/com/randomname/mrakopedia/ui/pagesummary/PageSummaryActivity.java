package com.randomname.mrakopedia.ui.pagesummary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.settings.SettingsWorker;
import com.randomname.mrakopedia.ui.views.ToolbarHideRecyclerOnScrollListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PageSummaryActivity extends AppCompatActivity {

    public static final String PAGE_NAME_EXTRA = "pageNameExtra";
    public static final String PAGE_ID_EXTRA = "pageIdExtra";
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
    @Bind(R.id.shadow_view_copy)
    View shadowViewCopy;

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
        setContentView(R.layout.activity_page_summary);
        ButterKnife.bind(this);

        String pageTitle = getIntent().getStringExtra(PAGE_NAME_EXTRA);
        String pageId = getIntent().getStringExtra(PAGE_ID_EXTRA);

        if (pageTitle == null && pageId == null) {
            pageTitle = getIntent().getData().getQueryParameter("pageTitle");
        }

        if (pageTitle != null) {
            pageTitle = pageTitle.replaceAll("_", " ");
        }

        setPageSummaryFragment(pageTitle, pageId);
        initToolbar(pageTitle);

        toolbarHideListener = new ToolbarHideRecyclerOnScrollListener(toolbarWrapper);

        if (SettingsWorker.getInstance(this).isKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public boolean toolbarIsHidden() {
        return ((ToolbarHideRecyclerOnScrollListener)toolbarHideListener).isHidden();
    }

    public void startSelection() {
        setAlphaAnimation(copyToolbar, false);
        setAlphaAnimation(shadowViewCopy, false);

        isSelectedMode = true;
    }

    public void stopSelection() {
        setAlphaAnimation(copyToolbar, true);
        setAlphaAnimation(shadowViewCopy, true);

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

        view.setVisibility(View.VISIBLE);
        view.clearAnimation();
        view.setAnimation(alphaAnimation);
        view.animate();

    }

    @Override
    public void onBackPressed() {
        if (isSelectedMode) {
            stopSelection();
        } else {
            if (!fragment.onBackPressed()) {
                super.onBackPressed();
            }
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

    private void setPageSummaryFragment(String pageTitle, String pageId) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(PAGE_FRAGMENT_TAG);

        if (frag != null) {
            fragment = (PageSummaryFragment) frag;
            return;
        }

        fragment = PageSummaryFragment.getInstance(pageTitle, pageId);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, PAGE_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}
