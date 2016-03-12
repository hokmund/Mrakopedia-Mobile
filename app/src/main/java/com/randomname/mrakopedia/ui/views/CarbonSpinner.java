package com.randomname.mrakopedia.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.caverock.androidsvg.SVG;

import carbon.Carbon;
import carbon.widget.Spinner;

/**
 * Created by Vlad on 12.03.2016.
 */
public class CarbonSpinner extends Spinner{
    public CarbonSpinner(Context context) {
        super(context);
    }

    public CarbonSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CarbonSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDropDownColor(int color) {
        try {
            int size = (int) (Carbon.getDip(getContext()) * 24);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            SVG svg3 = SVG.getFromResource(getContext(), carbon.R.raw.carbon_dropdown);
            Canvas canvas = new Canvas(bitmap);
            svg3.setDocumentWidth(bitmap.getWidth());
            svg3.setDocumentHeight(bitmap.getHeight());
            svg3.renderToCanvas(canvas);

            BitmapDrawable dropdown = new BitmapDrawable(bitmap);
            dropdown.setBounds(0, 0, size, size);
            dropdown.setAlpha(Color.alpha(color));
            dropdown.setColorFilter(new LightingColorFilter(0, color));
            setCompoundDrawables(null, null, dropdown, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
