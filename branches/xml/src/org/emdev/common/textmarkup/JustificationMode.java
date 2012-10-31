package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.LineStream;

public enum JustificationMode implements MarkupElement {

    /** Centered line */
    Center,

    /** Left justified */
    Left,

    /** Right justified */
    Right,

    /** Width justified */
    Justify;

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
        lines.params.jm = this;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, this);
    }

    public static void write(final DataOutputStream out, final JustificationMode jm) throws IOException {
        out.writeByte(MarkupTag.JustificationMode.ordinal());
        out.writeByte(jm.ordinal());
    }

    public static void addToLines(final MarkupStream stream, final LineStream lines) throws IOException {
        final JustificationMode jm = JustificationMode.values()[stream.in.readByte()];
        jm.publishToLines(stream, lines);
    }
}
