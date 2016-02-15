package com.randomname.mrakopedia.ui.views;

/**
 * Created by vgrigoryev on 15.02.2016.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.util.Util;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.utils.Utils;

public class StickySummaryDecoration extends RecyclerView.ItemDecoration {
    Context context;

    public StickySummaryDecoration(Context context) {
        this.context = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        int childCount = parent.getAdapter().getItemCount();

        if (parent.getChildViewHolder(view).getItemViewType() == TextSection.CATEGORY_TYPE) {
            int extraTop = parent.getHeight();

            for (int i = 0; i < childCount; i++) {
                View childView = parent.getChildAt(i);

                if (childView != null) {
                    extraTop -= childView.getHeight();
                }
            }

            extraTop -= Utils.convertDpToPixel(75.0f, context);

            if (extraTop > 0) {
                outRect.top = extraTop;
            }
        }
    }
}
