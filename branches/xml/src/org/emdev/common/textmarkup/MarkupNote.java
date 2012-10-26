package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.emdev.common.textmarkup.line.Line;
import org.emdev.utils.LengthUtils;

public class MarkupNote implements MarkupElement {

    private final int ref;

    public MarkupNote(final int ref) {
        this.ref = ref;
    }

    @Override
    public void publishToLines(final ArrayList<Line> lines, final LineCreationParams params) {
        final List<Line> note = params.content.getNote(ref);
        if (note != null && LengthUtils.isNotEmpty(lines)) {
            final Line line = Line.getLastLine(lines, params);
            line.addNote(note);
        }
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, ref);
    }

    public static void write(final DataOutputStream out, final int ref) throws IOException {
        out.writeByte(MarkupTag.MarkupNote.ordinal());
        out.writeInt(ref);
    }

    public static void publishToLines(final DataInputStream in, final ArrayList<Line> lines,
            final LineCreationParams params) throws IOException {
        final int ref = in.readInt();
        final List<Line> note = params.content.getNote(ref);
        if (note != null && LengthUtils.isNotEmpty(lines)) {
            final Line line = Line.getLastLine(lines, params);
            line.addNote(note);
        }
    }
};
