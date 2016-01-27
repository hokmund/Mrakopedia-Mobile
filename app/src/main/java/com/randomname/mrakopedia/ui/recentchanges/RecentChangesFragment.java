package com.randomname.mrakopedia.ui.recentchanges;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.categorymembers.Categorymembers;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.api.recentchanges.RecentChangesResult;
import com.randomname.mrakopedia.models.api.recentchanges.Recentchanges;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.utils.StringUtils;
import com.randomname.mrakopedia.utils.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Vlad on 27.01.2016.
 */
public class RecentChangesFragment extends RxBaseFragment {

    private static final String TAG = "Recent changes Fragment";

    private String continueString = "";
    private ArrayList<Recentchanges> recentChangesArrayList;
    private RecentChangesAdapter adapter;

    @Bind(R.id.recent_changes_recycler_view)
    RecyclerView recyclerView;

    public RecentChangesFragment () {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recentChangesArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recent_changes_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new RecentChangesAdapter(recentChangesArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(manager) {
            @Override
            public void onLoadMore(int current_page) {
                getRecentChanges();
            }
        });

        getRecentChanges();
        return view;
    }

    private void getRecentChanges() {
        if (continueString == null) {
            return;
        }
        Log.e(TAG, "start downloading");

        String savedString = continueString;
        continueString = null;

        Subscription subscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getRecentChanges(savedString)
                        .doOnNext(new Action1<RecentChangesResult>() {
                            @Override
                            public void call(RecentChangesResult recentChangesResult) {
                                if (recentChangesResult.getmContinue() != null) {
                                    continueString = recentChangesResult.getmContinue().getRccontinue();
                                } else {
                                    continueString = null;
                                }
                            }
                        })
                        .flatMap(new Func1<RecentChangesResult, Observable<Recentchanges>>() {
                            @Override
                            public Observable<Recentchanges> call(RecentChangesResult recentChangesResult) {
                                return Observable.from(recentChangesResult.getQuery().getRecentchanges());
                            }
                        })
                        .filter(new Func1<Recentchanges, Boolean>() {
                            @Override
                            public Boolean call(Recentchanges recentchanges) {
                                for (String banString : Utils.pagesBanList) {
                                    if (recentchanges.getTitle().toLowerCase().contains(banString.toLowerCase())) {
                                        return false;
                                    }
                                }
                                return true;
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Recentchanges>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.getMessage());
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Recentchanges recentchanges) {
                                recentChangesArrayList.add(recentchanges);
                                adapter.notifyItemInserted(adapter.getDisplayedData().indexOf(recentchanges));

                            }
                        });

        bindToLifecycle(subscription);
    }

    private class RecentChangesAdapter extends RecyclerView.Adapter<RecentChangesAdapter.ListItemViewHolder> {
        private ArrayList<Recentchanges> recentChangesArrayList;

        private View.OnClickListener onClickListener;

        public RecentChangesAdapter(ArrayList<Recentchanges> recentChangesArrayList, View.OnClickListener onClickListener) {
            this.recentChangesArrayList = recentChangesArrayList;
            this.onClickListener = onClickListener;
        }

        public ArrayList<Recentchanges> getDisplayedData() {
            return recentChangesArrayList;
        }

        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, parent, false);
            view.setOnClickListener(onClickListener);
            return new ListItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int position) {
            holder.titleTextView.setText(recentChangesArrayList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return recentChangesArrayList == null ? 0 : recentChangesArrayList.size();
        }

        protected class ListItemViewHolder extends RecyclerView.ViewHolder {

            public TextView titleTextView;

            public ListItemViewHolder(View itemView) {
                super(itemView);
                titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            }
        }
    }
}
