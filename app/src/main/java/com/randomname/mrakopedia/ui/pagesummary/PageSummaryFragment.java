package com.randomname.mrakopedia.ui.pagesummary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.randomname.mrakopedia.MrakopediaApplication;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.models.api.pagesummary.Categories;
import com.randomname.mrakopedia.models.api.pagesummary.CategoriesTextSection;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.Templates;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.models.realm.TextSectionRealm;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.categorymembers.CategoryMembersActivity;
import com.randomname.mrakopedia.ui.fullscreenfoto.FullScreentFotoActivity;
import com.randomname.mrakopedia.ui.settings.ColorSchemes.ColorSchemeAdapter;
import com.randomname.mrakopedia.ui.settings.ColorSchemes.ColorSchemeEditorActivity;
import com.randomname.mrakopedia.ui.settings.SettingsWorker;
import com.randomname.mrakopedia.ui.views.CarbonSpinner;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.ui.views.StickySummaryDecoration;
import com.randomname.mrakopedia.ui.views.selection.SelectableLayoutManager;
import com.randomname.mrakopedia.ui.views.selection.SelectableRecyclerView;
import com.randomname.mrakopedia.ui.views.selection.SelectableTextView;
import com.randomname.mrakopedia.ui.views.selection.SelectionCallback;
import com.randomname.mrakopedia.utils.NetworkUtils;
import com.randomname.mrakopedia.utils.StringUtils;
import com.randomname.mrakopedia.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import carbon.internal.TypefaceUtils;
import carbon.widget.ProgressBar;
import carbon.widget.Spinner;
import codetail.graphics.drawables.LollipopDrawablesCompat;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Vlad on 20.01.2016.
 */
public class PageSummaryFragment extends RxBaseFragment implements OnPageSummaryFragmentBackListener {

    private static final String TAG = "PageSummaryFragment";
    private static final String PAGE_TITLE_KEY = "pageTitleKey";
    private static final String PAGE_ID_KEY = "pageIdKey";

    private static final String IS_OPTIONS_SHOWN_KEY = "isOptionsShownKey";
    private static final String TEXT_SECTIONS_KEY = "textSectionsKey";

    private static final int COLOR_SCHEME_EDITOR_RESULT = 42;

    private static final float MIN_TEXT_SIZE = 10f;
    private static final float MAX_TEXT_SIZE = 32f;

    private String pageTitle;
    private String pageId;
    private boolean pageIsFavorite = false;
    private boolean pageIsRead = false;
    private boolean isLoading = false;
    private boolean isOptionsShown = false;

    private Tracker mTracker;

    @Bind(R.id.page_summary_recycler_view)
    SelectableRecyclerView recyclerView;

    private ArrayList<TextSection> textSections;
    private PageSummaryAdapter adapter;

    @Bind(R.id.error_text_view)
    carbon.widget.TextView errorTextView;

    @Bind(R.id.loading_progress_bar)
    ProgressBar loadingProgressBar;

    @Bind(R.id.options_layout)
    LinearLayout optionsLayout;

    @Bind(R.id.font_size_seek_bar)
    SeekBar fontSizeSeekBar;

    @Bind(R.id.color_scheme_recycler_view)
    RecyclerView colorSchemeRecyclerView;

    @Bind(R.id.reveal_view)
    RelativeLayout revealView;

    @Bind(R.id.add_color_scheme_button)
    ImageView addColorSchemeButton;

    @Bind(R.id.font_type_spinner)
    CarbonSpinner fontTypeSpinner;

    private ArrayList<ColorScheme> colorsList;
    private ColorSchemeAdapter colorSchemeAdapter;
    private boolean animating = false;

    public PageSummaryFragment() {
    }

    public static PageSummaryFragment getInstance(String pageTitle, String pageId) {
        PageSummaryFragment fragment = new PageSummaryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PAGE_TITLE_KEY, pageTitle);
        bundle.putString(PAGE_ID_KEY, pageId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            pageTitle = getArguments().getString(PAGE_TITLE_KEY);
            pageId = getArguments().getString(PAGE_ID_KEY);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TEXT_SECTIONS_KEY)) {
                textSections = savedInstanceState.getParcelableArrayList(TEXT_SECTIONS_KEY);
            } else {
                textSections = new ArrayList<>();
            }
            isOptionsShown = savedInstanceState.getBoolean(IS_OPTIONS_SHOWN_KEY);
        } else {
            textSections = new ArrayList<>();
        }

        pageIsFavorite = DBWorker.getPageIsFavorite(pageTitle);
        pageIsRead = DBWorker.getPageIsRead(pageTitle);
        setHasOptionsMenu(true);

        MrakopediaApplication application = (MrakopediaApplication)getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.page_summary_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new PageSummaryAdapter(textSections, getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                Intent intent = new Intent(getActivity(), PageSummaryActivity.class);
                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, adapter.getDisplayedData().get(position).getText());
                startActivity(intent);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);

                ArrayList<String> imageArray = new ArrayList<>();

                for (TextSection section : adapter.getDisplayedData()) {
                    if (section.getType() == TextSection.IMAGE_TYPE) {
                        imageArray.add(section.getText().toString());
                    }
                }

                Intent intent = new Intent(getActivity(), FullScreentFotoActivity.class);
                intent.putExtra(FullScreentFotoActivity.IMAGE_ARRAY_KEY, imageArray);
                intent.putExtra(FullScreentFotoActivity.SELECTED_IMAGE_KEY, imageArray.indexOf(adapter.getDisplayedData().get(position).getText()));
                startActivity(intent);
            }
        }, new OnCategoryClickListener() {
            @Override
            public void OnCategoryClick(String categoryTitle) {
                Intent intent = new Intent(getActivity(), CategoryMembersActivity.class);
                intent.putExtra(CategoryMembersActivity.CATEGORY_NAME_EXTRA, categoryTitle);
                startActivity(intent);
            }
        });

        adapter.setHasStableIds(true);
        SelectableLayoutManager manager = new SelectableLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        recyclerView.setSelectionCallback(new SelectionCallback() {
            @Override
            public void startSelection() {
                ((PageSummaryInterface) getActivity()).startSelection();
            }

            @Override
            public void stopSelection() {
                ((PageSummaryInterface) getActivity()).stopSelection();
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isOptionsShown) {
                    closeOptions();
                    return true;
                }

                return false;
            }
        });
        recyclerView.addItemDecoration(new StickySummaryDecoration(getActivity()));

        recyclerView.addOnScrollListener(((PageSummaryInterface) getActivity()).getToolbarHideListener());

        if (adapter.getDisplayedData().size() <= 1) {
            if (DBWorker.isPageSummarySavedById(pageId) || DBWorker.isPageSummarySaved(pageTitle)) {
                getArticleByRealm();
            } else {
                getArticleByNetwork();
            }
        }

        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    progress += MIN_TEXT_SIZE;

                    adapter.notifyFontSizeChanged(progress);

                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        View v = recyclerView.getChildAt(i);
                        View tv = v.findViewWithTag(getString(R.string.font_size_can_change_key));

                        if (tv != null) {
                            ((TextView) tv).setTextSize(progress);
                        }
                    }

                    SettingsWorker.getInstance(getActivity()).setCurrentFontSize(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ColorScheme colorScheme = SettingsWorker.getInstance(getActivity()).getCurrentColorScheme();
        view.setBackgroundColor(colorScheme.getBackgroundColor());

        float currentFontSize = SettingsWorker.getInstance(getActivity()).getCurrentFontSize();

        fontSizeSeekBar.setProgress(Math.round(currentFontSize - MIN_TEXT_SIZE));
        fontSizeSeekBar.setMax(Math.round(MAX_TEXT_SIZE - MIN_TEXT_SIZE));

        adapter.notifyFontSizeChanged(currentFontSize);

        if (!isOptionsShown) {
            final ViewTreeObserver observer = optionsLayout.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    optionsLayout.setTranslationY(-optionsLayout.getHeight());

                    if (Build.VERSION.SDK_INT < 16) {
                        optionsLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        optionsLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        colorsList = new ArrayList<>();
        loadColorSchemes();

        colorSchemeAdapter = new ColorSchemeAdapter(colorsList);
        colorSchemeAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = colorSchemeRecyclerView.getChildAdapterPosition(v);
                Intent intent = new Intent(getActivity(), ColorSchemeEditorActivity.class);
                intent.putExtra(ColorSchemeEditorActivity.COLOR_SCHEME_ID, colorsList.get(position).getSchemeId());
                startActivityForResult(intent, COLOR_SCHEME_EDITOR_RESULT);

                return true;
            }
        });
        colorSchemeAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = colorSchemeRecyclerView.getChildAdapterPosition(v);
                final ColorScheme colorScheme = colorsList.get(position);
                SettingsWorker.getInstance(getActivity()).setCurrentColorScheme(colorScheme);

                int cx = (revealView.getLeft() + revealView.getRight()) / 2;
                int cy = (revealView.getTop() + revealView.getBottom()) / 2;

                // get the final radius for the clipping circle
                int dx = Math.max(cx, revealView.getWidth() - cx);
                int dy = Math.max(cy, revealView.getHeight() - cy);
                float finalRadius = (float) Math.hypot(dx, dy);

                final SupportAnimator animator =
                        ViewAnimationUtils.createCircularReveal(revealView, cx, cy, 0, finalRadius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(300);
                animator.addListener(new SupportAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart() {
                    }

                    @Override
                    public void onAnimationEnd() {
                        view.setBackgroundColor(colorScheme.getBackgroundColor());
                        revealView.setVisibility(View.GONE);
                        animating = false;
                    }

                    @Override
                    public void onAnimationCancel() {
                        animating = false;
                    }

                    @Override
                    public void onAnimationRepeat() {

                    }
                });

                revealView.setBackgroundColor(colorScheme.getBackgroundColor());
                revealView.setVisibility(View.VISIBLE);

                animator.start();

                adapter.notifyColorSchemeChanged(colorScheme);

                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View view = recyclerView.getChildAt(i);
                    View tv = view.findViewWithTag(getString(R.string.font_size_can_change_key));

                    if (tv != null) {
                        ((TextView) tv).setTextColor(colorScheme.getTextColor());
                        ((TextView) tv).setLinkTextColor(colorScheme.getLinkColor());
                        ((SelectableTextView) tv).setColor(colorScheme.getSelectedColor());
                    }
                }
            }
        });
        colorSchemeRecyclerView.setAdapter(colorSchemeAdapter);
        colorSchemeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        try {
            addColorSchemeButton.setBackgroundDrawable(LollipopDrawablesCompat.getDrawable(getActivity().getResources(), R.drawable.ripple, getActivity().getTheme()));
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        fontTypeSpinner.setItems(FontTypeConstants.FONT_TYPES_ARRAY);
        fontTypeSpinner.setText(FontTypeConstants.FONT_TYPES_ARRAY[SettingsWorker.getInstance(getActivity()).getFontType()]);

        try {
            Typeface typeface = TypefaceUtils.getTypeface(getContext(), FontTypeConstants.FONT_PATHS_ARRAY[SettingsWorker.getInstance(getActivity()).getFontType()]);
            fontTypeSpinner.setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fontTypeSpinner.setDropDownColor(getResources().getColor(R.color.save_color));
        fontTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Typeface typeface = null;

                try {
                    typeface = TypefaceUtils.getTypeface(getContext(), FontTypeConstants.FONT_PATHS_ARRAY[position]);
                    fontTypeSpinner.setTypeface(typeface);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                SettingsWorker.getInstance(getActivity()).setFontType(position);

                if (typeface != null) {
                    adapter.notifyFontTypeChanged(typeface);

                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        View recyclerViewChildAt = recyclerView.getChildAt(i);
                        View tv = recyclerViewChildAt.findViewWithTag(getString(R.string.font_size_can_change_key));

                        if (tv != null) {
                            ((TextView) tv).setTypeface(typeface);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pageTitle != null) {
            mTracker.setScreenName(TAG + " " + pageTitle);
        } else {
            mTracker.setScreenName(TAG);
        }
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COLOR_SCHEME_EDITOR_RESULT && resultCode == Activity.RESULT_OK) {
            loadColorSchemes();

            ColorScheme colorScheme = SettingsWorker.getInstance(getActivity()).getCurrentColorScheme();
            getView().setBackgroundColor(colorScheme.getBackgroundColor());
            adapter.notifyColorSchemeChanged(colorScheme);

            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View view = recyclerView.getChildAt(i);
                View tv = view.findViewWithTag(getString(R.string.font_size_can_change_key));

                if (tv != null) {
                    ((TextView)tv).setTextColor(colorScheme.getTextColor());
                    ((TextView)tv).setLinkTextColor(colorScheme.getLinkColor());
                    ((SelectableTextView)tv).setColor(colorScheme.getSelectedColor());
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectedToInternet() {
    }

    public void copySelectedText() {
        if (recyclerView != null) {
            recyclerView.copyTextToClipboard();
        }
    }

    public void cancelSelection() {
        if (recyclerView != null) {
            recyclerView.resetSelection();
        }
    }

    private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (adapter.getDisplayedData().size() > 1) {
            inflater.inflate(R.menu.menu_page_summary, menu);
            setMenuFavoriteStatus(menu.findItem(R.id.action_favorite_page));
            setMenuReadStatus(menu.findItem(R.id.action_read_page));

            if(Build.VERSION.SDK_INT < 21) {
                final ViewTreeObserver viewTreeObserver = getActivity().getWindow().getDecorView().getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        View favoriteMenu = getActivity().findViewById(R.id.action_favorite_page);
                        View readPage = getActivity().findViewById(R.id.action_read_page);
                        View settingsAction = getActivity().findViewById(R.id.action_settings);

                        if (favoriteMenu != null) {
                            Utils.setRippleToMenuItem(favoriteMenu, getActivity());
                        }

                        if (readPage != null) {
                            Utils.setRippleToMenuItem(readPage, getActivity());
                        }

                        if (settingsAction != null) {
                            Utils.setRippleToMenuItem(settingsAction, getActivity());
                        }


                        if (Build.VERSION.SDK_INT < 16) {
                            getActivity().getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite_page:
                pageIsFavorite = !pageIsFavorite;
                DBWorker.setPageFavoriteStatus(pageTitle, pageIsFavorite);
                setMenuFavoriteStatus(item);
                return true;
            case R.id.action_read_page:
                pageIsRead = !pageIsRead;
                DBWorker.setPageReadStatus(pageTitle, pageIsRead);
                setMenuReadStatus(item);
                return true;
            case R.id.action_settings:

                if (!isOptionsShown) {
                    showOptions();
                } else {
                    closeOptions();
                }

                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.add_color_scheme_button)
    public void addColorClick() {
        Intent intent = new Intent(getActivity(), ColorSchemeEditorActivity.class);
        startActivityForResult(intent, COLOR_SCHEME_EDITOR_RESULT);
    }

    private void showOptions() {
        optionsLayout.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);

        isOptionsShown = true;
    }

    private void closeOptions() {
        optionsLayout.animate()
                .translationY(-optionsLayout.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);

        isOptionsShown = false;
    }

    private void setMenuFavoriteStatus(MenuItem favoriteItem) {
        if (pageIsFavorite) {
            favoriteItem.setIcon(R.drawable.ic_star_white_24dp);
            favoriteItem.setTitle("Удалить из избранного");
        } else {
            favoriteItem.setIcon(R.drawable.ic_star_outline_white_24dp);
            favoriteItem.setTitle("Добавить в избранное");
        }
    }

    private void setMenuReadStatus(MenuItem item) {
        if (!pageIsRead) {
            item.setIcon(R.drawable.ic_bookmark_white_24dp);
            item.setTitle("Отметить как прочитанное");
        } else {
            item.setIcon(R.drawable.ic_bookmark_check_white_24dp);
            item.setTitle("Отметить как не прочитанное");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!adapter.getDisplayedData().isEmpty()) {
            outState.putParcelableArrayList(TEXT_SECTIONS_KEY, textSections);
        }
        outState.putBoolean(IS_OPTIONS_SHOWN_KEY, isOptionsShown);
        super.onSaveInstanceState(outState);
    }

    private void loadColorSchemes() {
        colorsList.clear();
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
                                colorSchemeAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(ColorScheme colorScheme) {
                                colorsList.add(colorScheme);
                            }
                        });

        bindToLifecycle(subscription);

    }

    private void getArticleByNetwork() {

        if (isLoading) {
            return;
        }

        isLoading = true;

        recyclerView.setVisibility(View.INVISIBLE);
        errorTextView.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        Observable<PageSummaryResult> observable;

        if (pageId == null) {
            observable = MrakopediaApiWorker.getInstance().getPageSummaryByTitle(pageTitle);
        } else {
            observable = MrakopediaApiWorker.getInstance().getPageSummary(pageId);
        }


        Subscription subscription =
                observable
                .map(new Func1<PageSummaryResult, PageSummaryResult>() {
                    @Override
                    public PageSummaryResult call(PageSummaryResult pageSummaryResult) {
                        String htmlText = pageSummaryResult.getParse().getText().getText();

                        Document doc = Jsoup.parse(htmlText);

                        Elements ratingSpan = doc.select("span#w4g_rb_area-1");

                        if (!ratingSpan.isEmpty()) {
                            ratingSpan.remove();
                        }

                        Elements noJsDiv = doc.select("div.w4g_rb_nojs");

                        if (!noJsDiv.isEmpty()) {
                            noJsDiv.remove();
                        }

                        Elements scriptTags = doc.select("script");

                        if (!scriptTags.isEmpty()) {
                            scriptTags.remove();
                        }

                        if (htmlText.contains("spoilerLink")) {
                            Elements spoilerLinks = doc.select("a.spoilerLink");
                            if (!spoilerLinks.isEmpty()) {
                                spoilerLinks.remove();
                            }
                        }

                        if (htmlText.contains("spoilers-button")) {
                            Elements spoilersButtons = doc.select("span.spoilers-button");
                            if (!spoilersButtons.isEmpty()) {
                                spoilersButtons.remove();
                            }
                        }

                        if (htmlText.contains("del")) {
                            Elements delTag = doc.select("del");
                            if (!delTag.isEmpty()) {
                                delTag.tagName("strike");
                            }
                        }

                        Elements aTags = doc.select("a");

                        if (!aTags.isEmpty()) {
                            for (Element aTag : aTags) {
                                if (aTag.attr("abs:href").length() == 0) {
                                    // this is not a global url
                                    String decodedHref = null;
                                    boolean toUnwrap = true;

                                    try {
                                        decodedHref = URLDecoder.decode(aTag.attr("href"), "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }

                                    if (decodedHref != null && decodedHref.contains("Категория:")) {
                                        aTag.attr("href", "mrakopediaCategory://?categoryTitle=" + decodedHref.substring(decodedHref.lastIndexOf("Категория:") + 10));
                                        toUnwrap = false;
                                    }

                                    if (decodedHref != null && !decodedHref.contains("#") && !decodedHref.contains("index.php")) {
                                        boolean toAdd = true;

                                        for (String banString : Utils.pagesBanList) {
                                            if (decodedHref.contains(banString)) {
                                                toAdd = false;
                                                break;
                                            }
                                        }

                                        if (toAdd) {
                                            aTag.attr("href", "mrakopediaPage://?pageTitle=" + decodedHref.substring(decodedHref.lastIndexOf("wiki/") + 5));
                                            toUnwrap = false;
                                        }
                                    }

                                    if (toUnwrap) {
                                        aTag.unwrap();
                                    }
                                }
                            }
                        }

                        if (htmlText.contains("box")) {
                            Elements boxDivs = doc.select("div.box");

                            if (!boxDivs.isEmpty()) {
                                boxDivs.remove();
                            }
                        }

                        if (htmlText.contains("mw-editsection")) {
                            Elements editSections = doc.select("span.mw-editsection");

                            if (!editSections.isEmpty()) {
                                editSections.remove();
                            }
                        }

                        if (htmlText.contains("h2")) {
                            Elements h2 = doc.select("h2");

                            if (!h2.isEmpty()) {
                                for (Element element : h2) {
                                    element.html(element.html() + " ");
                                }
                            }
                        }

                        if (htmlText.contains("li")) {
                            Elements liTags = doc.select("li");

                            if (!liTags.isEmpty()) {
                                for (Element liTag : liTags) {
                                    liTag.tagName("p");
                                }
                            }
                        }


                        if (htmlText.contains("iframe")) {
                            Elements iFrames = doc.select("iframe");

                            if (!iFrames.isEmpty()) {
                                for (Element iFrame : iFrames) {
                                    String src;
                                    if (iFrame.attr("src").contains("youtube")) {
                                        src = iFrame.attr("src");

                                        src = src.substring(src.indexOf("embed/") + 6);

                                        if (src.charAt(src.length() - 1) == '?') {
                                            src = src.substring(0, src.length() - 1);
                                        }

                                        iFrame.tagName("p");
                                        iFrame.addClass("youtubeVideo");
                                        iFrame.html(src);


                                        pageSummaryResult.getParse().setHasYoutube(true);
                                    }
                                }
                            }
                        }


                        Elements imgTags = doc.select("img");

                        if (!imgTags.isEmpty()) {
                            int index = 0;
                            for (Element imgTag : imgTags) {
                                imgTag.attr("iter_key", String.valueOf(index++) + imgTag.attr("src"));

                                String imgSrc = imgTag.attr("src");

                                if (imgSrc.contains("thumb/")) {
                                    imgSrc = imgSrc.replace("thumb/", "");

                                    if (imgSrc.contains(".jpg")) {
                                        imgSrc = imgSrc.substring(0, imgSrc.indexOf(".jpg") + 4);
                                    }

                                    if (imgSrc.contains(".png")) {
                                        imgSrc = imgSrc.substring(0, imgSrc.indexOf(".png") + 4);
                                    }

                                    if (imgSrc.contains(".jpeg")) {
                                        imgSrc = imgSrc.substring(0, imgSrc.indexOf(".jpeg") + 5);
                                    }
                                    imgTag.attr("src", imgSrc);
                                }

                                if (imgSrc.contains(".gif")) {
                                    imgSrc = imgSrc.substring(0, imgSrc.indexOf(".gif") + 4);
                                    imgTag.attr("src", imgSrc);
                                }
                            }
                        }

                        pageSummaryResult.getParse().getText().setText(doc.html());

                        return pageSummaryResult;
                    }
                })
                .map(new Func1<PageSummaryResult, PageSummaryResult>() {
                    @Override
                    public PageSummaryResult call(PageSummaryResult pageSummaryResult) {
                        splitTextAndImages(pageSummaryResult);
                        getYoutubeVideos(pageSummaryResult);
                        addHeader(pageSummaryResult);
                        addTemplates(pageSummaryResult);
                        addLinks(pageSummaryResult);
                        addCategories(pageSummaryResult);
                        return pageSummaryResult;
                    }
                })
                .doOnNext(new Action1<PageSummaryResult>() {
                    @Override
                    public void call(PageSummaryResult pageSummaryResult) {
                        if (SettingsWorker.getInstance(getActivity()).isPagesCachingEnabled()) {
                            if (DBWorker.isPageSummarySaved(pageTitle)) {
                                pageIsRead = DBWorker.getPageIsRead(pageTitle);
                            } else {
                                pageIsRead = true;
                            }

                            DBWorker.savePageSummary(pageSummaryResult, pageIsRead, pageId, pageTitle);
                        }
                    }
                })
                .doOnNext(new Action1<PageSummaryResult>() {
                    @Override
                    public void call(PageSummaryResult pageSummaryResult) {
                        for (TextSection textSection : pageSummaryResult.getParse().getTextSections()) {
                            if (textSection.getType() == TextSection.TEXT_TYPE) {
                                textSection.setText(Html.fromHtml(textSection.getText().toString(), null, new HtmlTagHandler()));
                            }
                        }
                    }
                })
                .map(new Func1<PageSummaryResult, PageSummaryResult>() {
                    @Override
                    public PageSummaryResult call(PageSummaryResult pageSummaryResult) {
                        ArrayList<TextSection> newSections = new ArrayList<TextSection>();

                        for (TextSection textSection : pageSummaryResult.getParse().getTextSections()) {
                            if (textSection.getType() == TextSection.TEXT_TYPE) {
                                Pattern pattern = Pattern.compile("\n");
                                String htmlString = textSection.getText().toString();
                                String[] splited = pattern.split(htmlString);

                                for (String splitString : splited) {

                                    int start = htmlString.indexOf(splitString);
                                    int end = splitString.length() + start;
                                    try {
                                        newSections.add(new TextSection(TextSection.TEXT_TYPE, textSection.getText().subSequence(start, end)));
                                    } catch (IndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }
                                }


                            } else {
                                newSections.add(textSection);
                            }
                        }

                        pageSummaryResult.getParse().setTextSections(newSections);
                        return pageSummaryResult;
                    }
                })
                .doOnNext(new Action1<PageSummaryResult>() {
                    @Override
                    public void call(PageSummaryResult pageSummaryResult) {
                        for (TextSection textSection : pageSummaryResult.getParse().getTextSections()) {
                            if (textSection.getType() == TextSection.TEXT_TYPE) {
                                textSection.setText(StringUtils.trimTrailingWhitespace(textSection.getText()));
                            }
                        }
                    }
                })
                .flatMap(new Func1<PageSummaryResult, Observable<TextSection>>() {
                    @Override
                    public Observable<TextSection> call(PageSummaryResult pageSummaryResult) {
                        return Observable.from(pageSummaryResult.getParse().getTextSections());
                    }
                })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<TextSection>() {
                            @Override
                            public void onCompleted() {
                                isLoading = false;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.setVisibility(View.VISIBLE);

                                        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                                        animation.setDuration(600);

                                        recyclerView.setAnimation(animation);
                                        recyclerView.animate();
                                    }
                                }, 1000);

                                AlphaAnimation animation = new AlphaAnimation(1f, 0.0f);
                                animation.setDuration(1000);
                                animation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        loadingProgressBar.setVisibilityImmediate(View.GONE);
                                        getActivity().invalidateOptionsMenu();
                                        getActivity().setResult(Activity.RESULT_OK);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                loadingProgressBar.setAnimation(animation);
                                loadingProgressBar.animate();
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                                e.printStackTrace();
                                errorTextView.setVisibility(View.VISIBLE);

                                if (!NetworkUtils.isInternetAvailable(getActivity())) {
                                    errorTextView.setText(errorTextView.getText() + ", " + getString(R.string.no_internet_text));
                                }

                                isLoading = false;

                                loadingProgressBar.setVisibility(View.GONE);
                                getActivity().invalidateOptionsMenu();
                            }

                            @Override
                            public void onNext(TextSection section) {
                                adapter.getDisplayedData().add(section);
                            }
                        });
        bindToLifecycle(subscription);
    }

    private void getArticleByRealm() {
        recyclerView.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);

        Subscription subscription = Observable.just("")
                .flatMap(new Func1<String, Observable<PageSummaryRealm>>() {
                    @Override
                    public Observable<PageSummaryRealm> call(String s) {
                        if (pageId == null) {
                            return Observable.just(DBWorker.getPageSummary(pageTitle));
                        } else {
                            return Observable.just(DBWorker.getPageSummaryById(pageId));
                        }
                    }
                })
                .flatMap(new Func1<PageSummaryRealm, Observable<TextSection>>() {
                    @Override
                    public Observable<TextSection> call(PageSummaryRealm pageSummaryRealm) {
                        if (pageSummaryRealm == null) {
                            return Observable.empty();
                        }
                        ArrayList<TextSection> textSections = new ArrayList<TextSection>();
                        if (pageSummaryRealm.getTextSections() == null) {
                            return Observable.from(textSections);
                        }

                        TextSection textSection = null;

                        for (TextSectionRealm textSectionRealm : pageSummaryRealm.getTextSections()) {
                            textSection = null;
                            if (textSectionRealm.getType() == TextSection.CATEGORY_TYPE) {
                                textSection = new CategoriesTextSection(textSectionRealm.getCategoriesTitles());
                            } else if (textSectionRealm.getType() == TextSection.TEXT_TYPE) {
                                CharSequence htmlChars = Html.fromHtml(textSectionRealm.getText(), null, new HtmlTagHandler());
                                Pattern pattern = Pattern.compile("\n");
                                String htmlString = htmlChars.toString();

                                String[] splited = pattern.split(htmlChars);

                                for (String splitString : splited) {
                                    textSection = null;

                                    int start = htmlString.indexOf(splitString);
                                    int end = splitString.length() + start;
                                    try {
                                        textSection = new TextSection(TextSection.TEXT_TYPE, htmlChars.subSequence(start, end));
                                        textSection.setText(StringUtils.trimTrailingWhitespace(textSection.getText()));
                                        textSections.add(textSection);
                                    } catch (IndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }
                                }

                                textSection = null;
                            } else {
                                textSection = new TextSection(textSectionRealm.getType(), textSectionRealm.getText());
                            }

                            if (textSection != null) {
                                textSections.add(textSection);
                            }
                        }

                        return Observable.from(textSections);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TextSection>() {
                    @Override
                    public void onCompleted() {
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);

                        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                        animation.setDuration(300);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                getActivity().invalidateOptionsMenu();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        recyclerView.setAnimation(animation);
                        recyclerView.animate();

                        loadingProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                        getArticleByNetwork();
                    }

                    @Override
                    public void onNext(TextSection section) {
                        adapter.getDisplayedData().add(section);
                    }
                });

        bindToLifecycle(subscription);
    }

    private void splitTextAndImages(PageSummaryResult pageSummaryResult) {
        Document doc = Jsoup.parse(pageSummaryResult.getParse().getText().getText());
        doc.setBaseUri("https://mrakopedia.ru");

        String stringToSplit = pageSummaryResult.getParse().getText().getText();

        Elements imgTags = doc.select("img");

        if (!imgTags.isEmpty()) {
            for (Element imgTag : imgTags) {
                String[] splited = stringToSplit.split(Pattern.quote(imgTag.outerHtml()));
                if (splited.length > 1) {
                    stringToSplit = splited[1];
                } else {
                    stringToSplit = "";
                }

                pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, splited[0]));

                if (imgTag.absUrl("src").contains(".gif")) {
                    pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.GIF_TYPE, imgTag.absUrl("src")));
                } else {
                    pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.IMAGE_TYPE, imgTag.absUrl("src")));
                }
            }
            pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, stringToSplit));
        } else {
            pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, pageSummaryResult.getParse().getText().getText()));
        }
    }

    private void getYoutubeVideos(PageSummaryResult pageSummaryResult) {
        if (!pageSummaryResult.getParse().isHasYoutube()) {
            return;
        }

        TextSection textSection;
        Document doc;
        Elements youtubeTags;
        ArrayList<TextSection> newSections = new ArrayList<>();
        String stringToSplit;
        String[] splitedString;

        for (int i = 0; i < pageSummaryResult.getParse().getTextSections().size(); i++) {
            textSection = pageSummaryResult.getParse().getTextSections().get(i);

            if (textSection.getType() == TextSection.TEXT_TYPE && textSection.getText().toString().contains("youtubeVideo")) {
                doc = Jsoup.parse(textSection.getText().toString());
                stringToSplit = textSection.getText().toString();

                youtubeTags = doc.select("p.youtubeVideo");

                if (!youtubeTags.isEmpty()) {
                    for (Element youtubeElement : youtubeTags) {
                        splitedString = stringToSplit.split(Pattern.quote(youtubeElement.outerHtml()));

                        newSections.add(new TextSection(TextSection.TEXT_TYPE, splitedString[0]));
                        newSections.add(new TextSection(TextSection.YOUTUBE_TYPE, youtubeElement.html()));

                        if (splitedString.length > 1) {
                            stringToSplit = splitedString[1];
                        }
                    }
                }

                newSections.add(new TextSection(TextSection.TEXT_TYPE, stringToSplit));
            } else {
                newSections.add(textSection);
            }
        }

        pageSummaryResult.getParse().setTextSections(newSections);
    }

    private void addHeader(PageSummaryResult pageSummaryResult) {
        pageSummaryResult
                .getParse()
                .getTextSections()
                .add(0, new TextSection(
                        TextSection.TEXT_TYPE,
                        "<h2>" + pageSummaryResult.getParse().getTitle() + "</h2>"));
    }

    private void addTemplates(PageSummaryResult pageSummaryResult) {
        TextSection textSection = null;

        for (Templates template : pageSummaryResult.getParse().getTemplates()) {
            switch (template.getTitle()) {
                case "Шаблон:NSFW":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "NSFW");
                    break;
                case "Шаблон:Anomaly":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "Anomaly");
                    break;
                case "Шаблон:Parody":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "Parody");
                    break;
                case "Шаблон:Save":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "Save");
                    break;
                case "Шаблон:Vg":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "Vg");
                    break;
                case "Шаблон:WTF":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "WTF");
                    break;
                case "Шаблон:Избранное":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "Избранное");
                    break;
                case "Шаблон:КГАМ":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "КГАМ");
                    break;
                case "Шаблон:Классика":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "Классика");
                    break;
                case "Шаблон:НПЧДХ":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "НПЧДХ");
                    break;
                case "Шаблон:Юмор":
                    textSection = new TextSection(TextSection.TEMPLATE_TYPE, "Юмор");
                    break;
                default:
                    textSection = null;
            }

            if (textSection != null) {
                pageSummaryResult.getParse().getTextSections().add(0, textSection);
            }
        }
    }

    private void addLinks(PageSummaryResult pageSummaryResult) {

        boolean headerAdded = false;
        boolean toSkip = false;
        TextSection lastSection = pageSummaryResult.getParse().getTextSections().get(pageSummaryResult.getParse().getTextSections().size() - 1);

        lastSection.setText(lastSection.getText().toString().replaceAll(Pattern.quote("См.также"), "Смотри также"));
        lastSection.setText(lastSection.getText().toString().replaceAll(Pattern.quote("См. также"), "Смотри также"));
    }

    private void addCategories(PageSummaryResult pageSummaryResult) {
        CategoriesTextSection categoryWrapper = new CategoriesTextSection();

        for (Categories category : pageSummaryResult.getParse().getCategories()) {
            boolean toSkip = false;
            category.setTitle(category.getTitle().replaceAll("_", " "));

            for (String banString : Utils.categoriesBanList) {
                if (category.getTitle().contains(banString)) {
                    toSkip = true;
                    break;
                }
            }

            if (!toSkip) {
                categoryWrapper.addCategory(category.getTitle());
            }
        }

        if (!categoryWrapper.getCategoriesArrayList().isEmpty()) {
            pageSummaryResult.getParse()
                    .getTextSections().add(categoryWrapper);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isOptionsShown) {
            closeOptions();
            return false;
        }

        return true;
    }
}
