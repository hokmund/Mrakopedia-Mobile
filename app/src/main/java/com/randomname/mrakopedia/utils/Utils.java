package com.randomname.mrakopedia.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.ui.views.RippleImageButton;

import java.lang.reflect.Field;

import codetail.graphics.drawables.LollipopDrawablesCompat;

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
                "Шаблоны",
                "Не оборачивайся"
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

        public static String[] redirectCategoriesDescriptions = {
                "Темная романтика"
        };

        public static String[] redirectCategoriesDescriptionsTitles = {
                "Тёмная романтика"
        };

        public static String[] categoriesDescriptionsTitles = {
                "Юмор (спойлер)",
                "Файлы 21",
                "Переводы участников",
                "Пародии",
                "Паранормальная деревня",
                "Мясо",
                "Истории участников",
        };

        public static String[] categoryDescriptions = {
                "<p><b>ВНИМАНИЕ!</b>\n</p><p>Это категория-спойлер. " +
                        "В неё попадают истории, которые пытаются вызвать страх у читателя на " +
                        "протяжении всей своей длины, кроме финала, в котором их юмористическая суть " +
                        "ВНЕЗАПНО раскрывается. \n</p><p>Если вы не хотите портить себе впечатление от " +
                        "собранных здесь историй, то немедленно покиньте эту категорию.\n</p><p>Но если вы " +
                        "искали именно рассказы с неожиданными смешными концовками, то попали в нужное место. " +
                        "Скролльте вниз, чтобы увидеть список историй. Только не жалуйтесь на спойлерность, разумеется.\n</p>",

                "<p><b>Файлы 21</b> - самое крупное общество из существующих, занимающихся поиском и классификацией файлов," +
                        " которые неизвестным образом воздействуют на запустившего их человека либо на компьютер, " +
                        "на котором они находились/были запущены. Группа была создана 29 сентября 2003 года в Испании, " +
                        "Италии и Франции, из членов группы Кардиналы.\n</p>" +
                        "\n<p>После гибели верхушки Кардиналов в связи с файлом BarelyBreathing.exe, правительства стран, " +
                        "на территориях которых группа действовала, попытались скрыть их существование. Однако слухи расползлись" +
                        " с невероятной скоростью, и 21-е подразделение Кардиналов, группа из города Ош, численностью в 12" +
                        " человек, 29 сентября обьявила в Интернете о наборе новой группы. Впервые существование особых файлов" +
                        " было признано публично, самые безопасные из них даже были выложены на сайт. Зародился форум " +
                        "<a href='http://auch-21.com/'>http://auch-21.com/</a> (ныне не существует). Тогда же группа дала себе самоназвание, " +
                        "Файлы 21, и обьявила о международном наборе энтузиастов, интересующихся особыми файлами.\n</p>" +
                        "<p>Вскоре 9 человек из группы были убиты, а оставшиеся получили ультиматум с требованием прекратить свою деятельность. " +
                        "Группа из Ош ушла в подполье. В 20 странах Европы, Азии и Америки начались преследования группы, 4 правительства " +
                        "(в том числе и российское) выразили неофицальную поддержку начинаниям. По утверждениям представителей правительства," +
                        " начались неоглашенные международные дебаты о будущем группы. После полугода успехов и поражений, практически все страны" +
                        " признали необходимость существования такой группы. В связи с этим почти в каждое региональное подразделение группы 'добавили' " +
                        "представителей интересов государства. После еще года бурных обсуждений примерно половина государств существенно " +
                        "ослабила надзор, с наложением лишь обязательства присылать ежегодные отчеты о проделанной работе. К настоящему" +
                        " времени государством контролируется примерно треть подразделений группы. Сейчас функционирует официальный сайт группы," +
                        " содержащий данную вики.\n</p>" +
                        "<p>В данный момент полуофициально запрещена деятельность группы в следующих странах:\nАвстралия, Литва, Турция, " +
                        "Швеция\n</p><p>Группа существует как часть службы безопасности следующих стран:\nВеликобритания, Франция, Испания, " +
                        "Румыния, Чехия, Япония\n</p><p>Группа существует как независимое общество в следующих странах:\nБразилия, Венгрия, " +
                        "Германия, Дания, Италия, Канада, Корея, Польша,Португалия, Россия,США, Финляндия\n</p> " +
                        "<p><b>Данный раздел Мракопедии не является официальным представительством Организации!</b> " +
                        "Вся информация, размещённая здесь, не является инсайдерской и получена из открытых источников.\n</p>",

                "<p>Зарубежная крипота, переведённая на русский язык участниками Мракопедии.</p>",
                "<p>Здесь собраны юмористические подражания всевозможным страшным историям.</p>",
                "<p>Байки о мистических явлениях, происходящих в одной из русских деревень. " +
                        "Главной особенностью является то, что в историях непременно принимают участие " +
                        "два главных героя: первый - безымённый герой, который рассказывает историю от первого " +
                        "лица, второй - его друг Семёныч.</p>",
                "<p>Рассказы со сравнительно высоким уровнем жестокости и анатомических подробностей разных, кхм, процессов. Вы предупреждены.<p>",
                "<p>Эти истории были придуманы участниками Мракопедии, либо имели место в их жизни.</p>"
        };

        public static int convertDpToPixel(float dp, Context context){
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float px = dp * (metrics.densityDpi / 160f);
            return Math.round(px);
        }

        public static void setRippleToToolbarIcon(Toolbar toolbar, Context context) {
            if(Build.VERSION.SDK_INT < 21) {
                try {
                    Field f = toolbar.getClass().getDeclaredField("mNavButtonView");
                    f.setAccessible(true);
                    final View navigationIcon = (View) f.get(toolbar);
                    navigationIcon.setBackgroundDrawable(LollipopDrawablesCompat.getDrawable(context.getResources(), R.drawable.ripple, context.getTheme()));

                    final ViewTreeObserver viewTreeObserver = navigationIcon.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            Toolbar.LayoutParams params = (Toolbar.LayoutParams) navigationIcon.getLayoutParams();
                            params.width = navigationIcon.getHeight();
                            navigationIcon.setLayoutParams(params);

                            if (Build.VERSION.SDK_INT < 16) {
                                navigationIcon.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                navigationIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }

    public static void setRippleToMenuItem(View menuButton, Context context) {
        try {
            menuButton.setBackgroundDrawable(LollipopDrawablesCompat.getDrawable(context.getResources(), R.drawable.ripple, context.getTheme()));
            ActionMenuView.LayoutParams params = (ActionMenuView.LayoutParams) menuButton.getLayoutParams();
            params.width = menuButton.getHeight();
            menuButton.setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
}
