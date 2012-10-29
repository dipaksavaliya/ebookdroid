package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Line;
import org.emdev.utils.bytes.ByteArray.DataArrayInputStream;

public class MarkupExtraSpace implements MarkupElement {

    private final int extraSpace;

    public MarkupExtraSpace(final int extraSpace) {
        this.extraSpace = extraSpace;
    }

    @Override
    public void publishToLines(MarkupStream stream, final ArrayList<Line> lines, final LineCreationParams params) {
        params.extraSpace += this.extraSpace;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, extraSpace);
    }

    public static void write(final DataOutputStream out, final int extraSpace) throws IOException {
        out.writeByte(MarkupTag.MarkupExtraSpace.ordinal());
        out.writeInt(extraSpace);
    }

    public static void addToLines(final MarkupStream stream, final ArrayList<Line> lines,
            final LineCreationParams params) throws IOException {

        final int extraSpace = stream.in.readInt();
        params.extraSpace += extraSpace;
    }
}
