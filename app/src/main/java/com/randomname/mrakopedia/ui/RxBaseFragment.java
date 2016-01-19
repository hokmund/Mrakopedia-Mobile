package com.randomname.mrakopedia.ui;

import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.ArrayList;

import rx.Subscription;

/**
 * Created by Vlad on 19.01.2016.
 */
public abstract class RxBaseFragment extends Fragment {
    private static final String TAG = "RxBaseFragment";

    private ArrayList<Subscription> subscriptions = new ArrayList<>();

    protected void bindToLifecycle(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView");
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
    }
}
