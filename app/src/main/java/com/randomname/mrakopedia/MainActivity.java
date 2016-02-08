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
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.randomname.mrakopedia.ui.allcategories.AllCategoriesFragment;
import com.randomname.mrakopedia.ui.favorite.FavoriteFragment;
import com.randomname.mrakopedia.ui.recentchanges.RecentChangesFragment;
import com.randomname.mrakopedia.ui.search.SearchFragment;
import com.randomname.mrakopedia.ui.views.ToolbarHideRecyclerOnScrollListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private final static String ALL_CATEGORIES_FRAGMENT_TAG = "allCategoriesFragment";
    private final static String FAVORITE_FRAGMENT_TAG = "favoriteFragmentTag";
    private final static String RECENT_CHANGES_FRAGMENT_TAG = "recentChagesFragmentTag";
    private final static String SEARCH_FRAGMENT_TAG = "searchFragmentTag";

    private final static String DRAWER_SELECTION_KEY = "drawerSelectionKey";

    private final int DRAWER_ALL_CATEGORIES = 0;
    private final int DRAWER_FAVORITE = 1;
    private final int DRAWER_RECENT_CHANGES = 2;
    private final int DRAWER_SEARCH_FRAGMENT = 3;

    public ToolbarHideRecyclerOnScrollListener toolbarHideRecyclerOnScrollListener;

    private static final int TIME_INTERVAL = 1000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    private int drawerSelection = 0;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private Drawer materialDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            drawerSelection = savedInstanceState.getInt(DRAWER_SELECTION_KEY, 0);
        }

        initToolbar();
        initDrawer();

        toolbarHideRecyclerOnScrollListener = new ToolbarHideRecyclerOnScrollListener(toolbar);

        if (getSupportFragmentManager().getFragments() == null) {
            setAllCategoriesFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(DRAWER_SELECTION_KEY, drawerSelection);
        super.onSaveInstanceState(outState);
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
                    createDrawerItem(R.string.favorite_drawer, DRAWER_FAVORITE),
                    createDrawerItem(R.string.recent_changes_drawer, DRAWER_RECENT_CHANGES),
                    createDrawerItem(R.string.search_drawer, DRAWER_SEARCH_FRAGMENT)
                )
                .withSelectedItem(drawerSelection)
                .build();

        materialDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                if (iDrawerItem == null) {
                    return false;
                }

                drawerSelection = iDrawerItem.getIdentifier();

                switch (iDrawerItem.getIdentifier()) {
                    case DRAWER_ALL_CATEGORIES:
                        setAllCategoriesFragment();
                        break;
                    case DRAWER_FAVORITE:
                        setFavoriteFragment();
                        break;
                    case DRAWER_RECENT_CHANGES:
                        setRecentChangesFragment();
                        break;
                    case DRAWER_SEARCH_FRAGMENT:
                        setSearchFragment();
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

    private void setRecentChangesFragment() {
        setTitle(R.string.recent_changes_drawer);
        setFragment(new RecentChangesFragment(), RECENT_CHANGES_FRAGMENT_TAG);
    }

    private void setSearchFragment() {
        setTitle(R.string.search_drawer);
        setFragment(new SearchFragment(), SEARCH_FRAGMENT_TAG);
    }

    private void setFragment(Fragment fragment, String tag) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);

        toolbar.setTranslationY(0);
        toolbarHideRecyclerOnScrollListener.setVerticalOffset(0);

        if (frag != null) {
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, tag);
        fragmentTransaction.commit();
    }
}
