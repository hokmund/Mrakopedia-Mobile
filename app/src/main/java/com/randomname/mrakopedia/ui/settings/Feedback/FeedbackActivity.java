package com.randomname.mrakopedia.ui.settings.Feedback;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FeedbackActivity extends AppCompatActivity {

    private static final String FEEDBACK_FRAGMENT_TAG = "feedBackFragmentTag";

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
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);

        initToolbar();

        setFeedbackFragment();
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.feedback);

        Utils.setRippleToToolbarIcon(toolbar, this);
    }

    private void setFeedbackFragment() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(FEEDBACK_FRAGMENT_TAG);

        if (frag != null) {
            return;
        }

        frag = new FeedbackFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, frag, FEEDBACK_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }
}
