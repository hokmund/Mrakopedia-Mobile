package com.randomname.mrakopedia.ui.allcategories;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.allcategories.AllCategoriesResult;
import com.randomname.mrakopedia.models.api.allcategories.Allcategories;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.categorymembers.CategoryMembersActivity;
import com.randomname.mrakopedia.ui.views.EndlessRecyclerOnScrollListener;
import com.randomname.mrakopedia.utils.StringUtils;
import com.randomname.mrakopedia.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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
        adapter = new AllCategoriesAdapter(resultArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                Intent intent = new Intent(getActivity(), CategoryMembersActivity.class);
                intent.putExtra(CategoryMembersActivity.CATEGORY_NAME_EXTRA, resultArrayList.get(position).getTitle());
                startActivity(intent);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);

        loadCategoryMembersViaNetwork();

        return view;
    }

    private void loadCategoryMembersViaNetwork() {
        if (continueString == null) {
            return;
        }

        Subscription getAllCategoriesSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getAllCategories(continueString)
                        .map(new Func1<AllCategoriesResult, AllCategoriesResult>() {
                            @Override
                            public AllCategoriesResult call(AllCategoriesResult allCategoriesResult) {
                                ArrayList<Allcategories> allCategories = new ArrayList<>(Arrays.asList(allCategoriesResult.getQuery().getAllcategories()));

                                for (Iterator<Allcategories> iterator = allCategories.iterator(); iterator.hasNext();) {
                                    Allcategories category = iterator.next();
                                }
                                allCategoriesResult.getQuery().setAllcategories(allCategories.toArray(new Allcategories[allCategories.size()]));
                                return allCategoriesResult;
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<AllCategoriesResult>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(AllCategoriesResult allCategoriesResult) {
                                if (allCategoriesResult.getAccContinue() != null) {
                                    continueString = allCategoriesResult.getAccContinue().getAccontinue();
                                } else {
                                    continueString = null;
                                }

                                for (Allcategories category : allCategoriesResult.getQuery().getAllcategories()) {
                                    boolean toSkip = false;

                                    for (String banString : Utils.categoriesBanList) {
                                        if (category.getTitle().contains(banString)) {
                                            toSkip = true;
                                            break;
                                        }
                                    }

                                    if (!toSkip) {
                                        resultArrayList.add(category);
                                        adapter.notifyItemInserted(resultArrayList.indexOf(category));
                                    }
                                }

                                loadCategoryMembersViaNetwork();
                            }
                        });


        bindToLifecycle(getAllCategoriesSubscription);
    }

    private class AllCategoriesAdapter extends RecyclerView.Adapter<AllCategoriesAdapter.ViewHolder> {

        ArrayList<Allcategories> categoriesArrayList;
        View.OnClickListener onClickListener;

        public AllCategoriesAdapter(ArrayList<Allcategories> categoriesArrayList, View.OnClickListener onClickListener) {
            this.categoriesArrayList = categoriesArrayList;
            this.onClickListener = onClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_categories_view_holder, parent, false);

            if (onClickListener != null) {
                view.setOnClickListener(onClickListener);
            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Allcategories category = categoriesArrayList.get(position);
            String[] pages = {"страница", "страницы", "страниц"};

            holder.titleTextView.setText(category.getTitle());
            holder.membersCountTextView.setText(category.getPages()
                    + " " + StringUtils.declarationOfNum(Integer.parseInt(category.getPages()), pages)
                    + " " + getString(R.string.in_this_category));
        }

        @Override
        public int getItemCount() {
            return categoriesArrayList == null ? 0 : categoriesArrayList.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {

            public TextView titleTextView, membersCountTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
                membersCountTextView = (TextView) itemView.findViewById(R.id.members_count_text_view);
            }
        }
    }
}
