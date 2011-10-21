package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class FB2HorizontalRule extends AbstractFB2LineElement {

    private final float width;
    private final int height;
    private static Paint rulePaint;

    public FB2HorizontalRule(final int width, final int height) {
        this.width = width;
        this.height = height;
        if (rulePaint == null) {
            rulePaint = new Paint();
            rulePaint.setColor(Color.BLACK);
        }
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
    public void render(Canvas c, int y, int x) {
        c.drawLine(x, y - height/2, x + width, y - height/2, rulePaint);
    }

    @Override
    public void adjustWidth(float w) {
    }

    @Override
    public boolean isSizeable() {
        return false;
    }
}
