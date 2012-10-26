package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataInputStream;
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
    public void publishToLines(final ArrayList<Line> lines, final LineCreationParams params) {
        Line.getLastLine(lines, params).setTitle(this);
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, title, level);
    }

    public static void write(final DataOutputStream out, final String title, final int level) throws IOException {
        final byte[] bytes = title.getBytes();
        out.writeByte(MarkupTag.MarkupTitle.ordinal());
        out.writeInt(bytes.length + 1);
        out.writeByte(level);
        out.write(bytes);
    }

    public static void publishToLines(final DataInputStream in, final ArrayList<Line> lines,
            final LineCreationParams params) throws IOException {
        final int length = in.readInt() - 1;
        final int level = in.readByte();
        final byte[] bytes = new byte[length];
        in.read(bytes);
        String title = new String(bytes);
        Line.getLastLine(lines, params).setTitle(new MarkupTitle(title, level));
    }

}
