package com.randomname.mrakopedia.ui.pagesummary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.Sections;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.ui.views.UILImageGetter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    @Bind(R.id.page_text_text_view)
    TextView pageTextView;

    private String pageTitle;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_summary_fragment, null);
        ButterKnife.bind(this, view);

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
                                if (!aTag.children().isEmpty()) {
                                    for (Element aTagChildren : aTag.children()) {
                                        if (aTagChildren.tagName().equals("img")) {
                                        }
                                    }
                                }

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

                        for (Element element : doc.select("*")) {
                            if (!element.hasText() && element.isBlock()) {
                                element.remove();
                            }
                        }

                        pageSummaryResult.getParse().getText().setText(doc.html());

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
                        String text = pageSummaryResult.getParse().getText().getText();
                        Spannable spanned = (Spannable) Html.fromHtml(text, new UILImageGetter(pageTextView, getActivity(), "https://mrakopedia.ru"), new HtmlTagHandler());
                        spanned = addClickToImageSpans(spanned);


                        pageTextView.setText(spanned);
                        pageTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                });
        bindToLifecycle(subscription);

        return view;
    }

    public Spannable addClickToImageSpans(Spannable s) {
        ImageSpan[] image_spans = s.getSpans(0, s.length(), ImageSpan.class);

        for (ImageSpan span : image_spans) {

            final String image_src = span.getSource();
            final int start = s.getSpanStart(span);
            final int end = s.getSpanEnd(span);

            ClickableSpan click_span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Log.e(TAG, image_src);
                }
            };
            ClickableSpan[] click_spans = s.getSpans(start, end, ClickableSpan.class);

            if (click_spans.length != 0) {
                for (ClickableSpan c_span : click_spans) {
                    s.removeSpan(c_span);
                }


            }

            s.setSpan(click_span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return s;
    }
}
