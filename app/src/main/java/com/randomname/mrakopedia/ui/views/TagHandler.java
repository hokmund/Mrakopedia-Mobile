package com.randomname.mrakopedia.ui.views;

import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.util.Log;

import org.xml.sax.XMLReader;

/**
 * Created by vgrigoryev on 21.01.2016.
 */
public class TagHandler implements Html.TagHandler {
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equals("center")) {
            if(opening){
                start((SpannableStringBuilder) output, new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER));

            } else {
                end((SpannableStringBuilder) output, AlignmentSpan.class, new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER));
            }
        }
    }

    private static <T> Object getLast(Spanned text, Class<T> kind) {
    /*
     * This knows that the last returned object from getSpans()
     * will be the most recently added.
     */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static <T> void end(SpannableStringBuilder text, Class<T> kind,
                                Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
