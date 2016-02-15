package com.randomname.mrakopedia.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.randomname.mrakopedia.MainActivity;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.ui.settings.ColorSchemes.ColorSchemesActivity;
import com.randomname.mrakopedia.ui.settings.ColorSchemes.ColorSchemesFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.RelativeLayout;

/**
 * Created by vgrigoryev on 11.02.2016.
 */
public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    @Bind(R.id.caching_photo_switch)
    SwitchCompat cachingPhotoSwitch;
    @Bind(R.id.caching_photo_layout)
    RelativeLayout cachingPhotoLayout;
    @Bind(R.id.caching_pages_switch)
    SwitchCompat cachingPagesSwitch;
    @Bind(R.id.caching_pages_layout)
    RelativeLayout cachingPagesLayout;
    @Bind(R.id.keep_screen_on_switch)
    SwitchCompat keepScreenOnSwitch;
    @Bind(R.id.keep_screen_on_layout)
    RelativeLayout keepScreenOnLayout;
    @Bind(R.id.current_color_scheme_layout)
    RelativeLayout currentColorSchemeLayout;
    @Bind(R.id.color_view)
    View colorSchemeBackgroundColor;
    @Bind(R.id.text_view)
    TextView colorSchemeTextView;

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
        keepScreenOnSwitch.setChecked(SettingsWorker.getInstance(getActivity()).isKeepScreenOn());

        cachingPagesSwitch.setOnCheckedChangeListener(this);
        cachingPhotoSwitch.setOnCheckedChangeListener(this);
        keepScreenOnSwitch.setOnCheckedChangeListener(this);

        cachingPagesLayout.setOnClickListener(this);
        cachingPhotoLayout.setOnClickListener(this);
        keepScreenOnLayout.setOnClickListener(this);
        currentColorSchemeLayout.setOnClickListener(this);

        ColorScheme colorScheme = SettingsWorker.getInstance(getActivity()).getCurrentColorScheme();
        colorSchemeBackgroundColor.setBackgroundColor(colorScheme.getBackgroundColor());
        colorSchemeTextView.setTextColor(colorScheme.getTextColor());
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
            case R.id.keep_screen_on_switch:
                SettingsWorker.getInstance(getActivity()).setKeepScreenOn(isChecked);
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.caching_pages_layout:
                cachingPagesSwitch.setChecked(!cachingPagesSwitch.isChecked());
                break;
            case R.id.caching_photo_layout:
                cachingPhotoSwitch.setChecked(!cachingPhotoSwitch.isChecked());
                break;
            case R.id.keep_screen_on_layout:
                keepScreenOnSwitch.setChecked(!keepScreenOnSwitch.isChecked());
                break;
            case R.id.current_color_scheme_layout:
                showColorSchemes();
                break;
            default:
                break;
        }
    }

    private void showColorSchemes() {
        Intent intent = new Intent(getActivity(), ColorSchemesActivity.class);
        startActivity(intent);
    }
}
