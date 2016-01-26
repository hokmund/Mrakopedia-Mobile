package com.randomname.mrakopedia.ui.pagesummary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

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
import com.randomname.mrakopedia.utils.NetworkUtils;
import com.randomname.mrakopedia.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Array;
import java.util.ArrayList;

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

    private static final String TEXT_SECTIONS_KEY = "textSectionsKey";

    private String pageTitle;
    private boolean pageIsFavorite = false;

    @Bind(R.id.page_summary_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.error_text_view)
    carbon.widget.TextView errorTextView;

    @Bind(R.id.loading_progress_bar)
    ProgressBar loadingProgressBar;

    private ArrayList<TextSection> textSections;
    private PageSummaryAdapter adapter;

    public PageSummaryFragment() {
    }

    public static PageSummaryFragment getInstance(String pageTitle) {
        PageSummaryFragment fragment = new PageSummaryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PAGE_TITLE_KEY, pageTitle);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            pageTitle = getArguments().getString(PAGE_TITLE_KEY);
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
                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, textSections.get(position).getText());
                startActivity(intent);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);

                ArrayList<String> imageArray = new ArrayList<>();

                for (TextSection section : textSections) {
                    if (section.getType() == TextSection.IMAGE_TYPE) {
                        imageArray.add(section.getText());
                    }
                }

                Intent intent = new Intent(getActivity(), FullScreentFotoActivity.class);
                intent.putExtra(FullScreentFotoActivity.IMAGE_ARRAY_KEY, imageArray);
                intent.putExtra(FullScreentFotoActivity.SELECTED_IMAGE_KEY, imageArray.indexOf(textSections.get(position).getText()));
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        if (textSections.isEmpty()) {
            if (DBWorker.isPageSummarySaved("")) {
                getArticleByRealm();
            } else {
                getArticleByNetwork();
            }
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_page_summary, menu);

        MenuItem favoriteItem = menu.findItem(R.id.action_favorite_page);
        setMenuFavoriteStatus(favoriteItem);

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
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setMenuFavoriteStatus(MenuItem favoriteItem) {
        if (pageIsFavorite) {
            favoriteItem.setIcon(R.drawable.ic_star_black_48dp);
            favoriteItem.setTitle("Удалить из избранного");
        } else {
            favoriteItem.setIcon(R.drawable.ic_star_outline_black_48dp);
            favoriteItem.setTitle("Добавить в избранное");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!textSections.isEmpty()) {
            outState.putParcelableArrayList(TEXT_SECTIONS_KEY, textSections);
        }
        super.onSaveInstanceState(outState);
    }

    private void getArticleByNetwork() {
        recyclerView.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);

        Subscription subscription = MrakopediaApiWorker
                .getInstance()
                .getPageSummary(pageTitle)
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

                        Elements embedVideos = doc.select(".embedvideo");

                        if (!embedVideos.isEmpty()) {
                            embedVideos.remove();
                        }

                        Elements spoilerLinks = doc.select("a.spoilerLink");
                        if (!spoilerLinks.isEmpty()) {
                            spoilerLinks.remove();
                        }

                        Elements delTag = doc.select("del");
                        if (!delTag.isEmpty()) {
                            delTag.tagName("strike");
                        }

                        Elements aTags = doc.select("a");

                        if (!aTags.isEmpty()) {
                            for (Element aTag : aTags) {
                                if (aTag.attr("abs:href").length() == 0) {
                                    aTag.unwrap();
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

                        if (pageSummaryResult.getParse().getSections().length > 0) {
                            for (Sections section : pageSummaryResult.getParse().getSections()) {
                                if (section.getLine().equals("См. также")) {
                                    Elements lookMore = doc.select("[id^=" + section.getAnchor() + "]");
                                    if (!lookMore.isEmpty()) {
                                        Element lookMoreParent = lookMore.first().parent();

                                        if (lookMoreParent != null) {
                                            while (lookMoreParent.nextElementSibling() != null) {
                                                lookMoreParent.nextElementSibling().remove();
                                            }
                                            lookMoreParent.remove();
                                        }
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
                        DBWorker.savePageSummary(pageSummaryResult);
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
                        errorTextView.setVisibility(View.VISIBLE);

                        if (!NetworkUtils.isInternetAvailable(getActivity())) {
                            errorTextView.setText(errorTextView.getText() + ", " + getString(R.string.no_internet_text));
                        }

                        loadingProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(TextSection section) {
                        textSections.add(section);
                        adapter.notifyItemInserted(textSections.indexOf(section));
                    }
                });
        bindToLifecycle(subscription);
    }

    private void getArticleByRealm() {
        Subscription subscription = DBWorker.getPageSummary(pageTitle)
                .flatMap(new Func1<PageSummaryRealm, Observable<TextSectionRealm>>() {
                    @Override
                    public Observable<TextSectionRealm> call(PageSummaryRealm pageSummaryRealm) {
                        return Observable.from(pageSummaryRealm.getTextSections());
                    }
                })
                .map(new Func1<TextSectionRealm, TextSection>() {
                    @Override
                    public TextSection call(TextSectionRealm textSectionRealm) {
                        return new TextSection(textSectionRealm.getType(), textSectionRealm.getText());
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
                        textSections.add(section);
                        adapter.notifyItemInserted(textSections.indexOf(section));
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
                String[] splited = stringToSplit.split(imgTag.outerHtml());
                if (splited.length > 1) {
                    stringToSplit = splited[1];
                } else {
                    stringToSplit = "";
                }

                pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, splited[0]));
                pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.IMAGE_TYPE, imgTag.absUrl("src")));
            }
            pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, stringToSplit));
        } else {
            pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, pageSummaryResult.getParse().getText().getText()));
        }
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

        for (Links link : pageSummaryResult.getParse().getLinks()) {
            if (link.getTitle().contains("Шаблон") || link.getTitle().contains("Категория")) {
                continue;
            }

            if (!headerAdded) {
                pageSummaryResult.getParse()
                        .getTextSections()
                        .add(new TextSection(
                                TextSection.TEXT_TYPE,
                                "<h2>Смотри также</h2>"));
                headerAdded = true;
            }

            pageSummaryResult.getParse()
                    .getTextSections()
                    .add(new TextSection(
                            TextSection.LINK_TYPE,
                            link.getTitle()));
        }
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
