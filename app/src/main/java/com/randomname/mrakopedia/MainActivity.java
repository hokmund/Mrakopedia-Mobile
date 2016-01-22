package com.randomname.mrakopedia;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.randomname.mrakopedia.ui.allcategories.AllCategoriesFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private final static String ALL_CATEGORIES_FRAGMENT_TAG = "allCategoriesFragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbar();

        if (getSupportFragmentManager().getFragments() == null) {
            setAllCategoriesFragment();
        }
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setAllCategoriesFragment() {
        setFragment(new AllCategoriesFragment(), ALL_CATEGORIES_FRAGMENT_TAG);
    }

    private void setFragment(Fragment fragment, String tag) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);

        if (frag != null) {
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, tag);
        fragmentTransaction.commit();
    }
}
