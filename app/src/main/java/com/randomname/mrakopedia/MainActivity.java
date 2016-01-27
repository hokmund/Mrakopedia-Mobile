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
import com.randomname.mrakopedia.ui.favorite.FavoriteFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private final static String ALL_CATEGORIES_FRAGMENT_TAG = "allCategoriesFragment";
    private final static String FAVORITE_FRAGMENT_TAG = "favoriteFragmentTag";

    private final int DRAWER_ALL_CATEGORIES = 0;
    private final int DRAWER_FAVORITE = 1;

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
        if (materialDrawer.isDrawerOpen()) {
            materialDrawer.closeDrawer();
            return;
        }

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
                    createDrawerItem(R.string.all_categories_drawer, DRAWER_ALL_CATEGORIES),
                    createDrawerItem(R.string.favorite_drawer, DRAWER_FAVORITE)
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
                        break;
                    case DRAWER_FAVORITE:
                        setFavoriteFragment();
                        break;
                    default:
                        break;
                }

                return false;
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
        setTitle(R.string.all_categories_drawer);
        setFragment(new AllCategoriesFragment(), ALL_CATEGORIES_FRAGMENT_TAG);
    }

    private void setFavoriteFragment() {
        setTitle(R.string.favorite_drawer);
        setFragment(new FavoriteFragment(), FAVORITE_FRAGMENT_TAG);
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
