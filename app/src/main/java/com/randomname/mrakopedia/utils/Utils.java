package com.randomname.mrakopedia.utils;

import android.os.Build;

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
                "Категор"
        };
}
