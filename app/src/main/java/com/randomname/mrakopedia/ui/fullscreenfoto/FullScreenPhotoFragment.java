package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.support.v7.widget.ShareActionProvider;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.views.TouchImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vgrigoryev on 25.01.2016.
 */
public class FullScreenPhotoFragment extends Fragment {

    public final static String IMAGE_URL_KEY = "imageUrlKey";

    private String url = "";
    private long downloadId = -1;

    @Bind(R.id.full_screen_photo)
    TouchImageView imageView;

    public FullScreenPhotoFragment() {
    }

    public static FullScreenPhotoFragment getInstance(Bundle data) {
        FullScreenPhotoFragment fragment = new FullScreenPhotoFragment();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString(IMAGE_URL_KEY);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_screen_photo_fragment, container, false);
        ButterKnife.bind(this, view);

        Picasso.with(getActivity()).load(url).into(imageView);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_full_screen_photo, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share_photo);

        // Get its ShareActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);

        Uri bmpUri = getLocalBitmapUri(imageView);
        Intent shareIntent = null;

        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
        }

        // Connect the dots: give the ShareActionProvider its Share Intent
        if (shareIntent != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_photo:
                savePhotoToDisc();
                return false;
            default:
                break;
        }

        return false;
    }

    private void savePhotoToDisc() {
        String title = System.currentTimeMillis() + ".jpg";

        File direct = new File(Environment.DIRECTORY_PICTURES
                + "/mrakopedia");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(getString(R.string.app_name))
                .setDescription("Сохранение картинки")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/mrakopedia", title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        downloadId = mgr.enqueue(request);
    }

    private Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "mrakopedia_share_image.png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @OnClick(R.id.full_screen_photo)
    public void onPhotoClick() {
        try {
            ((FullScreentFotoActivity)getActivity()).showHideToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
