package com.randomname.mrakopedia.ui.categorymembers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.api.MrakopediaApiWorker;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.ui.RxBaseFragment;

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

    public CategoryMembersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_members_fragment, null);
        ButterKnife.bind(this, view);

        Subscription getCategoryMembersSubscription =
                MrakopediaApiWorker
                        .getInstance()
                        .getCategoryMembers("Категория:Без_мистики")
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
                            }
                        });
        bindToLifecycle(getCategoryMembersSubscription);

        return view;
    }
}
