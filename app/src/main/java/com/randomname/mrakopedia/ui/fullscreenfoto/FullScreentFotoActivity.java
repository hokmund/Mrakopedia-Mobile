package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.views.TouchImageView;
import com.randomname.mrakopedia.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.ImageView;

public class FullScreentFotoActivity extends AppCompatActivity {

    public static String IMAGE_ARRAY_KEY = "image_link_key";
    public static String SELECTED_IMAGE_KEY = "selected_image_key";

    private static final String FULL_SCREEN_FRAGMENT_HOST = "full_screen_fragment_host";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screent_foto);
        ButterKnife.bind(this);
        ArrayList<String> imagesArray = getIntent().getStringArrayListExtra(IMAGE_ARRAY_KEY);
        int selectedPosition = getIntent().getIntExtra(SELECTED_IMAGE_KEY, 0);

        if (getSupportFragmentManager().getFragments() == null) {
            Fragment frag = getSupportFragmentManager().findFragmentByTag(FULL_SCREEN_FRAGMENT_HOST);

            if (frag != null) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putStringArrayList(FullScreenPhotoFragmentHost.PHOTOS_ARRAY_KEY, imagesArray);
            bundle.putInt(FullScreenPhotoFragmentHost.POSITION_KEY, selectedPosition);
            FullScreenPhotoFragmentHost fullScreenPhotoFragmentHost = FullScreenPhotoFragmentHost.getInstance(bundle);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, fullScreenPhotoFragmentHost, FULL_SCREEN_FRAGMENT_HOST);
            fragmentTransaction.commit();
        }

        initToolbar();
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
        getSupportActionBar().setTitle("");

        if (Utils.checkForLollipop()) {
            getWindow().setStatusBarColor(Color.BLACK);
        }
    }
}
