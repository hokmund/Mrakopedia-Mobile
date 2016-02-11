package com.randomname.mrakopedia.ui.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.randomname.mrakopedia.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vgrigoryev on 11.02.2016.
 */
public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    @Bind(R.id.caching_photo_switch)
    SwitchCompat cachingPhotoSwitch;
    @Bind(R.id.caching_pages_switch)
    SwitchCompat cachingPagesSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, null);
        ButterKnife.bind(this, view);

        initUI();

        return view;
    }

    private void initUI() {
        cachingPagesSwitch.setChecked(SettingsWorker.getInstance(getActivity()).isPagesCachingEnabled());
        cachingPhotoSwitch.setChecked(SettingsWorker.getInstance(getActivity()).isPhotoCachingEnabled());

        cachingPagesSwitch.setOnCheckedChangeListener(this);
        cachingPhotoSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.caching_photo_switch:
                SettingsWorker.getInstance(getActivity()).setIsPhotoCachingEnabled(isChecked);
                break;
            case R.id.caching_pages_switch:
                SettingsWorker.getInstance(getActivity()).setIsPagesCachingEnabled(isChecked);
                break;
            default:
                break;
        }
    }
}
