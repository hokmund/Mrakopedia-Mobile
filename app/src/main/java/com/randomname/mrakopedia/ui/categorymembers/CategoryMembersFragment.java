package com.randomname.mrakopedia.ui.categorymembers;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.models.api.categorymembers.Categorymembers;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryActivity;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
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
        adapter = new CategoryMembersAdapter(categorymembersArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                Intent intent = new Intent(getActivity(), PageSummaryActivity.class);
                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, categorymembersArrayList.get(position).getTitle());
                startActivity(intent);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(manager) {
            @Override
            public void onLoadMore(int current_page) {
                loadCategoryMembers();
            }
        });

        loadCategoryMembers();

        return view;
    }

    private void loadCategoryMembers() {
        if (continueString == null) {
            return;
        }

        Subscription getCategoryMembersSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getCategoryMembers("Категория:" + categoryTitle, continueString)
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

                                    categoryMember.setIsViewed(DBWorker.isPageSummarySaved(categoryMember.getTitle()));
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

                                for (Categorymembers categorymember : categoryMembersResult.getQuery().getCategorymembers()) {
                                    categorymembersArrayList.add(categorymember);
                                    adapter.notifyItemInserted(categorymembersArrayList.size());
                                }
                            }
                        });
        bindToLifecycle(getCategoryMembersSubscription);
    }

    private class CategoryMembersAdapter extends RecyclerView.Adapter<CategoryMembersAdapter.ViewHolder> {

        ArrayList<Categorymembers> categorymembersArrayList;
        View.OnClickListener onClickListener;

        public CategoryMembersAdapter(ArrayList<Categorymembers> categorymembers, View.OnClickListener onClickListener) {
            categorymembersArrayList = categorymembers;
            this.onClickListener = onClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, parent, false);

            view.setOnClickListener(onClickListener);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.titleTextView.setText(categorymembersArrayList.get(position).getTitle());

            if (categorymembersArrayList.get(position).getIsViewed()) {
                holder.titleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                holder.titleTextView.setTextColor(Color.parseColor("#D9000000"));
            }
        }

        @Override
        public int getItemCount() {
            return categorymembersArrayList.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {

            public TextView titleTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            }
        }
    }
}
