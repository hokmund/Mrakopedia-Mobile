package com.randomname.mrakopedia.ui.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Vlad on 30.01.2016.
 */
public class ToolbarHideRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private int verticalOffset;
    // Determines the scroll UP/DOWN direction
    private boolean scrollingUp;
    private View viewToScroll;
    private boolean isHidden;

    public ToolbarHideRecyclerOnScrollListener(View viewToScroll) {
        this.viewToScroll = viewToScroll;
    }

    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrollingUp) {
                if (verticalOffset > viewToScroll.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
            } else {
                if (viewToScroll.getTranslationY() < viewToScroll.getHeight() * -0.6 && verticalOffset > viewToScroll.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
            }
        }
    }

    @Override
    public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        verticalOffset += dy;
        scrollingUp = dy > 0;
        int toolbarYOffset = (int) (dy - viewToScroll.getTranslationY());
        viewToScroll.animate().cancel();
        if (scrollingUp) {
            if (toolbarYOffset < viewToScroll.getHeight()) {
                viewToScroll.setTranslationY(-toolbarYOffset);
                isHidden = true;
            } else {
                viewToScroll.setTranslationY(-viewToScroll.getHeight());
                isHidden = true;
            }
        } else {
            if (toolbarYOffset < 0) {
                viewToScroll.setTranslationY(0);
                isHidden = false;
            } else {
                viewToScroll.setTranslationY(-toolbarYOffset);
                isHidden = false;
            }
        }
    }

    public void setVerticalOffset(int offset) {
        verticalOffset = offset;
    }

    public void toolbarAnimateShow(final int verticalOffset) {
        viewToScroll.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);

        isHidden = false;
    }

    private void toolbarAnimateHide() {
        viewToScroll.animate()
                .translationY(-viewToScroll.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);

        isHidden = true;
    }
}
