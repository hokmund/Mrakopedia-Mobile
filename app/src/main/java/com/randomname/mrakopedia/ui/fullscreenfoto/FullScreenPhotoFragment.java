package com.randomname.mrakopedia.ui.fullscreenfoto;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.views.TouchImageView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vgrigoryev on 25.01.2016.
 */
public class FullScreenPhotoFragment extends Fragment {

    public final static String IMAGE_URL_KEY = "imageUrlKey";

    private String url = "";

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_screen_photo_fragment, container, false);
        ButterKnife.bind(this, view);

        Picasso.with(getActivity()).load(url).into(imageView);
        return view;
    }
}
