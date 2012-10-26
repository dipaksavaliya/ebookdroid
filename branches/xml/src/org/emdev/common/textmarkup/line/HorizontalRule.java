package org.emdev.common.textmarkup.line;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.MarkupTag;

public class HorizontalRule extends AbstractLineElement {

    private static Paint rulePaint;

    public HorizontalRule(final int width, final int height) {
        super(width, height);
        if (rulePaint == null) {
            rulePaint = new Paint();
            rulePaint.setColor(Color.BLACK);
        }
    }

    @Override
    public float render(final Canvas c, final int y, final int x, final float additionalWidth, final float left,
            final float right, final int nightmode) {
        if (left < x + width && x < right) {
            c.drawLine(x, y - height / 2, x + width, y - height / 2, rulePaint);
            c.drawRect(x, y - height / 2, x + width, y - height / 2 + 1, rulePaint);
        }
        return width;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, MarkupTag.HorizontalRule, width, height);
    }

}
