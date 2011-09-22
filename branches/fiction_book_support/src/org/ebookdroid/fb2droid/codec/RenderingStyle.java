package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.EBookDroidApp;

import android.graphics.Paint;
import android.graphics.Typeface;

class RenderingStyle {

    public static final Typeface NORMAL_TF = Typeface.createFromAsset(EBookDroidApp.getAppContext().getAssets(),
            "fonts/OldStandard-Regular.ttf");
    public static final Typeface ITALIC_TF = Typeface.createFromAsset(EBookDroidApp.getAppContext().getAssets(),
            "fonts/OldStandard-Italic.ttf");

    public static final int MAIN_TITLE_SIZE = 48;
    public static final int SECTION_TITLE_SIZE = 36;
    public static final int TEXT_SIZE = 24;
    public static final int FOOTNOTE_SIZE = 20;

    private static final Paint textPaint = new Paint();

    int textSize = RenderingStyle.TEXT_SIZE;
    JustificationMode jm = JustificationMode.Justify;
    boolean bold = false;
    Typeface face = RenderingStyle.NORMAL_TF;

    public RenderingStyle(int textSize) {
        this.textSize = textSize;
    }

    public RenderingStyle(RenderingStyle old) {
        this.textSize = old.textSize;
        this.jm = old.jm;
        this.bold = old.bold;
        this.face = old.face;
    }

    public Paint getTextPaint() {
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(face);
        textPaint.setFakeBoldText(bold);
        textPaint.setAntiAlias(true);
        return textPaint;
    }

    public static Paint getTextPaint(int textSize) {
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(NORMAL_TF);
        textPaint.setFakeBoldText(false);
        textPaint.setAntiAlias(true);
        return textPaint;
    }

}
