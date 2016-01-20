package com.randomname.mrakopedia.ui.categorymembers;

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
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Vlad on 19.01.2016.
 */
public class CategoryMembersFragment extends RxBaseFragment {
    private static final String TAG = "categoryMembersFragment";

    @Bind(R.id.category_members_recycler_view)
    RecyclerView recyclerView;

    private CategoryMembersAdapter adapter;
    private ArrayList<Categorymembers> categorymembersArrayList;
    private String continueString = "";


    public CategoryMembersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_members_fragment, null);
        ButterKnife.bind(this, view);

        categorymembersArrayList = new ArrayList<>();
        adapter = new CategoryMembersAdapter(categorymembersArrayList);

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
                        .getCategoryMembers("Категория:Без_мистики", continueString)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<CategoryMembersResult>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                            }

                            @Override
                            public void onNext(CategoryMembersResult categoryMembersResult) {
                                if (categoryMembersResult.getmContinue().getCmcontinue() != null) {
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

        public CategoryMembersAdapter(ArrayList<Categorymembers> categorymembers) {
            categorymembersArrayList = categorymembers;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.titleTextView.setText(categorymembersArrayList.get(position).getTitle());
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
