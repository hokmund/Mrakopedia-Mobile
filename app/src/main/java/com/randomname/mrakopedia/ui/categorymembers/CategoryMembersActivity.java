package com.randomname.mrakopedia.ui.categorymembers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.randomname.mrakopedia.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CategoryMembersActivity extends AppCompatActivity {

    public static final String CATEGORY_NAME_EXTRA = "categoryNameExtra";
    private static final String CATEGORY_FRAGMENT_TAG = "categoryFragmentTag";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_members);
        ButterKnife.bind(this);

        String categoryTitle = getIntent().getStringExtra(CATEGORY_NAME_EXTRA);

        if (categoryTitle != null) {
            setCategoryMembersFragment(categoryTitle);
        }

        initToolbar(categoryTitle);
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

    private void setCategoryMembersFragment(String categoryTitle) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(CATEGORY_FRAGMENT_TAG);

        if (frag != null) {
            return;
        }

        CategoryMembersFragment fragment = CategoryMembersFragment.getInstance(categoryTitle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, CATEGORY_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}