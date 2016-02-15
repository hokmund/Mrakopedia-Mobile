package com.randomname.mrakopedia;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.randomname.mrakopedia.ui.allcategories.AllCategoriesFragment;
import com.randomname.mrakopedia.ui.favorite.FavoriteFragment;
import com.randomname.mrakopedia.ui.recentchanges.RecentChangesFragment;
import com.randomname.mrakopedia.ui.search.SearchCallback;
import com.randomname.mrakopedia.ui.search.SearchFragment;
import com.randomname.mrakopedia.ui.settings.SettingsFragment;
import com.randomname.mrakopedia.ui.views.ToolbarHideRecyclerOnScrollListener;
import com.randomname.mrakopedia.ui.views.materialsearch.MaterialSearchView;
import com.randomname.mrakopedia.utils.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private final static String ALL_CATEGORIES_FRAGMENT_TAG = "allCategoriesFragment";
    private final static String FAVORITE_FRAGMENT_TAG = "favoriteFragmentTag";
    private final static String RECENT_CHANGES_FRAGMENT_TAG = "recentChagesFragmentTag";
    private final static String SEARCH_FRAGMENT_TAG = "searchFragmentTag";
    private final static String SETTINGS_FRAGMENT_TAG = "settingsFragmentTag";

    private final static String DRAWER_SELECTION_KEY = "drawerSelectionKey";

    private final int DRAWER_ALL_CATEGORIES = 0;
    private final int DRAWER_FAVORITE = 1;
    private final int DRAWER_RECENT_CHANGES = 2;
    private final int DRAWER_SEARCH_FRAGMENT = 3;
    private final int DRAWER_SETTINGS_FRAGMENT = 4;

    public ToolbarHideRecyclerOnScrollListener toolbarHideRecyclerOnScrollListener;

    private static final int TIME_INTERVAL = 1000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    private int drawerSelection = 0;
    private ArrayList<SearchCallback>searchListeners = new ArrayList<>();

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.toolbar_container)
    RelativeLayout toolbarContainer;
    @Bind(R.id.search_view)
    MaterialSearchView searchView;
    @Bind(R.id.shadow_view)
    View shadowView;

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

        toolbarHideRecyclerOnScrollListener = new ToolbarHideRecyclerOnScrollListener(toolbarContainer);

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
        searchView.setUpButtonIcon(R.drawable.ic_menu_black_24dp);
        searchView.setUpButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!materialDrawer.isDrawerOpen()) {
                    materialDrawer.openDrawer();
                }
            }
        });
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                for (SearchCallback searchCallback : searchListeners) {
                    searchCallback.onSearchChanged(newText);
                }

                return true;
            }
        });
    }

    public void registerForSearchListener(SearchCallback searchCallback) {
        searchListeners.add(searchCallback);
    }

    public void unregisterForSearchListener(SearchCallback searchCallback) {
        searchListeners.remove(searchCallback);
    }

    private void initDrawer() {
        int headerId = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            headerId = R.layout.drawer_header_lollipop;
        } else {
            headerId = R.layout.drawer_header;
        }


        materialDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(headerId)
                .withSliderBackgroundColorRes(R.color.primary)
                .addDrawerItems(
                        createDrawerItem(R.string.all_categories_drawer, DRAWER_ALL_CATEGORIES, R.drawable.ic_file_document_white_48dp, R.drawable.ic_document_selected),
                        createDrawerItem(R.string.favorite_drawer, DRAWER_FAVORITE, R.drawable.ic_star_outline_white_48dp, R.drawable.ic_star_outline_selected),
                        createDrawerItem(R.string.recent_changes_drawer, DRAWER_RECENT_CHANGES, R.drawable.ic_new_releases_white_48dp, R.drawable.ic_new_selected),
                        createDrawerItem(R.string.search_drawer, DRAWER_SEARCH_FRAGMENT, R.drawable.ic_search_white_48dp, R.drawable.ic_search_selected)
                        //createDrawerItem(R.string.settings_drawer, DRAWER_SETTINGS_FRAGMENT, R.drawable.ic_settings_white_24dp, R.drawable.ic_settings_selected)
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
                    case DRAWER_SETTINGS_FRAGMENT:
                        setSettingsFragment();
                        break;
                    default:
                        break;
                }

                return false;
            }
        });
    }

    private SecondaryDrawerItem createDrawerItem(int name, int index, int icon, int selectedIcon) {
        SecondaryDrawerItem item = new SecondaryDrawerItem()
                .withName(name)
                .withIdentifier(index)
                .withIcon(icon)
                .withSelectedIcon(selectedIcon)
                .withSelectedColorRes(R.color.colorPrimaryDark)
                .withTextColorRes(R.color.iconsColor)
                .withSelectedTextColorRes(R.color.colorPrimaryLight);

        return item;
    }

    private void setAllCategoriesFragment() {
        setTitle(R.string.all_categories_drawer);
        setDrawerFragment(new AllCategoriesFragment(), ALL_CATEGORIES_FRAGMENT_TAG);
    }

    private void setFavoriteFragment() {
        setTitle(R.string.favorite_drawer);
        setDrawerFragment(new FavoriteFragment(), FAVORITE_FRAGMENT_TAG);
    }

    private void setRecentChangesFragment() {
        setTitle(R.string.recent_changes_drawer);
        setDrawerFragment(new RecentChangesFragment(), RECENT_CHANGES_FRAGMENT_TAG);
    }

    private void setSearchFragment() {
        setTitle(R.string.search_drawer);
        setDrawerFragment(new SearchFragment(), SEARCH_FRAGMENT_TAG, true);
    }

    private void setSettingsFragment() {
        setTitle(R.string.settings_drawer);
        setDrawerFragment(new SettingsFragment(), SETTINGS_FRAGMENT_TAG);
    }

    private void setDrawerFragment(Fragment fragment, String tag) {
        setDrawerFragment(fragment, tag, false);
    }

    private void setDrawerFragment(Fragment fragment, String tag, Boolean isSearchViewOpen) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);

        toolbarContainer.setTranslationY(0);
        toolbarHideRecyclerOnScrollListener.setVerticalOffset(0);

        if (isSearchViewOpen) {
            searchView.showSearch(true);
            shadowView.setVisibility(View.INVISIBLE);
        } else {
            searchView.closeSearch();
            shadowView.setVisibility(View.VISIBLE);
        }

        if (frag != null) {
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment, tag);
        fragmentTransaction.commit();
    }

}
