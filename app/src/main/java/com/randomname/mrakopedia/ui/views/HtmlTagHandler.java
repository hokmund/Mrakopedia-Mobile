package com.randomname.mrakopedia.ui.views;

import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Layout.Alignment;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan.Standard;
import android.text.style.StrikethroughSpan;
import android.text.style.TypefaceSpan;

import org.xml.sax.XMLReader;

import java.util.Stack;

public class HtmlTagHandler implements TagHandler {
    Stack<String> lists = new Stack();
    Stack<Integer> olNextIndex = new Stack();
    private static final int indent = 10;
    private static final int listItemIndent = 20;
    private static final BulletSpan bullet = new BulletSpan(10);

    public HtmlTagHandler() {
    }

    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if(opening) {
            if(tag.equalsIgnoreCase("ul")) {
                this.lists.push(tag);
            } else if(tag.equalsIgnoreCase("ol")) {
                this.lists.push(tag);
                this.olNextIndex.push(Integer.valueOf(1));
            } else if(tag.equalsIgnoreCase("li")) {
                if(output.length() > 0 && output.charAt(output.length() - 1) != 10) {
                    output.append("\n");
                }

                String numberMargin = (String)this.lists.peek();
                if(numberMargin.equalsIgnoreCase("ol")) {
                    this.start(output, new HtmlTagHandler.Ol());
                    output.append(((Integer)this.olNextIndex.peek()).toString()).append(". ");
                    this.olNextIndex.push(Integer.valueOf(((Integer)this.olNextIndex.pop()).intValue() + 1));
                } else if(numberMargin.equalsIgnoreCase("ul")) {
                    this.start(output, new HtmlTagHandler.Ul());
                }
            } else if(tag.equalsIgnoreCase("code")) {
                this.start(output, new HtmlTagHandler.Code());
            } else if(tag.equalsIgnoreCase("center")) {
                this.start(output, new HtmlTagHandler.Center());
            } else if(tag.equalsIgnoreCase("s") || tag.equalsIgnoreCase("strike")) {
                this.start(output, new HtmlTagHandler.Strike());
            }
        } else if(tag.equalsIgnoreCase("ul")) {
            this.lists.pop();
        } else if(tag.equalsIgnoreCase("ol")) {
            this.lists.pop();
            this.olNextIndex.pop();
        } else if(tag.equalsIgnoreCase("li")) {
            int numberMargin1;
            if(((String)this.lists.peek()).equalsIgnoreCase("ul")) {
                if(output.length() > 0 && output.charAt(output.length() - 1) != 10) {
                    output.append("\n");
                }

                numberMargin1 = 10;
                if(this.lists.size() > 1) {
                    numberMargin1 = 10 - bullet.getLeadingMargin(true);
                    if(this.lists.size() > 2) {
                        numberMargin1 -= (this.lists.size() - 2) * 20;
                    }
                }

                BulletSpan newBullet = new BulletSpan(numberMargin1);
                this.end(output, HtmlTagHandler.Ul.class, false, new Object[]{new Standard(20 * (this.lists.size() - 1)), newBullet});
            } else if(((String)this.lists.peek()).equalsIgnoreCase("ol")) {
                if(output.length() > 0 && output.charAt(output.length() - 1) != 10) {
                    output.append("\n");
                }

                numberMargin1 = 20 * (this.lists.size() - 1);
                if(this.lists.size() > 2) {
                    numberMargin1 -= (this.lists.size() - 2) * 20;
                }

                this.end(output, HtmlTagHandler.Ol.class, false, new Object[]{new Standard(numberMargin1)});
            }
        } else if(tag.equalsIgnoreCase("code")) {
            this.end(output, HtmlTagHandler.Code.class, false, new Object[]{new TypefaceSpan("monospace")});
        } else if(tag.equalsIgnoreCase("center")) {
            this.end(output, HtmlTagHandler.Center.class, true, new Object[]{new android.text.style.AlignmentSpan.Standard(Alignment.ALIGN_CENTER)});
        } else if(tag.equalsIgnoreCase("s") || tag.equalsIgnoreCase("strike")) {
            this.end(output, HtmlTagHandler.Strike.class, false, new Object[]{new StrikethroughSpan()});
        }

    }

    private void start(Editable output, Object mark) {
        int len = output.length();
        output.setSpan(mark, len, len, 17);
    }

    private void end(Editable output, Class kind, boolean paragraphStyle, Object... replaces) {
        Object obj = getLast(output, kind);
        int where = output.getSpanStart(obj);
        int len = output.length();
        output.removeSpan(obj);
        if(where != len) {
            int thisLen = len;
            if(paragraphStyle) {
                output.append("\n");
                thisLen = len + 1;
            }

            Object[] arr$ = replaces;
            int len$ = replaces.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                Object replace = arr$[i$];
                output.setSpan(replace, where, thisLen, 33);
            }
        }

    }

    private static Object getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if(objs.length == 0) {
            return null;
        } else {
            for(int i = objs.length; i > 0; --i) {
                if(text.getSpanFlags(objs[i - 1]) == 17) {
                    return objs[i - 1];
                }
            }

            return null;
        }
    }

    private static class Strike {
        private Strike() {
        }
    }

    private static class Center {
        private Center() {
        }
    }

    private static class Code {
        private Code() {
        }
    }

    private static class Ol {
        private Ol() {
        }
    }

    private static class Ul {
        private Ul() {
        }
    }
}