package com.randomname.mrakopedia.ui.settings.ColorSchemes;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.randomname.mrakopedia.MainActivity;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.views.selection.SelectableTextView;
import com.randomname.mrakopedia.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by vgrigoryev on 12.02.2016.
 */
public class ColorSchemeEditorFragment extends Fragment {

    private static final String COLOR_SCHEME_ID_KEY = "colorSchemeIdKey";

    private static final String PREVIEW_STRING = "<h1>Заголовок</h1> \n <p>текст</p> \n <a href=''>ссылка</a>";

    private static final String BACKGROUND_COLOR_KEY = "backgroundColorKey";
    private static final String TEXT_COLOR_KEY = "textColorKey";
    private static final String SELECTED_COLOR_KEY = "selectedColorKey";
    private static final String LINK_COLOR_KEY = "linkColorKey";

    private ColorScheme colorScheme;

    private int backgroundColor = 0;
    private int textColor = 0;
    private int selectedColor = 0;
    private int linkColor = 0;

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

        if (savedInstanceState != null) {
            backgroundColor = savedInstanceState.getInt(BACKGROUND_COLOR_KEY, (colorScheme != null ? colorScheme.getBackgroundColor() : getActivity().getResources().getColor(R.color.iconsColor) ));
            textColor = savedInstanceState.getInt(TEXT_COLOR_KEY, (colorScheme != null ? colorScheme.getTextColor() : getActivity().getResources().getColor(R.color.textColorPrimary) ));
            linkColor = savedInstanceState.getInt(LINK_COLOR_KEY, (colorScheme != null ? colorScheme.getLinkColor() : getActivity().getResources().getColor(R.color.colorPrimary) ));
            selectedColor = savedInstanceState.getInt(SELECTED_COLOR_KEY, (colorScheme != null ? colorScheme.getSelectedColor() : getActivity().getResources().getColor(R.color.colorPrimary) ));
        } else {
            if (colorScheme != null) {
                backgroundColor = colorScheme.getBackgroundColor();
                textColor = colorScheme.getTextColor();
                linkColor = colorScheme.getLinkColor();
                selectedColor = colorScheme.getSelectedColor();
            } else {
                backgroundColor = getActivity().getResources().getColor(R.color.iconsColor);
                textColor = getActivity().getResources().getColor(R.color.textColorPrimary);
                linkColor = getActivity().getResources().getColor(R.color.colorPrimary);
                selectedColor = getActivity().getResources().getColor(R.color.colorPrimary);
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.color_scheme_editor_fragment, null);
        ButterKnife.bind(this, view);

        previewTextView.setText(Html.fromHtml(PREVIEW_STRING));
        previewTextView.selectText(0, 2);

        previewLayout.setBackgroundColor(backgroundColor);
        backgroundColorView.setBackgroundColor(backgroundColor);

        previewTextView.setTextColor(textColor);
        textColorView.setBackgroundColor(textColor);

        previewTextView.setLinkTextColor(linkColor);
        linkColorView.setBackgroundColor(linkColor);

        previewTextView.setColor(selectedColor);
        selectedColorView.setBackgroundColor(selectedColor);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(BACKGROUND_COLOR_KEY, backgroundColor);
        outState.putInt(TEXT_COLOR_KEY, textColor);
        outState.putInt(LINK_COLOR_KEY, linkColor);
        outState.putInt(SELECTED_COLOR_KEY, selectedColor);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_color_scheme_editor, menu);

        if(Build.VERSION.SDK_INT < 21) {
            final ViewTreeObserver viewTreeObserver = getActivity().getWindow().getDecorView().getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View menuButton = getActivity().findViewById(R.id.action_save_scheme);
                    // This could be called when the button is not there yet, so we must test for null
                    if (menuButton != null) {

                        Utils.setRippleToMenuItem(menuButton, getActivity());

                        if (Build.VERSION.SDK_INT < 16) {
                            getActivity().getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }

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
        realm.beginTransaction();

        if (colorScheme == null) {
            colorScheme = new ColorScheme();
            colorScheme.setSchemeId(DBWorker.getNextColorSchemeId());
        }

        colorScheme.setBackgroundColor(backgroundColor);

        colorScheme.setTextColor(textColor);

        colorScheme.setSelectedColor(selectedColor);

        colorScheme.setLinkColor(linkColor);

        realm.copyToRealmOrUpdate(colorScheme);
        realm.commitTransaction();
        realm.close();

        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
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

        lobsterPicker.setColor(backgroundColor);

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

        lobsterPicker.setColor(textColor);

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

        lobsterPicker.setColor(selectedColor);

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

        lobsterPicker.setColor(linkColor);

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
