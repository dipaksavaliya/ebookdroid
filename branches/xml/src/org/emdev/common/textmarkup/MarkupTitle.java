package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Line;

public class MarkupTitle implements MarkupElement {

    public final String title;
    public final int level;

    public MarkupTitle(final String string, final int level) {
        this.title = string;
        this.level = level;
    }

    @Override
    public void publishToLines(MarkupStream stream, final ArrayList<Line> lines, final LineCreationParams params) {
        // Line.getLastLine(params.content, lines, params).setTitle(this);
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, title, level);
    }

    public static void write(final DataOutputStream out, final String title, final int level) throws IOException {
        final byte[] bytes = title.getBytes();
        out.writeByte(MarkupTag.MarkupTitle.ordinal());
        out.writeByte(level);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static void addToLines(final MarkupStream stream, final ArrayList<Line> lines,
            final LineCreationParams params) throws IOException {
        final int level = stream.in.readByte();
        final int length = stream.in.readInt();
        final byte[] bytes = new byte[length];
        stream.in.read(bytes);
        String title = new String(bytes);
        Line.getLastLine(params.content, stream, lines, params).setTitle(new MarkupTitle(title, level));
    }

}
