package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.LineStream;

public class MarkupSpace implements MarkupElement {

    private MarkupSpace() {
    }

    public static final MarkupSpace E = new MarkupSpace();

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
        lines.params.insertSpace = true;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(final DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupSpace.ordinal());
    }
}
