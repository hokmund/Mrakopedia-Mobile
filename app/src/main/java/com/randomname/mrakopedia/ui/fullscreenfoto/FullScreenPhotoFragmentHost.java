package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;

import com.randomname.mrakopedia.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vgrigoryev on 25.01.2016.
 */
public class FullScreenPhotoFragmentHost extends Fragment {
    public final static String PHOTOS_ARRAY_KEY = "photos_array_key";
    public final static String POSITION_KEY = "position_key";

    private ArrayList<String> photosArrayList;
    private int position;

    private PhotosAdapter adapter;

    @Bind(R.id.viewPager)
    ViewPager viewPager;

    @Bind(R.id.main_layout)
    RelativeLayout mainLayout;

    public FullScreenPhotoFragmentHost() {
    }

    public static FullScreenPhotoFragmentHost getInstance(Bundle data) {
        FullScreenPhotoFragmentHost fragment = new FullScreenPhotoFragmentHost();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        photosArrayList = new ArrayList<>();

        if (getArguments().getStringArrayList(PHOTOS_ARRAY_KEY) != null) {
            photosArrayList.addAll(getArguments().getStringArrayList(PHOTOS_ARRAY_KEY));
        }

        position = getArguments().getInt(POSITION_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_screen_photo_fragment_host, container, false);
        ButterKnife.bind(this, view);

        adapter = new PhotosAdapter(getChildFragmentManager(), photosArrayList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setNewTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setNewTitle(position);
        return view;
    }


    private void setNewTitle(int position) {
        if (photosArrayList.size() > 1) {
            try {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle((position + 1) + " фото из " + photosArrayList.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PhotosAdapter extends FragmentPagerAdapter {
        private ArrayList<String> wallPhotos;

        public PhotosAdapter(FragmentManager fragmentManager, ArrayList<String> wallPhotos) {
            super(fragmentManager);
            this.wallPhotos = wallPhotos;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return wallPhotos.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            Bundle data = new Bundle();
            data.putString(FullScreenPhotoFragment.IMAGE_URL_KEY, wallPhotos.get(position));
            Fragment fragment = FullScreenPhotoFragment.getInstance(data);

            return fragment;
        }

    }
}
