package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

public class FB2LineWhiteSpace extends AbstractFB2LineElement {

    private final int height;
    private float width;
    private final boolean sizeable;

    public FB2LineWhiteSpace(final float width, final int height, final boolean sizeable) {
        this.width = width;
        this.height = height;
        this.sizeable = sizeable;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void render(final Canvas c, final int y, final int x) {
    }

    @Override
    public void adjustWidth(final float w) {
        if (sizeable) {
            width += w;
        }
    }

    @Override
    public boolean isSizeable() {
        return sizeable;
    }
}
