package com.randomname.mrakopedia.ui;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.randomname.mrakopedia.api.NetworkChangeReceiver;

import java.util.ArrayList;

import rx.Subscription;

/**
 * Created by Vlad on 19.01.2016.
 */
public abstract class RxBaseFragment extends Fragment {
    private static final String TAG = "RxBaseFragment";

    protected NetworkChangeReceiver networkChangeReceiver;

    private ArrayList<Subscription> subscriptions = new ArrayList<>();

    protected void bindToLifecycle(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkChangeReceiver = new NetworkChangeReceiver() {
            @Override
            public void onConnected() {
                onConnectedToInternet();
            }
        };

        getActivity().registerReceiver(networkChangeReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public abstract void onConnectedToInternet();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView");
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }

        getActivity().unregisterReceiver(networkChangeReceiver);
    }
}
