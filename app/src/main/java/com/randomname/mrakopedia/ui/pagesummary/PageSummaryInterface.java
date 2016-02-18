package com.randomname.mrakopedia.ui.pagesummary;

import android.support.v7.widget.RecyclerView;

/**
 * Created by vgrigoryev on 18.02.2016.
 */
public interface PageSummaryInterface {
    public void startSelection();
    public void stopSelection();
    public RecyclerView.OnScrollListener getToolbarHideListener();
}
