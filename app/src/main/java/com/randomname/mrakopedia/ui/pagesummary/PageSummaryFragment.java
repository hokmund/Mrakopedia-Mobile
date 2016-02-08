package com.randomname.mrakopedia.ui.pagesummary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.pagesummary.Categories;
import com.randomname.mrakopedia.models.api.pagesummary.CategoriesTextSection;
import com.randomname.mrakopedia.models.api.pagesummary.Links;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.Sections;
import com.randomname.mrakopedia.models.api.pagesummary.Templates;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.models.realm.TextSectionRealm;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.categorymembers.CategoryMembersActivity;
import com.randomname.mrakopedia.ui.fullscreenfoto.FullScreentFotoActivity;
import com.randomname.mrakopedia.ui.views.selection.SelectableLayoutManager;
import com.randomname.mrakopedia.ui.views.selection.SelectableRecyclerView;
import com.randomname.mrakopedia.ui.views.selection.SelectionCallback;
import com.randomname.mrakopedia.utils.NetworkUtils;
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
import carbon.widget.ProgressBar;
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
public class PageSummaryFragment extends RxBaseFragment {

    private static final String TAG = "PageSummaryFragment";
    private static final String PAGE_TITLE_KEY = "pageTitleKey";
    private static final String PAGE_ID_KEY = "pageIdKey";

    private static final String TEXT_SECTIONS_KEY = "textSectionsKey";

    private String pageTitle;
    private String pageId;
    private boolean pageIsFavorite = false;
    private boolean pageIsRead = false;

    @Bind(R.id.page_summary_recycler_view)
    SelectableRecyclerView recyclerView;

    @Bind(R.id.error_text_view)
    carbon.widget.TextView errorTextView;

    @Bind(R.id.loading_progress_bar)
    ProgressBar loadingProgressBar;

    private ArrayList<TextSection> textSections;
    private PageSummaryAdapter adapter;

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
        } else {
            textSections = new ArrayList<>();
        }

        pageIsFavorite = DBWorker.getPageIsFavorite(pageTitle);
        pageIsRead = DBWorker.getPageIsRead(pageTitle);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_summary_fragment, null);
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
                        imageArray.add(section.getText());
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

        LinearLayoutManager manager = new SelectableLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        recyclerView.setSelectionCallback(new SelectionCallback() {
            @Override
            public void startSelection() {
                ((PageSummaryActivity) getActivity()).startSelection();
            }

            @Override
            public void stopSelection() {
                ((PageSummaryActivity) getActivity()).stopSelection();
            }
        });
        recyclerView.addOnScrollListener(((PageSummaryActivity)getActivity()).toolbarHideListener);

        if (adapter.getDisplayedData().size() <= 1) {
            if (DBWorker.isPageSummarySavedById(pageId) || DBWorker.isPageSummarySaved(pageTitle)) {
                getArticleByRealm();
            } else {
                getArticleByNetwork();
            }
        }

        return view;
    }

    @Override
    public void onConnectedToInternet() {
        if (adapter.getDisplayedData().size() <= 1) {
            getArticleByNetwork();
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (adapter.getDisplayedData().size() > 1) {
            inflater.inflate(R.menu.menu_page_summary, menu);

            setMenuFavoriteStatus(menu.findItem(R.id.action_favorite_page));
            setMenuReadStatus(menu.findItem(R.id.action_read_page));
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
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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
        super.onSaveInstanceState(outState);
    }

    private void getArticleByNetwork() {
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
                        Document doc = Jsoup.parse(pageSummaryResult.getParse().getText().getText());

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

                        Elements spoilerLinks = doc.select("a.spoilerLink");
                        if (!spoilerLinks.isEmpty()) {
                            spoilerLinks.remove();
                        }

                        Elements spoilersButtons = doc.select("span.spoilers-button");
                        if (!spoilersButtons.isEmpty()) {
                            spoilersButtons.remove();
                        }

                        Elements delTag = doc.select("del");
                        if (!delTag.isEmpty()) {
                            delTag.tagName("strike");
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

                        Elements boxDivs = doc.select("div.box");

                        if (!boxDivs.isEmpty()) {
                            boxDivs.remove();
                        }

                        Elements editSections = doc.select("span.mw-editsection");

                        if (!editSections.isEmpty()) {
                            editSections.remove();
                        }

                        Elements liTags = doc.select("li");

                        if (!liTags.isEmpty()) {
                            for (Element liTag: liTags) {
                                liTag.tagName("p");
                            }
                        }



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
                        if (DBWorker.isPageSummarySaved(pageTitle)) {
                            pageIsRead = DBWorker.getPageIsRead(pageTitle);
                        } else {
                            pageIsRead = true;
                        }

                        DBWorker.savePageSummary(pageSummaryResult, pageIsRead, pageId, pageTitle);
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
        Observable<PageSummaryRealm> observable;

        if (pageId == null) {
            observable = DBWorker.getPageSummary(pageTitle);
        } else {
            observable = DBWorker.getPageSummaryById(pageId);
        }


        Subscription subscription = observable
                .flatMap(new Func1<PageSummaryRealm, Observable<TextSectionRealm>>() {
                    @Override
                    public Observable<TextSectionRealm> call(PageSummaryRealm pageSummaryRealm) {
                        return Observable.from(pageSummaryRealm.getTextSections());
                    }
                })
                .map(new Func1<TextSectionRealm, TextSection>() {
                    @Override
                    public TextSection call(TextSectionRealm textSectionRealm) {
                        if (textSectionRealm.getType() == TextSection.CATEGORY_TYPE) {
                            return new CategoriesTextSection(textSectionRealm.getCategoriesTitles());
                        } else {
                            return new TextSection(textSectionRealm.getType(), textSectionRealm.getText());
                        }
                    }
                })
                .subscribe(new Subscriber<TextSection>() {
                    @Override
                    public void onCompleted() {
                        recyclerView.setVisibility(View.VISIBLE);

                        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                        animation.setDuration(300);

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
                        adapter.notifyItemInserted(adapter.getDisplayedData().indexOf(section));
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

            if (textSection.getType() == TextSection.TEXT_TYPE && textSection.getText().contains("youtubeVideo")) {
                doc = Jsoup.parse(textSection.getText());
                stringToSplit = textSection.getText();

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

        lastSection.setText(lastSection.getText().replaceAll(Pattern.quote("См.также"), "Смотри также"));
        lastSection.setText(lastSection.getText().replaceAll(Pattern.quote("См. также"), "Смотри также"));
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
}
