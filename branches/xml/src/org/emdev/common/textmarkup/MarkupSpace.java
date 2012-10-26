package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Line;

public class MarkupSpace implements MarkupElement {

    private MarkupSpace() {
    }

    public static final MarkupSpace E = new MarkupSpace();

    @Override
    public void publishToLines(final ArrayList<Line> lines, final LineCreationParams params) {
        params.insertSpace = true;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(final DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupSpace.ordinal());
    }
}
