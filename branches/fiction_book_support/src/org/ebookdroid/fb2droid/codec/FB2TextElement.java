package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;
import android.graphics.Paint;

public class FB2TextElement implements FB2LineElement {

    private static final Paint textPaint = new Paint();

    private final char[] chars;
    private final int start;
    private final int length;
    private final int height;
    private final int width;
    private final boolean bold;
    private final boolean italic;

    public FB2TextElement(final char[] ch, final int st, final int len, final int height, final int width,
            final boolean bold, final boolean italic) {
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.height = height;
        this.width = width;
        this.bold = bold;
        this.italic = italic;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void render(final Canvas c, final int y, final int x) {
        textPaint.setTextSize(height);
        textPaint.setTypeface(italic ? FB2Document.ITALIC_TF : FB2Document.NORMAL_TF);
        textPaint.setFakeBoldText(bold);
        textPaint.setAntiAlias(true);
        c.drawText(chars, start, length, x, y, textPaint);
    }

    @Override
    public void adjustWidth(final int w) {
    }

    @Override
    public boolean isSizeable() {
        return false;
    }
}
