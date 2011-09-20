package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;
import android.graphics.Paint;


public class FB2TextElement implements FB2LineElement {

    private final char[] chars;
    private final int start;
    private final int length;
    private final int height;
    private final int width;
    private final boolean bold;
    private final boolean italic;

    private final static Paint textPaint = new Paint();
    
    public FB2TextElement(char[] ch, int st, int len, int height, int width, boolean bold, boolean italic) {
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
    public void render(Canvas c, int y, int x) {
        textPaint.setTextSize(height);
        textPaint.setTypeface(italic ? FB2Document.ITALIC_TF : FB2Document.NORMAL_TF);
        textPaint.setFakeBoldText(bold);
        textPaint.setAntiAlias(true);
        c.drawText(chars, start, length, x, y, textPaint);
    }

    @Override
    public void adjustWidth(int w) {
    }

    @Override
    public boolean isSizeable() {
        return false;
    }

}
