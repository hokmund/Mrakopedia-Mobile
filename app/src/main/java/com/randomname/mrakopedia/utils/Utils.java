package com.randomname.mrakopedia.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.randomname.mrakopedia.ui.views.RippleImageButton;

import java.lang.reflect.Field;

/**
 * Created by Vlad on 24.01.2016.
 */
public class Utils {

        public static boolean checkForLollipop() {
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        }

        public static String[] categoriesBanList = {
                "ICQ",
                "Архивы обсуждений",
                "Авторские",
                "Авторская",
                "Википедия",
                "Документация шаблонов",
                "Дублирующие",
                "Итоги",
                "Дятлы",
                "К обработке",
                "К удалению",
                "О проекте",
                "Обсуждения",
                "Общие",
                "Плашки",
                "Рейтинг",
                "Страницы с",
                "Тян не нужны",
                "Шаблоны"
        };

        public static String[] pagesBanList = {
                "Файл:",
                "Участник:",
                "Участница:",
                "Категор",
                "Обсуждение",
                "Архив:",
                "Шаблон:",
                "MediaWiki:",
                "Wikipedia:",
                "Рейтинг"
        };

        public static int convertDpToPixel(float dp, Context context){
                Resources resources = context.getResources();
                DisplayMetrics metrics = resources.getDisplayMetrics();
                float px = dp * (metrics.densityDpi / 160f);
                return Math.round(px);
        }

        public static void setRippleToToolbarIcon(Toolbar toolbar, Context context) {
                if(Build.VERSION.SDK_INT < 21) {

                        RippleImageButton rippleImageButton = null;

                        try {
                                Field f = toolbar.getClass().getDeclaredField("mNavButtonView");
                                f.setAccessible(true);

                                rippleImageButton = new RippleImageButton(context, null,
                                        android.support.v7.appcompat.R.attr.toolbarNavigationButtonStyle);
                                final Toolbar.LayoutParams lp = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
                                lp.gravity = GravityCompat.START | (Gravity.TOP & Gravity.VERTICAL_GRAVITY_MASK);
                                rippleImageButton.setLayoutParams(lp);
                                rippleImageButton.setCornerRadius(100);
                                rippleImageButton.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                                f.set(toolbar, rippleImageButton);

                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }
        }
}
