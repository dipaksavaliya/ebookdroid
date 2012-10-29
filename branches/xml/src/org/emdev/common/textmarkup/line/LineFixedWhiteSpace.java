package org.emdev.common.textmarkup.line;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import android.graphics.Canvas;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.MarkupStream;
import org.emdev.common.textmarkup.MarkupTag;

public class LineFixedWhiteSpace extends AbstractLineElement {

    public LineFixedWhiteSpace(final float width, final int height) {
        super(MarkupTag.LineFixedWhiteSpace, width, height);
    }

    @Override
    public float render(final Canvas c, final int y, final int x, final float additionalWidth, final float left,
            final float right, final int nightmode) {
        return width;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, MarkupTag.LineFixedWhiteSpace, width, height);
    }

    public static void addToLines(MarkupStream stream, final ArrayList<Line> lines,
            final LineCreationParams params) throws IOException {
        final int position = stream.in.position() - 1;
        final float width = stream.in.readFloat();
        final int height = stream.in.readInt();

        AbstractLineElement.addToLines(stream, MarkupTag.LineFixedWhiteSpace, position, null, width, height, lines, params);
    }
}
