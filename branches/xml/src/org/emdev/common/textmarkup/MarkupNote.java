package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.Line;
import org.emdev.common.textmarkup.line.LineStream;
import org.emdev.utils.LengthUtils;

public class MarkupNote implements MarkupElement {

    private final int ref;

    public MarkupNote(final int ref) {
        this.ref = ref;
    }

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, ref);
    }

    public static void write(final DataOutputStream out, final int ref) throws IOException {
        out.writeByte(MarkupTag.MarkupNote.ordinal());
        out.writeInt(ref);
    }

    public static void addToLines(final MarkupStream stream, final LineStream lines) throws IOException {

        final int ref = stream.in.readInt();
        final LineStream note = lines.params.content.getNote(ref);
        if (note != null && LengthUtils.isNotEmpty(lines)) {
            final Line line = lines.last(stream);
            line.addNote(stream, note);
        }
    }
};
