package com.randomname.mrakopedia.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.randomname.mrakopedia.MainActivity;
import com.randomname.mrakopedia.MrakopediaApplication;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.search.Search;
import com.randomname.mrakopedia.models.api.search.SearchResult;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryActivity;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;
import com.randomname.mrakopedia.ui.views.selection.SelectableTextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.TextView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by vgrigoryev on 08.02.2016.
 */
public class SearchFragment extends RxBaseFragment implements SearchCallback {

    private static final String TAG = "SearchFragment";
    private static final String CONTINUES_STRING_KEY = "continueStringKey";
    private static final String SEARCH_STRING_KEY = "searchStringKey";
    private static final String SEARCH_RESULT_LIST_KEY = "searchResultKey";

    private String continueString = null;
    private String searchString = "";

    @Bind(R.id.search_recycler_view)
    RecyclerView searchResultsRecyclerView;
    @Bind(R.id.nothing_found_text_view)
    SelectableTextView nothingFoundTextView;

    ArrayList<Search> searchResultArrayList;
    SearchResultsAdapter adapter;

    Subscription subscription;
    Subscription continueSearchSubscription;

    private EndlessRecyclerOnScrollListener endlessListener;

    private Tracker mTracker;

    public SearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            searchResultArrayList = savedInstanceState.getParcelableArrayList(SEARCH_RESULT_LIST_KEY);
            continueString = savedInstanceState.getString(CONTINUES_STRING_KEY);
            searchString = savedInstanceState.getString(searchString);
        } else {
            searchResultArrayList = new ArrayList<>();
        }

        ((MainActivity)getActivity()).registerForSearchListener(this);

        MrakopediaApplication application = (MrakopediaApplication)getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new SearchResultsAdapter(searchResultArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = searchResultsRecyclerView.getChildAdapterPosition(v);

                Intent intent = new Intent(getActivity(), PageSummaryActivity.class);
                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, searchResultArrayList.get(position).getTitle());
                startActivity(intent);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        endlessListener = new EndlessRecyclerOnScrollListener(manager) {
            @Override
            public void onLoadMore(int current_page) {
                continueSearch();
            }
        };

        searchResultsRecyclerView.setAdapter(adapter);
        searchResultsRecyclerView.setLayoutManager(manager);
        searchResultsRecyclerView.addOnScrollListener(endlessListener);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onConnectedToInternet() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CONTINUES_STRING_KEY, continueString);
        outState.putString(SEARCH_STRING_KEY, searchString);
        outState.putParcelableArrayList(SEARCH_RESULT_LIST_KEY, searchResultArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity)getActivity()).unregisterForSearchListener(this);
    }

    private void search(String searchString) {
        searchResultArrayList.clear();
        adapter.notifyDataSetChanged();

        if (subscription != null) {
            subscription.unsubscribe();
        }

        subscription = MrakopediaApiWorker
                .getInstance()
                .searchInText(searchString, "")
                .doOnNext(new Action1<SearchResult>() {
                    @Override
                    public void call(SearchResult searchResult) {
                        if (searchResult.getSearchContinue() != null && searchResult.getSearchContinue().getSroffset() != null) {
                            continueString = searchResult.getSearchContinue().getSroffset();
                        } else {
                            continueString = null;
                        }
                    }
                })
                .mergeWith(MrakopediaApiWorker.getInstance().searchInTitle(searchString, ""))
                .flatMap(new Func1<SearchResult, Observable<Search>>() {
                    @Override
                    public Observable<Search> call(SearchResult searchResult) {
                        if (searchResult.getQuery() == null || searchResult.getQuery().getSearch() == null) {
                            return Observable.empty();
                        }

                        return Observable.from(searchResult.getQuery().getSearch());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Search>() {
                    @Override
                    public void onCompleted() {
                        if (searchResultArrayList.isEmpty()) {
                            nothingFoundTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Search search) {
                        nothingFoundTextView.setVisibility(View.GONE);
                        searchResultArrayList.add(search);
                        adapter.notifyItemInserted(searchResultArrayList.indexOf(search));
                    }
                });

        bindToLifecycle(subscription);
    }

    private void continueSearch() {

        if (continueString == null) {
            return;
        }

        continueSearchSubscription = MrakopediaApiWorker
                .getInstance()
                .searchInText(searchString, continueString)
                .doOnNext(new Action1<SearchResult>() {
                    @Override
                    public void call(SearchResult searchResult) {
                        if (searchResult.getSearchContinue() != null && searchResult.getSearchContinue().getSroffset() != null) {
                            continueString = searchResult.getSearchContinue().getSroffset();
                        } else {
                            continueString = null;
                        }
                    }
                })
                .flatMap(new Func1<SearchResult, Observable<Search>>() {
                    @Override
                    public Observable<Search> call(SearchResult searchResult) {
                        return Observable.from(searchResult.getQuery().getSearch());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Search>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Search search) {
                        searchResultArrayList.add(search);
                        adapter.notifyItemInserted(searchResultArrayList.indexOf(search));
                    }
                });

        bindToLifecycle(continueSearchSubscription);
    }

    @Override
    public void onSearchChanged(String search) {
        search(search);
        endlessListener.clearListener();
        if (continueSearchSubscription != null) {
            continueSearchSubscription.unsubscribe();
        }
        searchString = search;

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Search")
                .setLabel(searchString)
                .build());
    }

    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder> {

        private ArrayList<Search> searchArrayList;
        private View.OnClickListener onClickListener;

        public SearchResultsAdapter(ArrayList<Search> searchArrayList, View.OnClickListener onClickListener) {
            this.searchArrayList = searchArrayList;
            this.onClickListener = onClickListener;
        }

        @Override
        public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_view_holder, parent, false);
            view.setOnClickListener(onClickListener);
            return new SearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SearchResultViewHolder holder, int position) {
            Search search = searchArrayList.get(position);
            holder.titleTextView.setText(search.getTitle());
        }

        @Override
        public int getItemCount() {
           return searchArrayList == null ? 0 : searchArrayList.size();
        }

        protected class SearchResultViewHolder extends RecyclerView.ViewHolder {

            public TextView titleTextView;

            public SearchResultViewHolder(View itemView) {
                super(itemView);
                titleTextView = (TextView)itemView.findViewById(R.id.title_text_view);
            }
        }
    }
}
