package com.randomname.mrakopedia.ui.allcategories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.randomname.mrakopedia.MainActivity;
import com.randomname.mrakopedia.MrakopediaApplication;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.allcategories.AllCategoriesResult;
import com.randomname.mrakopedia.models.api.allcategories.Allcategories;
import com.randomname.mrakopedia.models.realm.CategoryRealm;
import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.categorymembers.CategoryMembersActivity;
import com.randomname.mrakopedia.ui.categorymembers.CategoryMembersFragment;
import com.randomname.mrakopedia.ui.settings.SettingsWorker;
import com.randomname.mrakopedia.utils.NetworkUtils;
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
    private static final String RESULT_ARRAY_LIST_KEY = "resultArrayListKey";

    @Bind(R.id.all_categories_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.error_text_view)
    carbon.widget.TextView errorTextView;

    private AllCategoriesAdapter adapter;
    private ArrayList<Allcategories> resultArrayList;

    private String continueString = "";
    private boolean isLoading = false;

    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            resultArrayList = savedInstanceState.getParcelableArrayList(RESULT_ARRAY_LIST_KEY);
        } else {
            resultArrayList = new ArrayList<>();
        }

        MrakopediaApplication application = (MrakopediaApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_categories_fragment, null);
        ButterKnife.bind(this, view);

        adapter = new AllCategoriesAdapter(resultArrayList, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v) - 1;
                CategoryMembersFragment fragment = CategoryMembersFragment.getInstance(adapter.getDisplayedData().get(position).getTitle());
                ((MainActivity)getActivity()).addFragment(fragment);
            }
        });

        SettingsWorker settingsWorker = SettingsWorker.getInstance(getActivity());
        if (settingsWorker.isUseSchemeOnAllScreens()) {
            ColorScheme colorScheme = settingsWorker.getCurrentColorScheme();
            view.setBackgroundColor(colorScheme.getBackgroundColor());
            adapter.setColorScheme(colorScheme);
            errorTextView.setTextColor(colorScheme.getTextColor());
        }

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(((MainActivity) getActivity()).toolbarHideRecyclerOnScrollListener);

        if (resultArrayList.isEmpty()) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadCategoryMembersViaNetwork();
                }
            }, 100);

        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(RESULT_ARRAY_LIST_KEY, resultArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConnectedToInternet() {
        if (adapter.getDisplayedData().size() <= 1) {
           loadCategoryMembersViaNetwork();
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.all_categories_drawer);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void loadCategoryMembersViaNetwork() {
        if (continueString == null || isLoading) {
            return;
        }

        isLoading = true;

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

                                isLoading = false;

                                if (adapter.getDisplayedData().size() <= 1) {
                                    loadCategoriesViaRealm();
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.error_loading_categories) + " " + getString(R.string.no_internet_text), Toast.LENGTH_SHORT).show();
                                }
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
                                        adapter.getDisplayedData().add(category);
                                        adapter.notifyItemInserted(adapter.getDisplayedData().indexOf(category) + 1);
                                    }
                                }

                                isLoading = false;

                                loadCategoryMembersViaNetwork();

                                if (recyclerView.getVisibility() != View.VISIBLE) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    errorTextView.setVisibility(View.GONE);
                                }
                            }
                        });


        bindToLifecycle(getAllCategoriesSubscription);
    }

    private void loadCategoriesViaRealm() {

        Subscription getAllCategoriesSubscription =
                DBWorker.getAllCategories()
                .subscribe(new Subscriber<CategoryRealm>() {
                    @Override
                    public void onCompleted() {
                        if (adapter.getDisplayedData().size() <= 1) {
                            errorTextView.setVisibility(View.VISIBLE);

                            if (!NetworkUtils.isInternetAvailable(getActivity())) {
                                errorTextView.setText(getString(R.string.error_loading_categories) + " " + getString(R.string.no_internet_text));
                            }
                            recyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();


                        if (adapter.getDisplayedData().size() <= 1) {
                            errorTextView.setVisibility(View.VISIBLE);

                            if (!NetworkUtils.isInternetAvailable(getActivity())) {
                                errorTextView.setText(getString(R.string.error_loading_categories) + " " + getString(R.string.no_internet_text));
                            }
                            recyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNext(CategoryRealm categoryRealm) {
                        if (categoryRealm.getCategoryMembersTitles().isEmpty()) {
                            return;
                        }

                        Allcategories category = new Allcategories();
                        category.setFiles("");
                        category.setPages(String.valueOf(categoryRealm.getCategoryMembersTitles().size()));
                        category.setTitle(categoryRealm.getTitle());
                        category.setSubcats("");
                        category.setSize("");


                        adapter.getDisplayedData().add(category);
                        adapter.notifyItemInserted(adapter.getDisplayedData().indexOf(category) + 1);
                    }
                });


        bindToLifecycle(getAllCategoriesSubscription);
    }

    private class AllCategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int LIST_ITEM_TYPE = 0;
        private final static int SPACER_ITEM_TYPE = 1;

        private String[] pages = {"страница", "страницы", "страниц"};
        private ColorScheme colorScheme;

        ArrayList<Allcategories> categoriesArrayList;
        View.OnClickListener onClickListener;

        public AllCategoriesAdapter(ArrayList<Allcategories> categoriesArrayList, View.OnClickListener onClickListener) {
            this.categoriesArrayList = categoriesArrayList;
            this.onClickListener = onClickListener;
        }

        public ArrayList<Allcategories> getDisplayedData() {
            return categoriesArrayList;
        }
        public void setColorScheme(ColorScheme colorScheme) {
            this.colorScheme = colorScheme;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return SPACER_ITEM_TYPE;
            } else {
                return LIST_ITEM_TYPE;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == SPACER_ITEM_TYPE) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spacer_view_holder, parent, false);
                return new SpacerViewHolder(view);
            }

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_categories_view_holder, parent, false);

            if (onClickListener != null) {
                view.setOnClickListener(onClickListener);
            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position != 0) {
                Allcategories category = categoriesArrayList.get(position - 1);

                String categorySize = new StringBuilder()
                        .append(category.getPages())
                        .append(" ")
                        .append(StringUtils.declarationOfNum(Integer.parseInt(category.getPages()), pages))
                        .append(" ")
                        .append(getString(R.string.in_this_category))
                        .toString();

                ((ViewHolder) holder).titleTextView.setText(category.getTitle());
                ((ViewHolder)holder).membersCountTextView.setText(categorySize);

                if (colorScheme != null) {
                    ((ViewHolder) holder).titleTextView.setTextColor(colorScheme.getTextColor());
                    ((ViewHolder)holder).membersCountTextView.setTextColor(colorScheme.getTextColor());
                }
            }
        }

        @Override
        public int getItemCount() {
            return categoriesArrayList == null ? 0 : categoriesArrayList.size() + 1;
        }

        private class SpacerViewHolder extends RecyclerView.ViewHolder {
            public SpacerViewHolder(View itemView) {
                super(itemView);
            }
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
