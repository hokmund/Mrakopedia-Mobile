package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.views.TouchImageView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.ImageView;

public class FullScreentFotoActivity extends AppCompatActivity {

    public static String IMAGE_LINK_KEY = "image_link_key";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.full_screen_photo)
    TouchImageView fullScreenPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screent_foto);
        ButterKnife.bind(this);
        String imageUrl = getIntent().getStringExtra(IMAGE_LINK_KEY);

        Picasso.with(this).load(imageUrl).into(fullScreenPhoto);

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
    }
}
