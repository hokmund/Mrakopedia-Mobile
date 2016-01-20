package com.randomname.mrakopedia.ui.pagesummary;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.categorymembers.CategoryMembersFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PageSummaryActivity extends AppCompatActivity {

    public static final String PAGE_NAME_EXTRA = "pageNameExtra";
    private static final String PAGE_FRAGMENT_TAG = "summaryFragmentTag";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

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
    }

    private void setPageSummaryFragment(String pageTitle) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(PAGE_FRAGMENT_TAG);

        if (frag != null) {
            return;
        }

        PageSummaryFragment fragment = PageSummaryFragment.getInstance(pageTitle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, PAGE_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}
