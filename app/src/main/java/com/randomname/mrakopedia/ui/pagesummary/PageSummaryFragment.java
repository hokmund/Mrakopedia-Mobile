package com.randomname.mrakopedia.ui.pagesummary;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.Sections;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Vlad on 20.01.2016.
 */
public class PageSummaryFragment extends RxBaseFragment {

    private static final String TAG = "PageSummaryFragment";
    private static final String PAGE_TITLE_KEY = "pageTitleKey";

    private String pageTitle;

    @Bind(R.id.page_summary_recycler_view)
    RecyclerView recyclerView;

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

        textSections = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_summary_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new PageSummaryAdapter(textSections, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


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

                        Elements scriptsTags = doc.select("script");

                        if (!scriptsTags.isEmpty()) {
                            scriptsTags.remove();
                        }

                        Elements spoilerLinks = doc.select("a.spoilerLink");
                        if (!spoilerLinks.isEmpty()) {
                            spoilerLinks.remove();
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
                                imgTag.attr("iter_key", String.valueOf(index++));
                            }
                        }

                        pageSummaryResult.getParse().getText().setText(doc.html());

                        return pageSummaryResult;
                    }
                })
                .map(new Func1<PageSummaryResult, PageSummaryResult>() {
                    @Override
                    public PageSummaryResult call(PageSummaryResult pageSummaryResult) {

                        Document doc = Jsoup.parse(pageSummaryResult.getParse().getText().getText());
                        doc.setBaseUri("https://mrakopedia.ru");

                        String stringToSplit = pageSummaryResult.getParse().getText().getText();

                        Elements imgTags = doc.select("img");

                        if (!imgTags.isEmpty()) {
                            for (Element imgTag : imgTags) {
                                String[] splited = stringToSplit.split(imgTag.outerHtml());
                                stringToSplit = splited[1];
                                pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, splited[0]));
                                pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.IMAGE_TYPE, imgTag.absUrl("src")));
                            }
                            pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, stringToSplit));
                        } else {
                            pageSummaryResult.getParse().getTextSections().add(new TextSection(TextSection.TEXT_TYPE, pageSummaryResult.getParse().getText().getText()));
                        }


                        return pageSummaryResult;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PageSummaryResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(PageSummaryResult pageSummaryResult) {
                        for (TextSection section : pageSummaryResult.getParse().getTextSections()) {
                            textSections.add(section);
                            adapter.notifyItemInserted(textSections.indexOf(section));
                        }
                    }
                });
        bindToLifecycle(subscription);

        return view;
    }

    private class PageSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<TextSection> sections;
        private Context context;

        public PageSummaryAdapter(ArrayList<TextSection> sections, Context context) {
            this.sections = sections;
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            return sections.get(position).getType();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case TextSection.TEXT_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_text_view, parent, false);
                    return new TextViewHolder(view);
                case TextSection.IMAGE_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_image_view, parent, false);
                    return new ImageViewHolder(view);
                default:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_text_view, parent, false);
                    return new TextViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case TextSection.TEXT_TYPE:
                    Spannable span = (Spannable) Html.fromHtml(sections.get(position).getText(), null, new HtmlTagHandler());
                    span = (Spannable) trimTrailingWhitespace(span);
                            ((TextViewHolder) holder).textView.setText(span);
                    break;
                case TextSection.IMAGE_TYPE:
                    Picasso.with(context).load(sections.get(position).getText()).into(((ImageViewHolder)holder).imageView);
                    break;
                default:
            }
        }

        @Override
        public int getItemCount() {
            return sections == null ? 0 : sections.size();
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
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }
}
