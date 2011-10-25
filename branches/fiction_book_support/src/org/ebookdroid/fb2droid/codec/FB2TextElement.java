package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

public class FB2TextElement extends AbstractFB2LineElement {

    private final char[] chars;
    private final int start;
    private final int length;
    private final float width;

    private RenderingStyle renderingState;

    public FB2TextElement(char[] ch, int st, int len, RenderingStyle renderingState) {
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.renderingState = renderingState;
        this.width = (int) renderingState.getTextPaint().measureText(ch, st, len);
    }

    public FB2TextElement(char[] ch, int st, int len, RenderingStyle style, int width) {
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.renderingState = style;
        this.width = width;
    }

    @Override
    public int getHeight() {
        return renderingState.textSize;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void render(final Canvas c, final int y, final int x) {
        c.drawText(chars, start, length, x, renderingState.isSuperScript() ? y - renderingState.textSize
                : renderingState.isSubScript() ? y + renderingState.textSize / 2 : y, renderingState.getTextPaint());
    }

    @Override
    public void adjustWidth(final float w) {
    }

    @Override
    public boolean isSizeable() {
        return false;
    }
}
