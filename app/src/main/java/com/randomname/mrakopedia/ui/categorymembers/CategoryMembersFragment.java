package com.randomname.mrakopedia.ui.categorymembers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.randomname.mrakopedia.MrakopediaApplication;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.categorydescription.CategoryDescription;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.models.api.categorymembers.Categorymembers;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryActivity;
import com.randomname.mrakopedia.ui.settings.SettingsWorker;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.utils.NetworkUtils;
import com.randomname.mrakopedia.utils.StringUtils;
import com.randomname.mrakopedia.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.ProgressBar;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Vlad on 19.01.2016.
 */
public class CategoryMembersFragment extends RxBaseFragment {
    private static final String TAG = "categoryMembersFragment";
    private static final String CATEGORY_TITLE_KEY = "categoryTitleKey";

    private static final String CATEGORY_MEMBERS_KEY = "categoryMembersKey";
    private static final String DESCRIPTION_SECTIONS_KEY = "descriptionSectionsKey";
    private static final String CONTINUE_STRING_KEY = "continueStringKey";
    private static final String SELECTED_POSITION_KEY = "selectedPositionKey";

    private static final int PAGE_SUMMARY_ACTIVITY_CODE = 11;

    @Bind(R.id.category_members_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.error_text_view)
    carbon.widget.TextView errorTextView;
    @Bind(R.id.loading_progress_bar)
    ProgressBar loadingProgressBar;

    private boolean isLoading = false;
    private CategoryMembersAdapter adapter;
    private LinearLayoutManager manager;

    private ArrayList<Categorymembers> categorymembersArrayList;
    private ArrayList<TextSection> descriptionSections;
    private String continueString = "";
    private String categoryTitle;
    private int selectedPosition = 0;

    private Tracker mTracker;

    public CategoryMembersFragment() {}

    public static CategoryMembersFragment getInstance(String categoryTitle) {
        CategoryMembersFragment fragment = new CategoryMembersFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CATEGORY_TITLE_KEY, categoryTitle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (savedInstanceState != null) {
            categorymembersArrayList = savedInstanceState.getParcelableArrayList(CATEGORY_MEMBERS_KEY);
            continueString = savedInstanceState.getString(CONTINUE_STRING_KEY, null);
            selectedPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY, 0);
            descriptionSections = savedInstanceState.getParcelableArrayList(DESCRIPTION_SECTIONS_KEY);
        } else {
            categorymembersArrayList = new ArrayList<>();
        }

        if (bundle != null) {
            String title = bundle.getString(CATEGORY_TITLE_KEY);
            if (title != null) {
                categoryTitle = title;
            }
        }

        setHasOptionsMenu(true);

        MrakopediaApplication application = (MrakopediaApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_members_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new CategoryMembersAdapter(getActivity(), categorymembersArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                position -= adapter.getDescriptionCount();

                if (position < 0) {
                    return;
                }

                selectedPosition = position;
                Intent intent = new Intent(getActivity(), PageSummaryActivity.class);
                Categorymembers categoryMember = adapter.getDisplayedData().get(position);

                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, categoryMember.getTitle());

                if (categoryMember.getPageid() != null && !categoryMember.getPageid().isEmpty()) {
                    intent.putExtra(PageSummaryActivity.PAGE_ID_EXTRA, adapter.getDisplayedData().get(position).getPageid());
                }

                startActivityForResult(intent, PAGE_SUMMARY_ACTIVITY_CODE);
            }
        });

        manager = new LinearLayoutManager(getActivity());
        manager.setSmoothScrollbarEnabled(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(manager) {
            @Override
            public void onLoadMore(int current_page) {
                loadCategoryMembers();
            }
        });
        recyclerView.addOnScrollListener(((CategoryMembersActivity) getActivity()).toolbarHideRecyclerOnScrollListener);

        if (categorymembersArrayList.isEmpty()) {
            loadCategoryMembers();
            getCategoryDescription();
        } else {
            if (descriptionSections != null) {
                adapter.setDescriptionSections(descriptionSections);
            }

            recyclerView.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_category_members, menu);
        setShowRatingStatus(menu.findItem(R.id.action_show_rating));

        if(Build.VERSION.SDK_INT < 21) {
            final ViewTreeObserver viewTreeObserver = getActivity().getWindow().getDecorView().getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View menuItem = getActivity().findViewById(R.id.action_show_rating);

                    if (menuItem != null) {
                        Utils.setRippleToMenuItem(menuItem, getActivity());
                    }

                    if (Build.VERSION.SDK_INT < 16) {
                        getActivity().getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_show_rating) {
            SettingsWorker settingsWorker = SettingsWorker.getInstance(getActivity());

            boolean showByRating = settingsWorker.showByRating();
            settingsWorker.setShowByRating(!showByRating);

            categorymembersArrayList.clear();
            adapter.notifyDataSetChanged();
            continueString = "";

            loadCategoryMembers();

            setShowRatingStatus(item);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setShowRatingStatus(MenuItem menuItem) {
        boolean showByRating = SettingsWorker.getInstance(getActivity()).showByRating();

        if (showByRating) {
            menuItem.setIcon(R.drawable.ic_sort_descending_white_24dp);
            menuItem.setTitle(R.string.show_ratings);
        } else {
            menuItem.setIcon(R.drawable.ic_sort_alphabetical_white_24dp);
            menuItem.setTitle(R.string.show_alphabetical);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAGE_SUMMARY_ACTIVITY_CODE) {
            adapter.getDisplayedData().get(selectedPosition).setIsViewed(DBWorker.getPageIsRead(adapter.getDisplayedData().get(selectedPosition).getTitle()));
            adapter.notifyDataSetChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(CATEGORY_MEMBERS_KEY, categorymembersArrayList);
        outState.putString(CONTINUE_STRING_KEY, continueString);
        outState.putInt(SELECTED_POSITION_KEY, selectedPosition);
        outState.putParcelableArrayList(DESCRIPTION_SECTIONS_KEY, adapter.getDescriptionSections());

        super.onSaveInstanceState(outState);
    }

    public void setFilter(String filter) {
        adapter.setFilter(filter);
    }

    @Override
    public void onConnectedToInternet() {
        if (categorymembersArrayList.isEmpty()) {
            loadCategoryMembers();
        }

        if (adapter.getDescriptionCount() <= 2) {
            getCategoryDescriptionByNetwork();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG + " " +categoryTitle);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void loadCategoryMembersRating() {
        if (!categorymembersArrayList.isEmpty()) {
            return;
        }

        isLoading = true;

        MrakopediaApiWorker.getInstance().getCategoryRating(categoryTitle).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response) {
                String htmlString;

                try {
                    htmlString = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                    htmlString = null;
                    isLoading = false;
                }

                if (htmlString != null) {
                    parseRatingPage(htmlString);
                } else {
                    isLoading = false;
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                isLoading = false;
            }
        });
    }

    private void parseRatingPage(String html) {
        Subscription subscription =
                Observable.just(html)
                .flatMap(new Func1<String, Observable<Categorymembers>>() {
                    @Override
                    public Observable<Categorymembers> call(String s) {
                        Document document = Jsoup.parse(s);
                        Element ratingTable = document.select("table.w4g_rb-ratinglist-table").first();

                        if (ratingTable == null) {
                            return Observable.empty();
                        }

                        ArrayList<Categorymembers> categorymembers = new ArrayList<>();
                        Elements rows = ratingTable.select("tr");

                        Categorymembers categorymember;
                        Elements td;
                        Element tempElement;

                        for (Element row : rows) {

                            td = row.select("td");

                            if (td.isEmpty()) {
                                continue;
                            }

                            tempElement = td.get(0);
                            tempElement = tempElement.select("a").first();

                            if (tempElement == null) {
                                continue;
                            }

                            categorymember = new Categorymembers();

                            categorymember.setTitle(Html.fromHtml(tempElement.html()).toString());
                            categorymember.setNs("");
                            categorymember.setType("");
                            categorymember.setPageid("");
                            categorymember.setIsViewed(false);

                            tempElement = td.get(1);

                            if (tempElement != null) {
                                categorymember.setRating(tempElement.html());
                            }

                            categorymembers.add(categorymember);
                        }

                        return Observable.from(categorymembers);
                    }
                })
                .subscribe(new Subscriber<Categorymembers>() {
                    @Override
                    public void onCompleted() {
                        isLoading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                        isLoading = false;

                        if (categorymembersArrayList.isEmpty()) {
                            getCategoryMembersFromRealm();
                        }
                    }

                    @Override
                    public void onNext(Categorymembers categorymembers) {
                        categorymembersArrayList.add(categorymembers);
                        adapter.notifyItemInserted(categorymembersArrayList.indexOf(categorymembers) + adapter.getDescriptionSections().size());

                        checkIfPageWasRead(categorymembers);
                    }
                });

        bindToLifecycle(subscription);
    }

    private void loadCategoryMembers() {
        if ((continueString == null && !categorymembersArrayList.isEmpty()) || (isLoading)) {
            return;
        }

        if (SettingsWorker.getInstance(getActivity()).showByRating()) {
            loadCategoryMembersRating();
            return;
        }

        String continueStringSaved = continueString;
        continueString = null;
        isLoading = true;
        Subscription getCategoryMembersSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getCategoryMembers("Категория:" + categoryTitle, continueStringSaved)
                        .doOnNext(new Action1<CategoryMembersResult>() {
                            @Override
                            public void call(CategoryMembersResult categoryMembersResult) {
                                if (categoryMembersResult.getmContinue() != null) {
                                    continueString = categoryMembersResult.getmContinue().getCmcontinue();
                                } else {
                                    continueString = null;
                                }
                            }
                        })
                        .flatMap(new Func1<CategoryMembersResult, Observable<Categorymembers>>() {
                            @Override
                            public Observable<Categorymembers> call(CategoryMembersResult categoryMembersResult) {
                                return Observable.from(categoryMembersResult.getQuery().getCategorymembers());
                            }
                        })
                        .filter(new Func1<Categorymembers, Boolean>() {
                            @Override
                            public Boolean call(Categorymembers categorymembers) {
                                return !categorymembers.getType().equals("subcat");
                            }
                        })
                        .filter(new Func1<Categorymembers, Boolean>() {
                            @Override
                            public Boolean call(Categorymembers category) {
                                for (String banString : Utils.pagesBanList) {
                                    if (category.getTitle().contains(banString)) {
                                        return false;
                                    }
                                }

                                return true;
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Categorymembers>() {
                            @Override
                            public void onCompleted() {
                                isLoading = false;
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();

                                isLoading = false;

                                if (categorymembersArrayList.isEmpty()) {
                                    getCategoryMembersFromRealm();
                                }
                            }

                            @Override
                            public void onNext(Categorymembers categorymembers) {
                                categorymembersArrayList.add(categorymembers);
                                adapter.notifyItemInserted(categorymembersArrayList.indexOf(categorymembers) + adapter.getDescriptionSections().size());

                                checkIfPageWasRead(categorymembers);
                            }
                        });

        bindToLifecycle(getCategoryMembersSubscription);
    }

    private void getCategoryMembersFromRealm() {
        Subscription subscription =
                DBWorker.getCategoryMembers(categoryTitle)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        recyclerView.setVisibility(View.VISIBLE);
                        loadingProgressBar.setVisibilityImmediate(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        Categorymembers categorymembers = new Categorymembers();
                        categorymembers.setTitle(s);
                        categorymembers.setPageid(null);
                        categorymembers.setIsViewed(true);
                        categorymembers.setNs("");
                        categorymembers.setType("");

                        categorymembersArrayList.add(categorymembers);
                        adapter.notifyItemInserted(categorymembersArrayList.indexOf(categorymembers) + adapter.getDescriptionSections().size());

                        checkIfPageWasRead(categorymembers);
                    }
                });

        bindToLifecycle(subscription);
    }

    private void getCategoryDescription() {

        if (!SettingsWorker.getInstance(getActivity()).isPagesCachingEnabled()) {
            getCategoryDescriptionByNetwork();
            return;
        }

        Subscription getDescriptionFromRealm =
                DBWorker.getCategoryDescription(categoryTitle)
                .subscribe(new Subscriber<TextSection>() {
                    @Override
                    public void onCompleted() {
                        if (adapter.getDescriptionSections().isEmpty()) {
                            getCategoryDescriptionByNetwork();
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerView.setVisibility(View.VISIBLE);

                                    AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                                    animation.setDuration(600);

                                    recyclerView.setAnimation(animation);
                                    recyclerView.animate();
                                }
                            }, 600);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        getCategoryDescriptionByNetwork();
                    }

                    @Override
                    public void onNext(TextSection textSection) {
                        adapter.addDescriptionSection(textSection);
                    }
                });

        bindToLifecycle(getDescriptionFromRealm);
    }

    private void getCategoryDescriptionByNetwork() {

        for (int i = 0; i < Utils.redirectCategoriesDescriptions.length; i++) {
            if (Utils.redirectCategoriesDescriptions[i].equals(categoryTitle)) {
                categoryTitle = Utils.redirectCategoriesDescriptionsTitles[i];
                break;
            }
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        Subscription getCategoryDescriptionSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getCategoryDescription(categoryTitle)
                        .map(new Func1<CategoryDescription, CategoryDescription>() {
                            @Override
                            public CategoryDescription call(CategoryDescription categoryDescription) {
                                for (int i = 0; i < Utils.categoriesDescriptionsTitles.length; i++) {
                                    if (Utils.categoriesDescriptionsTitles[i].equals(categoryTitle)) {
                                        categoryDescription.getParse().getText().setText(Utils.categoryDescriptions[i]);
                                        return categoryDescription;
                                    }
                                }


                                if (categoryDescription.getParse() != null && categoryDescription.getParse().getText() != null && categoryDescription.getParse().getText().getText() != null) {
                                    Document doc = Jsoup.parse(categoryDescription.getParse().getText().getText());

                                    Elements aTags = doc.select("a");

                                    if (!aTags.isEmpty()) {
                                        for (Element aTag : aTags) {
                                            if (aTag.attr("abs:href").length() == 0) {
                                                aTag.unwrap();
                                            }
                                        }
                                    }

                                    Elements editSections = doc.select("span.mw-editsection");

                                    if (!editSections.isEmpty()) {
                                        editSections.remove();
                                    }

                                    Elements liTags = doc.select("li");

                                    if (!liTags.isEmpty()) {
                                        for (Element liTag : liTags) {
                                            liTag.tagName("p");
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

                                    String newText = doc.html();

                                    if (newText.contains("Рейтинговая таблица всех историй")) {
                                        newText = newText.substring(0, newText.indexOf("Рейтинговая таблица всех историй"));
                                    }

                                    categoryDescription.getParse().getText().setText(newText);
                                }

                                return categoryDescription;
                            }
                        })
                        .map(new Func1<CategoryDescription, CategoryDescription>() {
                            @Override
                            public CategoryDescription call(CategoryDescription categoryDescription) {
                                splitTextAndImages(categoryDescription);
                                return categoryDescription;
                            }
                        })
                        .map(new Func1<CategoryDescription, CategoryDescription>() {
                            @Override
                            public CategoryDescription call(CategoryDescription categoryDescription) {
                                TextSection lastSection = categoryDescription.getTextSections().get(categoryDescription.getTextSections().size() - 1);

                                if (lastSection.getText().toString().contains("См.также")) {
                                    Log.e(TAG, "contains 1");
                                    lastSection.setText(lastSection.getText().toString().split(Pattern.quote("См.также"))[0]);
                                }

                                if (lastSection.getText().toString().contains("Смотри также")) {
                                    Log.e(TAG, "contains 2");
                                    lastSection.setText(lastSection.getText().toString().split(Pattern.quote("Смотри также"))[0]);
                                }

                                if (lastSection.getText().toString().contains("См. также")) {
                                    Log.e(TAG, "contains 3");
                                    lastSection.setText(lastSection.getText().toString().split(Pattern.quote("См. также"))[0]);
                                }

                                categoryDescription.getTextSections().set(categoryDescription.getTextSections().size() - 1, lastSection);

                                return categoryDescription;
                            }
                        })
                        .doOnNext(new Action1<CategoryDescription>() {
                            @Override
                            public void call(CategoryDescription categoryDescription) {
                                if (SettingsWorker.getInstance(getActivity()).isPagesCachingEnabled()) {
                                    DBWorker.saveCategoryDescription(categoryDescription, categoryTitle);
                                }
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<CategoryDescription>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                                e.printStackTrace();
                                if (categorymembersArrayList.isEmpty()) {
                                    errorTextView.setVisibility(View.VISIBLE);
                                    loadingProgressBar.setVisibility(View.GONE);
                                    if (!NetworkUtils.isInternetAvailable(getActivity())) {
                                        errorTextView.setText(getString(R.string.error_loading_category) + " " + getString(R.string.no_internet_text));
                                    }
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    adapter.setDescriptionSections(new ArrayList<TextSection>());
                                }
                            }

                            @Override
                            public void onNext(CategoryDescription categoryDescription) {
                                adapter.setDescriptionSections(categoryDescription.getTextSections());
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
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                loadingProgressBar.setAnimation(animation);
                                loadingProgressBar.animate();
                            }
                        });

        bindToLifecycle(getCategoryDescriptionSubscription);
    }

    private void checkIfPageWasRead(final Categorymembers categorymember) {
        Subscription subscription =
                Observable.
                        just(categorymember)
                        .flatMap(new Func1<Categorymembers, Observable<Boolean>>() {
                            @Override
                            public Observable<Boolean> call(Categorymembers categorymember) {
                                return Observable.just(DBWorker.getPageIsRead(categorymember.getTitle()));
                            }
                        })
                        .filter(new Func1<Boolean, Boolean>() {
                            @Override
                            public Boolean call(Boolean aBoolean) {
                                return aBoolean;
                            }
                        })
                        .subscribe(new Subscriber<Boolean>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                adapter.getDisplayedData()
                                        .get(adapter.getDisplayedData()
                                                .indexOf(categorymember))
                                        .setIsViewed(aBoolean);

                                int childIndex = categorymembersArrayList.indexOf(categorymember) + adapter.getDescriptionSections().size();

                                if (childIndex <= manager.findLastVisibleItemPosition()) {
                                    adapter.notifyItemChanged(childIndex);
                                }
                            }
                        });

        bindToLifecycle(subscription);

    }

    private void splitTextAndImages(CategoryDescription categoryDescription) {
        if (categoryDescription.getParse() == null || categoryDescription.getParse().getText() == null || categoryDescription.getParse().getText().getText() == null) {
            return;
        }

        Document doc = Jsoup.parse(categoryDescription.getParse().getText().getText());
        doc.setBaseUri("https://mrakopedia.ru");

        String stringToSplit = categoryDescription.getParse().getText().getText();

        Elements imgTags = doc.select("img");

        if (!imgTags.isEmpty()) {
            for (Element imgTag : imgTags) {
                String[] splited = stringToSplit.split(imgTag.outerHtml());
                if (splited.length > 1) {
                    stringToSplit = splited[1];
                } else {
                    stringToSplit = "";
                }

                categoryDescription.getTextSections().add(new TextSection(TextSection.TEXT_TYPE, splited[0]));
                categoryDescription.getTextSections().add(new TextSection(TextSection.IMAGE_TYPE, imgTag.absUrl("src")));
            }
            categoryDescription.getTextSections().add(new TextSection(TextSection.TEXT_TYPE, stringToSplit));
        } else {
            categoryDescription.getTextSections().add(new TextSection(TextSection.TEXT_TYPE, categoryDescription.getParse().getText().getText()));
        }
    }

    private class CategoryMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int LIST_TYPE = 0;
        public static final int TEXT_TYPE = 1;
        public static final int IMAGE_TYPE = 2;
        private static final int SEPARATOR_TYPE = 7;

        private Context context;
        private String filter = "";

        private ArrayList<Categorymembers> categorymembersArrayList;

        private View.OnClickListener onClickListener;
        private ArrayList<TextSection> descriptionSections;

        private DisplayImageOptions options;

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public ArrayList<Categorymembers> getDisplayedData() {
            return categorymembersArrayList;
        }

        public CategoryMembersAdapter(Context context, ArrayList<Categorymembers> categorymembers, View.OnClickListener onClickListener) {
            this.context = context;
            categorymembersArrayList = categorymembers;
            this.onClickListener = onClickListener;
            descriptionSections = new ArrayList<>();

            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT);

            if (SettingsWorker.getInstance(getActivity()).isPhotoCachingEnabled()) {
                builder.cacheOnDisk(true);
            }

            options = builder.build();
        }

        public void setDescriptionSections(ArrayList<TextSection> sections) {
            descriptionSections = sections;

            if (descriptionSections.isEmpty() || descriptionSections.get(0).getType() != TextSection.SPACER_TYPE) {
                descriptionSections.add(0, new TextSection(TextSection.SPACER_TYPE, ""));
                descriptionSections.add(new TextSection(SEPARATOR_TYPE, ""));
            }

            notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
        }

        public void addDescriptionSection(TextSection textSection) {
            if (descriptionSections.isEmpty()) {
                descriptionSections.add(0, new TextSection(TextSection.SPACER_TYPE, ""));
                descriptionSections.add(new TextSection(SEPARATOR_TYPE, ""));
                notifyDataSetChanged();
            }

            descriptionSections.add(descriptionSections.size() - 1, textSection);
            notifyItemInserted(descriptionSections.indexOf(textSection));
        }

        public ArrayList<TextSection> getDescriptionSections() {
            return descriptionSections;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < descriptionSections.size()) {
                return descriptionSections.get(position).getType();
            }

            return LIST_TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case LIST_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, parent, false);
                    view.setOnClickListener(onClickListener);
                    return new ListItemViewHolder(view);
                case TEXT_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_text_view, parent, false);
                    return new TextViewHolder(view);
                case IMAGE_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_image_view, parent, false);
                    return new ImageViewHolder(view);
                case SEPARATOR_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_members_separator, parent, false);
                    return new SeparatorViewHolder(view);
                case TextSection.SPACER_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spacer_view_holder, parent, false);
                    return new SpacerViewHolder(view);
                default:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, parent, false);
                    return new ListItemViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position < descriptionSections.size()) {
                if (holder.getItemViewType() == TEXT_TYPE) {
                    Spannable span = (Spannable) Html.fromHtml(descriptionSections.get(position).getText().toString().replaceAll("&nbsp", ""), null, new HtmlTagHandler());
                    span = (Spannable) StringUtils.trimTrailingWhitespace(span);
                    ((TextViewHolder) holder).textView.setText(span);
                    ((TextViewHolder) holder).textView.setMovementMethod(new LinkMovementMethod());
                } else if (holder.getItemViewType() == IMAGE_TYPE) {
                    ImageLoader.getInstance().displayImage(descriptionSections.get(position).getText().toString(), ((ImageViewHolder)holder).imageView, options, new AnimateFirstDisplayListener(descriptionSections.get(position).getText().toString(), ((ImageViewHolder)holder).imageView));
                }
                return;
            }

            position -= descriptionSections.size();

            ListItemViewHolder listHolder = (ListItemViewHolder)holder;
            Categorymembers categorymember = categorymembersArrayList.get(position);

            if (categorymember.getRating().isEmpty()) {
                listHolder.titleTextView.setText(categorymember.getTitle());
            } else {
                listHolder.titleTextView.setText(categorymember.getTitle() + " - " + categorymember.getRating());
            }


            if (categorymembersArrayList.get(position).getIsViewed()) {
                listHolder.titleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                listHolder.titleTextView.setTextColor(Color.parseColor("#D9000000"));
            }
        }

        @Override
        public int getItemCount() {
            return categorymembersArrayList.size() + descriptionSections.size();
        }

        public int getDescriptionCount() {
            return descriptionSections.size();
        }

        private class SpacerViewHolder extends RecyclerView.ViewHolder {
            public SpacerViewHolder(View itemView) {
                super(itemView);
            }
        }

        private class ListItemViewHolder extends RecyclerView.ViewHolder {

            public TextView titleTextView;

            public ListItemViewHolder(View itemView) {
                super(itemView);
                titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            }
        }

        private class TextViewHolder extends RecyclerView.ViewHolder {
            protected TextView textView;

            public TextViewHolder(View itemView) {
                super(itemView);
                textView = (TextView)itemView.findViewById(R.id.text_view);
            }
        }

        private class ImageViewHolder extends RecyclerView.ViewHolder {
            protected ImageView imageView;

            public ImageViewHolder(View view) {
                super(view);
                imageView = (ImageView)view.findViewById(R.id.image_view);
            }
        }

        private class SeparatorViewHolder extends RecyclerView.ViewHolder {
            public SeparatorViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
        private final String imageUri;
        private final ImageView imageView;

        AnimateFirstDisplayListener(String imageUri, ImageView imageView) {
            this.imageUri = imageUri;
            this.imageView = imageView;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                } else {
                    imageView.setImageBitmap(loadedImage);
                }
                displayedImages.add(imageUri);
            }
        }
    }
}
