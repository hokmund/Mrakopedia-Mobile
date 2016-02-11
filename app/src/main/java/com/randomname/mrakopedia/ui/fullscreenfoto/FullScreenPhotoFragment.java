package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.settings.SettingsWorker;
import com.randomname.mrakopedia.ui.views.TouchImageView;

import java.io.File;

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

        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT);

        if (SettingsWorker.getInstance(getActivity()).isPhotoCachingEnabled()) {
            builder.cacheOnDisk(true);
        }

        ImageLoader.getInstance().displayImage(url, imageView, builder.build());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_full_screen_photo, menu);
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

    @OnClick(R.id.full_screen_photo)
    public void onPhotoClick() {
        try {
            ((FullScreentFotoActivity)getActivity()).showHideToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
