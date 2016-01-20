package com.randomname.mrakopedia.ui.pagesummary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
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

                        if (pageSummaryResult.getParse().getSections().length > 0) {
                            for (Sections section : pageSummaryResult.getParse().getSections()) {
                                if (section.getLine().equals("См. также")) {
                                    Elements lookMore = doc.select("[id^=" + section.getAnchor() + "]");

                                    //TODO delete content below this point
                                }
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

                        pageTextView.setText(Html.fromHtml(text));
                    }
                });
        bindToLifecycle(subscription);

        return view;
    }
}
