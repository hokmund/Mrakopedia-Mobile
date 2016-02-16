package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.randomname.mrakopedia.MrakopediaApplication;
import com.randomname.mrakopedia.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vgrigoryev on 25.01.2016.
 */
public class FullScreenPhotoFragmentHost extends Fragment {
    public final static String PHOTOS_ARRAY_KEY = "photos_array_key";
    public final static String POSITION_KEY = "position_key";

    private static final String TAG = "FullScreenPhotoFragmentHost";

    private ArrayList<String> photosArrayList;
    private int position;

    private PhotosAdapter adapter;

    @Bind(R.id.viewPager)
    ViewPager viewPager;

    @Bind(R.id.main_layout)
    RelativeLayout mainLayout;

    private Tracker mTracker;

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

        MrakopediaApplication application = (MrakopediaApplication)getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_screen_photo_fragment_host, container, false);
        ButterKnife.bind(this, view);

        adapter = new PhotosAdapter(getChildFragmentManager(), photosArrayList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.setOffscreenPageLimit(adapter.getCount() / 2);
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
    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
