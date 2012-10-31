package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.LineStream;

public class MarkupNoSpace implements MarkupElement {

    private MarkupNoSpace() {

    }

    public static final MarkupNoSpace E = new MarkupNoSpace();

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
        lines.params.insertSpace = false;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(final DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupNoSpace.ordinal());
    }
}
