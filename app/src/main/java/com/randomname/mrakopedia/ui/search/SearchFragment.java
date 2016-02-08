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

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.search.Search;
import com.randomname.mrakopedia.models.api.search.SearchResult;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryActivity;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;

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
public class SearchFragment extends RxBaseFragment {

    private static final String TAG = "SearchFragment";

    private String continueString = null;

    @Bind(R.id.search_edit_text)
    EditText searchEditText;
    @Bind(R.id.search_recycler_view)
    RecyclerView searchResultsRecyclerView;
    @Bind(R.id.cancel_search_btn)
    ImageButton cancelImageBtn;

    ArrayList<Search> searchResultArrayList;
    SearchResultsAdapter adapter;

    Subscription subscription;
    Subscription continueSearchSubscription;

    private EndlessRecyclerOnScrollListener endlessListener;

    public SearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchResultArrayList = new ArrayList<>();
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

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
                endlessListener.clearListener();
                if (continueSearchSubscription != null) {
                    continueSearchSubscription.unsubscribe();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cancelImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
                searchResultArrayList.clear();
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onConnectedToInternet() {
    }

    private void search(String searchString) {
        searchResultArrayList.clear();
        adapter.notifyDataSetChanged();

        if (subscription != null) {
            subscription.unsubscribe();
        }

        subscription = MrakopediaApiWorker
                .getInstance()
                .search(searchString, "")
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

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Search search) {
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
                .search(searchEditText.getText().toString(), continueString)
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
                        Log.e(TAG, e.getMessage());
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
