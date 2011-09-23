package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class FB2HorizontalRule extends AbstractFB2LineElement {

    private final int width;
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
    public int getWidth() {
        return width;
    }

    @Override
    public void render(Canvas c, int y, int x) {
        c.drawLine(x, y - height/2, x + width, y - height/2, rulePaint);
    }

    @Override
    public void adjustWidth(int w) {
    }

    @Override
    public boolean isSizeable() {
        return false;
    }
    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(RULE_ELEMENT_TAG);
        out.writeInt(height);
        out.writeInt(width);
    }
    public static FB2LineElement deserializeImpl(DataInputStream in) throws IOException {
        int height = in.readInt();
        int width = in.readInt();
        return new FB2HorizontalRule(width, height);
    }

}
