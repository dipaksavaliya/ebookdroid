package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.LineStream;

public class MarkupExtraSpace implements MarkupElement {

    private final int extraSpace;

    public MarkupExtraSpace(final int extraSpace) {
        this.extraSpace = extraSpace;
    }

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
        lines.params.extraSpace += this.extraSpace;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, extraSpace);
    }

    public static void write(final DataOutputStream out, final int extraSpace) throws IOException {
        out.writeByte(MarkupTag.MarkupExtraSpace.ordinal());
        out.writeInt(extraSpace);
    }

    public static void addToLines(final MarkupStream stream, final LineStream lines) throws IOException {
        final int extraSpace = stream.in.readInt();
        lines.params.extraSpace += extraSpace;
    }
}
