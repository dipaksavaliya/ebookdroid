package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.LineStream;

public class MarkupTitle implements MarkupElement {

    public final String title;
    public final int level;

    public MarkupTitle(final String string, final int level) {
        this.title = string;
        this.level = level;
    }

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
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

    public static void addToLines(final MarkupStream stream, final LineStream lines) throws IOException {
        final int level = stream.in.readByte();
        final int length = stream.in.readInt();
        final byte[] bytes = new byte[length];
        stream.in.read(bytes);

        final String title = new String(bytes);
        lines.last(stream).setTitle(new MarkupTitle(title, level));
    }

}
