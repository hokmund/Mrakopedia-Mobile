package com.randomname.mrakopedia.ui.settings.ColorSchemes;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.views.selection.SelectableTextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by vgrigoryev on 12.02.2016.
 */
public class ColorSchemeEditorFragment extends Fragment {

    private static final String COLOR_SCHEME_ID_KEY = "colorSchemeIdKey";

    private static final String PREVIEW_STRING = "<h1>Test header</h1> \n <p>test test </p> \n <a href=''>Link test</a>";

    private ColorScheme colorScheme;

    private int backgroundColor = -1;
    private int textColor = -1;
    private int selectedColor = -1;
    private int linkColor = -1;

    @Bind(R.id.preview_layout)
    RelativeLayout previewLayout;
    @Bind(R.id.preview_text_view)
    SelectableTextView previewTextView;
    @Bind(R.id.background_color_scheme_view)
    View backgroundColorView;
    @Bind(R.id.text_color_scheme_view)
    View textColorView;
    @Bind(R.id.selected_color_scheme_view)
    View selectedColorView;
    @Bind(R.id.link_color_scheme_view)
    View linkColorView;

    public static ColorSchemeEditorFragment getInstance(int schemeId) {
        Bundle bundle = new Bundle();
        bundle.putInt(COLOR_SCHEME_ID_KEY, schemeId);
        ColorSchemeEditorFragment fragment = new ColorSchemeEditorFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int schemeId = getArguments().getInt(COLOR_SCHEME_ID_KEY);
            colorScheme = DBWorker.getColorScheme(schemeId);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.color_scheme_editor_fragment, null);
        ButterKnife.bind(this, view);

        previewTextView.setText(Html.fromHtml(PREVIEW_STRING));
        previewTextView.selectText(0, 2);

        if (colorScheme != null) {
            previewLayout.setBackgroundColor(colorScheme.getBackgroundColor());
            backgroundColorView.setBackgroundColor(colorScheme.getBackgroundColor());
            backgroundColor = colorScheme.getBackgroundColor();

            previewTextView.setTextColor(colorScheme.getTextColor());
            textColorView.setBackgroundColor(colorScheme.getTextColor());
            textColor = colorScheme.getTextColor();

            previewTextView.setLinkTextColor(colorScheme.getLinkColor());
            linkColorView.setBackgroundColor(colorScheme.getLinkColor());
            linkColor = colorScheme.getLinkColor();

            previewTextView.setColor(colorScheme.getSelectedColor());
            selectedColorView.setBackgroundColor(colorScheme.getSelectedColor());
            selectedColor = colorScheme.getSelectedColor();
        } else {
            previewLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.iconsColor));
            backgroundColorView.setBackgroundColor(getActivity().getResources().getColor(R.color.iconsColor));
            backgroundColor = colorScheme.getBackgroundColor();

            previewTextView.setTextColor(getActivity().getResources().getColor(R.color.textColorPrimary));
            textColorView.setBackgroundColor(getActivity().getResources().getColor(R.color.textColorPrimary));
            textColor = getActivity().getResources().getColor(R.color.textColorPrimary);

            previewTextView.setLinkTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
            linkColorView.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
            linkColor = getActivity().getResources().getColor(R.color.colorPrimary);

            previewTextView.setColor(getActivity().getResources().getColor(R.color.colorPrimary));
            selectedColorView.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
            selectedColor = getActivity().getResources().getColor(R.color.colorPrimary);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_color_scheme_editor, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_scheme:
                saveColorScheme();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveColorScheme() {
        Realm realm = Realm.getDefaultInstance();

        if (colorScheme == null) {
            realm.beginTransaction();
            colorScheme = new ColorScheme();
            colorScheme.setSchemeId(DBWorker.getNextColorSchemeId());
            realm.copyToRealmOrUpdate(colorScheme);
        }

        if (backgroundColor >= 0) {
            colorScheme.setBackgroundColor(backgroundColor);
        }

        if (textColor >= 0) {
            colorScheme.setTextColor(textColor);
        }

        if (selectedColor >= 0) {
            colorScheme.setSelectedColor(selectedColor);
        }

        if (linkColor >= 0) {
            colorScheme.setLinkColor(linkColor);
        }

        realm.commitTransaction();
        realm.close();
        getActivity().finishActivity(Activity.RESULT_OK);
    }

    @OnClick(R.id.background_color_scheme_layout)
    public void onBackgroundColorSchemeClick() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.color_picker_dialog, null);
        dialogBuilder.setView(dialogView);

        final LobsterPicker lobsterPicker = (LobsterPicker)dialogView.findViewById(R.id.lobsterpicker);
        LobsterShadeSlider shadeSlider = (LobsterShadeSlider)dialogView.findViewById(R.id.shadeslider);
        LobsterOpacitySlider opacitySlider = (LobsterOpacitySlider)dialogView.findViewById(R.id.opacityslider);

        lobsterPicker.addDecorator(shadeSlider);
        lobsterPicker.addDecorator(opacitySlider);

        if (backgroundColor >= 0) {
            lobsterPicker.setColor(backgroundColor);
        }

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                backgroundColorView.setBackgroundColor(lobsterPicker.getColor());
                previewLayout.setBackgroundColor(lobsterPicker.getColor());
                backgroundColor = lobsterPicker.getColor();
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @OnClick(R.id.text_color_scheme_layout)
    public void onTextColorSchemeClick() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.color_picker_dialog, null);
        dialogBuilder.setView(dialogView);

        final LobsterPicker lobsterPicker = (LobsterPicker)dialogView.findViewById(R.id.lobsterpicker);
        LobsterShadeSlider shadeSlider = (LobsterShadeSlider)dialogView.findViewById(R.id.shadeslider);
        LobsterOpacitySlider opacitySlider = (LobsterOpacitySlider)dialogView.findViewById(R.id.opacityslider);

        lobsterPicker.addDecorator(shadeSlider);
        lobsterPicker.addDecorator(opacitySlider);

        if (textColor >= 0) {
            lobsterPicker.setColor(textColor);
        }

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textColorView.setBackgroundColor(lobsterPicker.getColor());
                previewTextView.setTextColor(lobsterPicker.getColor());
                textColor = lobsterPicker.getColor();
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @OnClick(R.id.selected_color_scheme_layout)
    public void onSelectedColorSchemeClick() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.color_picker_dialog, null);
        dialogBuilder.setView(dialogView);

        final LobsterPicker lobsterPicker = (LobsterPicker)dialogView.findViewById(R.id.lobsterpicker);
        LobsterShadeSlider shadeSlider = (LobsterShadeSlider)dialogView.findViewById(R.id.shadeslider);
        LobsterOpacitySlider opacitySlider = (LobsterOpacitySlider)dialogView.findViewById(R.id.opacityslider);

        lobsterPicker.addDecorator(shadeSlider);
        lobsterPicker.addDecorator(opacitySlider);

        if (selectedColor >= 0) {
            lobsterPicker.setColor(selectedColor);
        }

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                previewTextView.setColor(lobsterPicker.getColor());
                selectedColorView.setBackgroundColor(lobsterPicker.getColor());
                selectedColor = lobsterPicker.getColor();
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @OnClick(R.id.link_color_scheme_layout)
    public void onLinkColorSchemeClick() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.color_picker_dialog, null);
        dialogBuilder.setView(dialogView);

        final LobsterPicker lobsterPicker = (LobsterPicker)dialogView.findViewById(R.id.lobsterpicker);
        LobsterShadeSlider shadeSlider = (LobsterShadeSlider)dialogView.findViewById(R.id.shadeslider);
        LobsterOpacitySlider opacitySlider = (LobsterOpacitySlider)dialogView.findViewById(R.id.opacityslider);

        lobsterPicker.addDecorator(shadeSlider);
        lobsterPicker.addDecorator(opacitySlider);

        if (linkColor >= 0) {
            lobsterPicker.setColor(linkColor);
        }

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                previewTextView.setLinkTextColor(lobsterPicker.getColor());
                linkColorView.setBackgroundColor(lobsterPicker.getColor());
                linkColor = lobsterPicker.getColor();
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
