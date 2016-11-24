package com.randomname.mrakopedia.ui.categorymembers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.views.ToolbarHideRecyclerOnScrollListener;
import com.randomname.mrakopedia.ui.views.materialsearch.MaterialSearchView;
import com.randomname.mrakopedia.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CategoryMembersActivity extends AppCompatActivity {

    public static final String CATEGORY_NAME_EXTRA = "categoryNameExtra";
    private static final String CATEGORY_FRAGMENT_TAG = "categoryFragmentTag";

    public ToolbarHideRecyclerOnScrollListener toolbarHideRecyclerOnScrollListener;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.search_view)
    MaterialSearchView searchView;
    @Bind(R.id.toolbar_container)
    RelativeLayout toolbarContainer;

    private CategoryMembersFragment fragment;

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
        setContentView(R.layout.activity_category_members);
        ButterKnife.bind(this);

        String categoryTitle = getIntent().getStringExtra(CATEGORY_NAME_EXTRA);

        if (categoryTitle == null && getIntent() != null && getIntent().getData() != null) {
            categoryTitle = getIntent().getData().getQueryParameter("categoryTitle");
        }

        if (categoryTitle != null) {
            setCategoryMembersFragment(categoryTitle);
        }

        initToolbar(categoryTitle);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fragment.setFilter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fragment.setFilter(newText);
                return false;
            }
        });

        toolbarHideRecyclerOnScrollListener = new ToolbarHideRecyclerOnScrollListener(toolbarContainer);
    }

    public void setSearchMenuItem(MenuItem item) {
        searchView.setMenuItem(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (categoryTitle != null) {
            getSupportActionBar().setTitle(categoryTitle);
        } else {
            getSupportActionBar().setTitle("");
        }

        Utils.setRippleToToolbarIcon(toolbar, this);
    }

    private void setCategoryMembersFragment(String categoryTitle) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(CATEGORY_FRAGMENT_TAG);

        if (frag != null) {
            fragment = (CategoryMembersFragment) frag;
            return;
        }

        fragment = CategoryMembersFragment.getInstance(categoryTitle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, CATEGORY_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}
