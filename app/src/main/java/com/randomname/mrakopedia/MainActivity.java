package com.randomname.mrakopedia;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.randomname.mrakopedia.ui.allcategories.AllCategoriesFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private final static String ALL_CATEGORIES_FRAGMENT_TAG = "allCategoriesFragment";

    private final int DRAWER_ALL_CATEGORIES = 0;

    private static final int TIME_INTERVAL = 1000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Drawer materialDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbar();
        initDrawer();

        if (getSupportFragmentManager().getFragments() == null) {
            setAllCategoriesFragment();
        }
    }

    @Override
    public void onBackPressed() {

        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, R.string.press_back_again_to_exit, Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void initDrawer() {
        materialDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                    createDrawerItem(R.string.all_categories_drawer, DRAWER_ALL_CATEGORIES)
                )
                .build();

        materialDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                if (iDrawerItem == null) {
                    return false;
                }

                switch (iDrawerItem.getIdentifier()) {
                    case DRAWER_ALL_CATEGORIES:
                        setAllCategoriesFragment();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private PrimaryDrawerItem createDrawerItem(int name, int index) {
        PrimaryDrawerItem item = new PrimaryDrawerItem()
                .withName(name)
                .withIdentifier(index);

        return item;
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
