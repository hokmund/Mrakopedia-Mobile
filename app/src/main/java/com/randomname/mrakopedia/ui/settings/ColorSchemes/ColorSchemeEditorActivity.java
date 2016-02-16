package com.randomname.mrakopedia.ui.settings.ColorSchemes;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryFragment;
import com.randomname.mrakopedia.ui.views.materialsearch.MaterialSearchView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ColorSchemeEditorActivity extends AppCompatActivity {

    public static final String COLOR_SCHEME_ID = "colorSchemeId";

    private static final String COLOR_SCHEME_EDITOR_FRAGMENT_TAG = "colorSchemeEditorFragment";

    private ColorScheme colorScheme;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

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
        setContentView(R.layout.activity_color_scheme_editor);
        ButterKnife.bind(this);

        int colorSchemeId = getIntent().getIntExtra(COLOR_SCHEME_ID, -1);
        colorScheme = DBWorker.getColorScheme(colorSchemeId);
        initToolbar();
        setColorSchemeEditorFragment(colorSchemeId);
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

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (colorScheme == null) {
            setTitle(R.string.create_color_scheme);
        } else {
            setTitle(R.string.edit_color_scheme);
        }
    }

    private void setColorSchemeEditorFragment(int colorSchemeId) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(COLOR_SCHEME_EDITOR_FRAGMENT_TAG);

        if (frag != null) {
            return;
        }

        frag = ColorSchemeEditorFragment.getInstance(colorSchemeId);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, frag, COLOR_SCHEME_EDITOR_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}
