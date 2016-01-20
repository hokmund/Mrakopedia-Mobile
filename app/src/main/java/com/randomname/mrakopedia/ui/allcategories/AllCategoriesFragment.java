package com.randomname.mrakopedia.ui.allcategories;

import android.os.Bundle;
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
import com.randomname.mrakopedia.models.api.allcategories.AllCategoriesResult;
import com.randomname.mrakopedia.models.api.allcategories.Allcategories;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.models.api.categorymembers.Categorymembers;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class AllCategoriesFragment extends RxBaseFragment {
    private static final String TAG = "AllCategoriesFragment";

    @Bind(R.id.all_categories_recycler_view)
    RecyclerView recyclerView;

    private AllCategoriesAdapter adapter;
    private ArrayList<Allcategories> resultArrayList;

    private String continueString = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_categories_fragment, null);
        ButterKnife.bind(this, view);

        resultArrayList = new ArrayList<>();
        adapter = new AllCategoriesAdapter(resultArrayList);

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

        Subscription getAllCategoriesSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getAllCategories(continueString)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<AllCategoriesResult>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                            }

                            @Override
                            public void onNext(AllCategoriesResult allCategoriesResult) {
                                if (allCategoriesResult.getAccContinue() != null) {
                                    continueString = allCategoriesResult.getAccContinue().getAccontinue();
                                } else {
                                    continueString = null;
                                }

                                for (Allcategories category : allCategoriesResult.getQuery().getAllcategories()) {
                                    resultArrayList.add(category);
                                    adapter.notifyItemInserted(resultArrayList.indexOf(category));
                                }
                            }
                        });


        bindToLifecycle(getAllCategoriesSubscription);
    }

    private class AllCategoriesAdapter extends RecyclerView.Adapter<AllCategoriesAdapter.ViewHolder> {

        ArrayList<Allcategories> categorymembersArrayList;

        public AllCategoriesAdapter(ArrayList<Allcategories> categorymembers) {
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
