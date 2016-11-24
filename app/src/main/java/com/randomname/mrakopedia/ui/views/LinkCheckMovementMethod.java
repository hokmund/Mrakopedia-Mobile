package com.randomname.mrakopedia.ui.views;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Vlad on 24.11.2016.
 */

public class LinkCheckMovementMethod extends LinkMovementMethod {

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event ) {
        try {
            return super.onTouchEvent( widget, buffer, event ) ;
        } catch( Exception ex ) {
            if (widget.getContext() == null) {
                return true;
            }
            Toast.makeText( widget.getContext(), "Не удалось открыть страницу", Toast.LENGTH_SHORT ).show();
            return true;
        }
    }

}
