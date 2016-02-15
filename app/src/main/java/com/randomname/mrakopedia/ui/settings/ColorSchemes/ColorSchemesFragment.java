package com.randomname.mrakopedia.ui.settings.ColorSchemes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.settings.SettingsWorker;
import com.randomname.mrakopedia.ui.views.selection.SelectableTextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by vgrigoryev on 15.02.2016.
 */
public class ColorSchemesFragment extends RxBaseFragment {

    private static final String PREVIEW_STRING = "<h1>Test header</h1> \n <p>test test </p> \n <a href=''>Link test</a>";
    private static final int COLOR_SCHEME_EDITOR_RESULT = 42;

    @Bind(R.id.preview_layout)
    RelativeLayout previewLayout;
    @Bind(R.id.preview_text_view)
    SelectableTextView previewTextView;

    @Bind(R.id.add_action_button)
    FloatingActionButton addActionButton;

    @Bind(R.id.color_schemes_recycler_view)
    RecyclerView colorSchemesRecyclerView;
    ColorSchemeAdapter adapter;
    ArrayList<ColorScheme> colorSchemes = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.color_schemes_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new ColorSchemeAdapter(colorSchemes);
        adapter.setUseFixedSize(false);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = colorSchemesRecyclerView.getChildAdapterPosition(v);
                ColorScheme currentScheme = colorSchemes.get(position);
                SettingsWorker.getInstance(getActivity()).setCurrentColorScheme(currentScheme);

                previewTextView.setTextColor(currentScheme.getTextColor());
                previewTextView.setLinkTextColor(currentScheme.getLinkColor());
                previewLayout.setBackgroundColor(currentScheme.getBackgroundColor());
            }
        });

        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = colorSchemesRecyclerView.getChildAdapterPosition(v);
                Intent intent = new Intent(getActivity(), ColorSchemeEditorActivity.class);
                intent.putExtra(ColorSchemeEditorActivity.COLOR_SCHEME_ID, colorSchemes.get(position).getSchemeId());
                startActivityForResult(intent, COLOR_SCHEME_EDITOR_RESULT);
                return true;
            }
        });


        colorSchemesRecyclerView.setAdapter(adapter);
        colorSchemesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));

        ColorScheme currentScheme = SettingsWorker.getInstance(getActivity()).getCurrentColorScheme();

        previewTextView.setText(Html.fromHtml(PREVIEW_STRING));
        previewTextView.setTextColor(currentScheme.getTextColor());
        previewTextView.setLinkTextColor(currentScheme.getLinkColor());
        previewLayout.setBackgroundColor(currentScheme.getBackgroundColor());

        addActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ColorSchemeEditorActivity.class);
                startActivityForResult(intent, COLOR_SCHEME_EDITOR_RESULT);
            }
        });

        loadColorSchemes();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COLOR_SCHEME_EDITOR_RESULT && resultCode == Activity.RESULT_OK) {
            loadColorSchemes();
            ColorScheme currentScheme = SettingsWorker.getInstance(getActivity()).getCurrentColorScheme();

            previewTextView.setTextColor(currentScheme.getTextColor());
            previewTextView.setLinkTextColor(currentScheme.getLinkColor());
            previewLayout.setBackgroundColor(currentScheme.getBackgroundColor());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectedToInternet() {
    }

    private void loadColorSchemes() {
        colorSchemes.clear();
        Subscription subscription =
                Observable.just("")
                        .flatMap(new Func1<String, Observable<ColorScheme>>() {
                            @Override
                            public Observable<ColorScheme> call(String s) {
                                return Observable.from(DBWorker.getColorSchemes());
                            }
                        })
                        .subscribe(new Subscriber<ColorScheme>() {
                            @Override
                            public void onCompleted() {
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(ColorScheme colorScheme) {
                                colorSchemes.add(colorScheme);
                            }
                        });

        bindToLifecycle(subscription);

    }
}
