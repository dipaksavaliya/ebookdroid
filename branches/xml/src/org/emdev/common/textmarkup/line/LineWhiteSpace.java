package org.emdev.common.textmarkup.line;


import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import android.graphics.Canvas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.MarkupTag;


public class LineWhiteSpace extends AbstractLineElement {

    public LineWhiteSpace(final float width, final int height) {
        super(width, height);
    }

    @Override
    public float render(final Canvas c, final int y, final int x, final float additionalWidth, float left, float right, final int nightmode) {
        return width + additionalWidth;
    }

    @Override
    public void publishToStream(DataOutputStream out) throws IOException {
        write(out, MarkupTag.LineWhiteSpace, width, height);
    }

    public static void publishToLines(DataInputStream in, ArrayList<Line> lines, LineCreationParams params) throws IOException {
        final float width = in.readFloat();
        final int height = in.readInt();
        new LineWhiteSpace(width, height).publishToLines(lines, params);
    }
}
