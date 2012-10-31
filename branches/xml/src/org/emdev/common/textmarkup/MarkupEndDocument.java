package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.LineStream;

public class MarkupEndDocument implements MarkupElement {

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(final DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupEndDocument.ordinal());
    }
}
