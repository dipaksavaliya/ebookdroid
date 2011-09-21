package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

public class FB2TextElement implements FB2LineElement {

    private final char[] chars;
    private final int start;
    private final int length;
    private final int width;

    private RenderingStyle renderingState;

    public FB2TextElement(char[] ch, int st, int len, RenderingStyle renderingState) {
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.renderingState = renderingState;
        this.width = (int) renderingState.getTextPaint().measureText(ch, st, len);
    }

    @Override
    public int getHeight() {
        return renderingState.textSize;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void render(final Canvas c, final int y, final int x) {
        c.drawText(chars, start, length, x, y, renderingState.getTextPaint());
    }

    @Override
    public void adjustWidth(final int w) {
    }

    @Override
    public boolean isSizeable() {
        return false;
    }
}
