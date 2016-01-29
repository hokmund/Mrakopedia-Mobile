package com.randomname.mrakopedia.ui.pagesummary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.randomname.mrakopedia.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PageSummaryActivity extends AppCompatActivity {

    public static final String PAGE_NAME_EXTRA = "pageNameExtra";
    private static final String PAGE_FRAGMENT_TAG = "summaryFragmentTag";

    private PageSummaryFragment fragment;
    private boolean isSelectedMode = false;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.copy_toolbar)
    Toolbar copyToolbar;

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
    }

    public void startSelection() {
        copyToolbar.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);

        isSelectedMode = true;
    }

    public void stopSelection() {
        copyToolbar.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);


        fragment.cancelSelection();
        isSelectedMode = false;
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
