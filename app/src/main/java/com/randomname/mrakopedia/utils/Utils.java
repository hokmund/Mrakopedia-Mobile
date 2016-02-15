package com.randomname.mrakopedia.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

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
}
