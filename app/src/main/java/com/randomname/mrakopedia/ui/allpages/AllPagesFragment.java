package com.randomname.mrakopedia.ui.allpages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.allpages.AllPagesResult;
import com.randomname.mrakopedia.models.api.allpages.Allpages;
import com.randomname.mrakopedia.models.api.categorymembers.Categorymembers;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryActivity;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.utils.StringUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.TextView;
import io.realm.RealmObject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by vgrigoryev on 27.01.2016.
 */
public class AllPagesFragment extends RxBaseFragment {
    private static final String TAG = "All Pages Fragment";
    private static final int PAGE_SUMMARY_ACTIVITY_CODE = 11;

    @Bind(R.id.all_pages_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.error_text_view)
    TextView errorTextView;

    private String continueString = "";

    private AllPagesAdapter adapter;
    private ArrayList<Allpages> allPagesArrayList;

    public AllPagesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allPagesArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_pages_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new AllPagesAdapter(getActivity(), allPagesArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);

                Intent intent = new Intent(getActivity(), PageSummaryActivity.class);
                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, adapter.getDisplayedData().get(position).getTitle());

                startActivityForResult(intent, PAGE_SUMMARY_ACTIVITY_CODE);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        getAllPagesByNetwork();

        return view;
    }

    private void getAllPagesByNetwork() {
        if (continueString == null) {
            return;
        }

        Subscription subscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getAllPages(continueString)
                        .doOnNext(new Action1<AllPagesResult>() {
                            @Override
                            public void call(AllPagesResult allPagesResult) {
                                if (allPagesResult.getContinue() != null) {
                                    continueString = allPagesResult.getContinue().getApcontinue();
                                } else {
                                    continueString = null;
                                }
                            }
                        })
                        .flatMap(new Func1<AllPagesResult, Observable<Allpages>>() {
                            @Override
                            public Observable<Allpages> call(AllPagesResult allPagesResult) {
                                return Observable.from(allPagesResult.getQuery().getAllpages());
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Allpages>() {
                            @Override
                            public void onCompleted() {
                                getAllPagesByNetwork();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.getMessage());
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Allpages allpages) {
                                allPagesArrayList.add(allpages);
                                adapter.notifyItemInserted(allPagesArrayList.indexOf(allpages));
                                markPageAsRead(allpages);
                            }
                        });

        bindToLifecycle(subscription);
    }

    private void markPageAsRead(final Allpages allpages) {

    }

    private class AllPagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private String filter = "";

        private ArrayList<Allpages> allPagesArrayList;
        private ArrayList<Allpages> filteredArray;

        private View.OnClickListener onClickListener;

        public AllPagesAdapter(Context context, ArrayList<Allpages> allPages, View.OnClickListener onClickListener) {
            this.context = context;
            this.allPagesArrayList = allPages;
            this.onClickListener = onClickListener;
            filteredArray = new ArrayList<>();
        }

        public ArrayList<Allpages> getDisplayedData() {
            return allPagesArrayList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, parent, false);
            view.setOnClickListener(onClickListener);
            return new ListItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ((ListItemViewHolder)holder).titleTextView.setText(allPagesArrayList.get(position).getTitle());

            if (allPagesArrayList.get(position).isViewed()) {
                ((ListItemViewHolder)holder).titleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                ((ListItemViewHolder)holder).titleTextView.setTextColor(Color.parseColor("#D9000000"));
            }
        }

        @Override
        public int getItemCount() {
            return allPagesArrayList.size();
        }

        private class ListItemViewHolder extends RecyclerView.ViewHolder {

            public android.widget.TextView titleTextView;

            public ListItemViewHolder(View itemView) {
                super(itemView);
                titleTextView = (android.widget.TextView) itemView.findViewById(R.id.title_text_view);
            }
        }
    }
}
