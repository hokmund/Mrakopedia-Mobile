package com.randomname.mrakopedia.ui.categorymembers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.allcategories.Allcategories;
import com.randomname.mrakopedia.models.api.categorydescription.CategoryDescription;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.models.api.categorymembers.Categorymembers;
import com.randomname.mrakopedia.models.api.categorymembers.Continue;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryActivity;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.utils.NetworkUtils;
import com.randomname.mrakopedia.utils.StringUtils;
import com.randomname.mrakopedia.utils.Utils;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.ProgressBar;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Vlad on 19.01.2016.
 */
public class CategoryMembersFragment extends RxBaseFragment {
    private static final String TAG = "categoryMembersFragment";
    private static final String CATEGORY_TITLE_KEY = "categoryTitleKey";

    @Bind(R.id.category_members_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.error_text_view)
    carbon.widget.TextView errorTextView;
    @Bind(R.id.loading_progress_bar)
    ProgressBar loadingProgressBar;

    private CategoryMembersAdapter adapter;
    private ArrayList<Categorymembers> categorymembersArrayList;
    private String continueString = "";
    private String categoryTitle;


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

        if (bundle != null) {
            String title = bundle.getString(CATEGORY_TITLE_KEY);
            if (title != null) {
                categoryTitle = title;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_members_fragment, null);
        ButterKnife.bind(this, view);

        categorymembersArrayList = new ArrayList<>();
        adapter = new CategoryMembersAdapter(getActivity(), categorymembersArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                position -= adapter.getDescriptionCount();
                Intent intent = new Intent(getActivity(), PageSummaryActivity.class);
                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, categorymembersArrayList.get(position).getTitle());
                startActivity(intent);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);

        loadCategoryMembers();
        getCategoryDescription();

        return view;
    }

    private void loadCategoryMembers() {
        if (continueString == null) {
            return;
        }
        String continueStringSaved = continueString;
        continueString = null;
        Subscription getCategoryMembersSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getCategoryMembers("Категория:" + categoryTitle, continueStringSaved)
                        .map(new Func1<CategoryMembersResult, CategoryMembersResult>() {
                            @Override
                            public CategoryMembersResult call(CategoryMembersResult categoryMembersResult) {
                                ArrayList<Categorymembers> categoryMembers = new ArrayList<>(Arrays.asList(categoryMembersResult.getQuery().getCategorymembers()));

                                for (Iterator<Categorymembers> iterator = categoryMembers.iterator(); iterator.hasNext();) {
                                    Categorymembers categoryMember = iterator.next();

                                    if (categoryMember.getType().equals("subcat")) {
                                        iterator.remove();
                                        continue;
                                    }

                                    categoryMember.setIsViewed(DBWorker.getPageIsRead(categoryMember.getTitle()));
                                }
                                categoryMembersResult.getQuery().setCategorymembers(categoryMembers.toArray(new Categorymembers[categoryMembers.size()]));
                                return categoryMembersResult;
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<CategoryMembersResult>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(CategoryMembersResult categoryMembersResult) {
                                if (categoryMembersResult.getmContinue() != null) {
                                    continueString = categoryMembersResult.getmContinue().getCmcontinue();
                                } else {
                                    continueString = null;
                                }


                                for (Categorymembers category : categoryMembersResult.getQuery().getCategorymembers()) {
                                    boolean toSkip = false;

                                    for (String banString : Utils.pagesBanList) {
                                        if (category.getTitle().contains(banString)) {
                                            toSkip = true;
                                            break;
                                        }
                                    }

                                    if (!toSkip) {
                                        categorymembersArrayList.add(category);
                                        adapter.notifyItemInserted(categorymembersArrayList.indexOf(category));
                                    }
                                }
                                recyclerView.scrollToPosition(0);
                                loadCategoryMembers();
                            }
                        });
        bindToLifecycle(getCategoryMembersSubscription);
    }

    private void getCategoryDescription() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        Subscription getCategoryDescriptionSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getCategoryDescription(categoryTitle)
                        .map(new Func1<CategoryDescription, CategoryDescription>() {
                            @Override
                            public CategoryDescription call(CategoryDescription categoryDescription) {
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
                                        for (Element liTag: liTags) {
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
                                errorTextView.setVisibility(View.VISIBLE);

                                if (!NetworkUtils.isInternetAvailable(getActivity())) {
                                    errorTextView.setText(getString(R.string.error_loading_category) + " " + getString(R.string.no_internet_text));
                                }

                                loadingProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onNext(CategoryDescription categoryDescription) {
                                adapter.setDescriptionSections(categoryDescription.getTextSections());
                                recyclerView.setVisibility(View.VISIBLE);

                                AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                                animation.setDuration(300);

                                recyclerView.setAnimation(animation);
                                recyclerView.animate();

                                loadingProgressBar.setVisibility(View.GONE);
                            }
                        });

        bindToLifecycle(getCategoryDescriptionSubscription);
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
        private static final int SEPARATOR_TYPE = 5;

        Context context;

        ArrayList<Categorymembers> categorymembersArrayList;
        View.OnClickListener onClickListener;
        ArrayList<TextSection> descriptionSections;

        public CategoryMembersAdapter(Context context, ArrayList<Categorymembers> categorymembers, View.OnClickListener onClickListener) {
            this.context = context;
            categorymembersArrayList = categorymembers;
            this.onClickListener = onClickListener;
            descriptionSections = new ArrayList<>();
        }

        public void setDescriptionSections(ArrayList<TextSection> sections) {
            descriptionSections = sections;

            if (!descriptionSections.isEmpty()) {
                descriptionSections.add(new TextSection(SEPARATOR_TYPE, ""));
            }

            notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
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
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_text_view, parent, false);
                    return new TextViewHolder(view);
                case IMAGE_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_image_view, parent, false);
                    return new ImageViewHolder(view);
                case SEPARATOR_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_members_separator, parent, false);
                    return new SeparatorViewHolder(view);
                default:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, parent, false);
                    return new ListItemViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position < descriptionSections.size()) {
                if (holder.getItemViewType() == TEXT_TYPE) {
                    Spannable span = (Spannable) Html.fromHtml(descriptionSections.get(position).getText().replaceAll("&nbsp", ""), null, new HtmlTagHandler());
                    span = (Spannable) StringUtils.trimTrailingWhitespace(span);
                    ((TextViewHolder) holder).textView.setText(span);
                    ((TextViewHolder) holder).textView.setMovementMethod(new LinkMovementMethod());
                } else if (holder.getItemViewType() == IMAGE_TYPE) {
                    Picasso.with(context)
                            .load(descriptionSections.get(position).getText())
                            .into(((ImageViewHolder) holder).imageView);
                }
                return;
            }

            position -= descriptionSections.size();

            ((ListItemViewHolder)holder).titleTextView.setText(categorymembersArrayList.get(position).getTitle());

            if (categorymembersArrayList.get(position).getIsViewed()) {
                ((ListItemViewHolder)holder).titleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                ((ListItemViewHolder)holder).titleTextView.setTextColor(Color.parseColor("#D9000000"));
            }
        }

        @Override
        public int getItemCount() {
            return categorymembersArrayList.size() + descriptionSections.size();
        }

        public int getDescriptionCount() {
            return descriptionSections.size();
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
}
