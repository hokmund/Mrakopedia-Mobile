package com.randomname.mrakopedia.ui.settings;

import android.content.Context;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.randomname.mrakopedia.MainActivity;
import com.randomname.mrakopedia.MrakopediaApplication;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.settings.ColorSchemes.ColorSchemesActivity;
import com.randomname.mrakopedia.ui.settings.ColorSchemes.ColorSchemesFragment;
import com.randomname.mrakopedia.utils.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.RelativeLayout;
import io.realm.annotations.PrimaryKey;

/**
 * Created by vgrigoryev on 11.02.2016.
 */
public class SettingsFragment extends RxBaseFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "SettingsFragment";

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
    @Bind(R.id.use_scheme_on_all_screens_layout)
    RelativeLayout useSchemeOnAllScreensLayout;
    @Bind(R.id.use_scheme_on_all_screens_switch)
    SwitchCompat useSchemeOnAllScreensSwitch;


    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MrakopediaApplication application = (MrakopediaApplication)getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public void onConnectedToInternet() {

    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.settings_drawer);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onResumeFromBackStack() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, null);
        ButterKnife.bind(this, view);

        initUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        updateUi();
    }


    private void initUI() {
        cachingPagesSwitch.setChecked(SettingsWorker.getInstance(getActivity()).isPagesCachingEnabled());
        cachingPhotoSwitch.setChecked(SettingsWorker.getInstance(getActivity()).isPhotoCachingEnabled());
        keepScreenOnSwitch.setChecked(SettingsWorker.getInstance(getActivity()).isKeepScreenOn());
        useSchemeOnAllScreensSwitch.setChecked(SettingsWorker.getInstance(getActivity()).isUseSchemeOnAllScreens());

        cachingPagesSwitch.setOnCheckedChangeListener(this);
        cachingPhotoSwitch.setOnCheckedChangeListener(this);
        keepScreenOnSwitch.setOnCheckedChangeListener(this);
        useSchemeOnAllScreensSwitch.setOnCheckedChangeListener(this);

        cachingPagesLayout.setOnClickListener(this);
        cachingPhotoLayout.setOnClickListener(this);
        keepScreenOnLayout.setOnClickListener(this);
        currentColorSchemeLayout.setOnClickListener(this);
        useSchemeOnAllScreensLayout.setOnClickListener(this);

        SettingsWorker settingsWorker = SettingsWorker.getInstance(getActivity());
        ColorScheme colorScheme = settingsWorker.getCurrentColorScheme();
        colorSchemeBackgroundColor.setBackgroundColor(colorScheme.getBackgroundColor());
        colorSchemeTextView.setTextColor(colorScheme.getTextColor());
    }

    private void updateUi() {
        ColorScheme colorScheme = SettingsWorker.getInstance(getActivity()).getCurrentColorScheme();
        colorSchemeBackgroundColor.setBackgroundColor(colorScheme.getBackgroundColor());
        colorSchemeTextView.setTextColor(colorScheme.getTextColor());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.caching_photo_switch:
                SettingsWorker.getInstance(getActivity()).setIsPhotoCachingEnabled(isChecked);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Caching photo switched")
                        .setLabel(String.valueOf(isChecked))
                        .build());

                break;
            case R.id.caching_pages_switch:
                SettingsWorker.getInstance(getActivity()).setIsPagesCachingEnabled(isChecked);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Caching pages switched")
                        .setLabel(String.valueOf(isChecked))
                        .build());
                break;
            case R.id.keep_screen_on_switch:
                SettingsWorker.getInstance(getActivity()).setKeepScreenOn(isChecked);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("keep screen on switched")
                        .setLabel(String.valueOf(isChecked))
                        .build());
                break;
            case R.id.use_scheme_on_all_screens_switch:
                SettingsWorker.getInstance(getActivity()).setUseSchemeOnAllScreens(isChecked);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("use scheme on all screens switched")
                        .setLabel(String.valueOf(isChecked))
                        .build());

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
            case R.id.use_scheme_on_all_screens_layout:
                useSchemeOnAllScreensSwitch.setChecked(!useSchemeOnAllScreensSwitch.isChecked());
            default:
                break;
        }
    }

    private void showColorSchemes() {
        /*Intent intent = new Intent(getActivity(), ColorSchemesActivity.class);
        startActivity(intent);*/
        ((MainActivity)getActivity()).addFragment(new ColorSchemesFragment());
    }
}
