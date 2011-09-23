package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.EBookDroidApp;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.SparseArray;

class RenderingStyle {

    public static final Typeface NORMAL_TF = Typeface.createFromAsset(EBookDroidApp.getAppContext().getAssets(),
            "fonts/OldStandard-Regular.ttf");
    public static final Typeface ITALIC_TF = Typeface.createFromAsset(EBookDroidApp.getAppContext().getAssets(),
            "fonts/OldStandard-Italic.ttf");

    private static final SparseArray<TextPaint> paints = new SparseArray<TextPaint>();
    private static final SparseArray<RenderingStyle> styles = new SparseArray<RenderingStyle>();

    public static final int MAIN_TITLE_SIZE = 48;
    public static final int SECTION_TITLE_SIZE = 36;
    public static final int SUBTITLE_SIZE = 30;
    public static final int TEXT_SIZE = 24;
    public static final int FOOTNOTE_SIZE = 20;

    final int textSize;
    final JustificationMode jm;
    final boolean bold;
    final Typeface face;
    final TextPaint paint;

    public RenderingStyle(final int textSize) {
        this.textSize = textSize;
        this.jm = JustificationMode.Justify;
        this.bold = false;
        this.face = RenderingStyle.NORMAL_TF;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old) {
        this.textSize = old.textSize;
        this.jm = old.jm;
        this.bold = old.bold;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old, int textSize) {
        this.textSize = textSize;
        this.jm = old.jm;
        this.bold = old.bold;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old, int textSize, JustificationMode jm) {
        this.textSize = textSize;
        this.jm = jm;
        this.bold = old.bold;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old, int textSize, JustificationMode jm, boolean bold, Typeface face) {
        this.textSize = textSize;
        this.jm = jm;
        this.bold = bold;
        this.face = face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old, JustificationMode jm) {
        this.textSize = old.textSize;
        this.jm = jm;
        this.bold = old.bold;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old, JustificationMode jm, Typeface face) {
        this.textSize = old.textSize;
        this.jm = jm;
        this.bold = old.bold;
        this.face = face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old, boolean bold) {
        this.textSize = old.textSize;
        this.jm = old.jm;
        this.bold = bold;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(final RenderingStyle old, Typeface face) {
        this.textSize = old.textSize;
        this.jm = old.jm;
        this.bold = old.bold;
        this.face = face;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public RenderingStyle(int textSize, boolean bold, boolean italic) {
        this.textSize = textSize;
        this.jm = JustificationMode.Justify;
        this.bold = bold;
        this.face = italic ? ITALIC_TF : NORMAL_TF;
        this.paint = getTextPaint(face, textSize, bold);
    }

    public Paint getTextPaint() {
        return paint;
    }

    public static Paint getTextPaint(final int textSize) {
        return getTextPaint(NORMAL_TF, textSize, false);
    }

    private static final TextPaint getTextPaint(final Typeface face, final int textSize, final boolean bold) {
        final int key = (textSize & 0x0FFF) + (face == ITALIC_TF ? 1 << 14 : 0) + (bold ? 1 << 15 : 0);
        TextPaint paint = paints.get(key);
        if (paint == null) {
            paint = new TextPaint();
            paint.setTextSize(textSize);
            paint.setTypeface(face);
            paint.setFakeBoldText(bold);
            paint.setAntiAlias(true);

            paints.append(key, paint);
        }
        return paint;
    }

    public static RenderingStyle getStyle(int textSize, boolean bold, boolean italic) {
        final int key = (textSize & 0x0FFF) + (italic ? 1 << 14 : 0) + (bold ? 1 << 15 : 0);
        RenderingStyle style = styles.get(key);
        if (style == null) {
            style = new RenderingStyle(textSize, bold, italic);
            
            styles.append(key, style);
        }
        return style;
    }
}
