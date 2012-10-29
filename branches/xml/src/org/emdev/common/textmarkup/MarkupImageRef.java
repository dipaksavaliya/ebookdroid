package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.AbstractLineElement;
import org.emdev.common.textmarkup.line.Image;
import org.emdev.common.textmarkup.line.Line;
import org.emdev.utils.bytes.ByteArray.DataArrayInputStream;

public class MarkupImageRef implements MarkupElement {

    private final int ref;
    private final boolean inline;

    public MarkupImageRef(final int ref, final boolean inline) {
        this.ref = ref;
        this.inline = inline;
    }

    @Override
    public void publishToLines(MarkupStream stream, final ArrayList<Line> lines, final LineCreationParams params) {
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, ref, inline);
    }

    public static void write(final DataOutputStream out, final int ref, final boolean inline) throws IOException {
        out.writeByte(MarkupTag.MarkupImageRef.ordinal());
        out.writeInt(ref);
        out.writeBoolean(inline);
    }

    public static void addToLines(MarkupStream stream, ArrayList<Line> lines,
            LineCreationParams params) throws IOException {
        int ref = stream.in.readInt();
        boolean inline = stream.in.readBoolean();

        final Image image = params.content.getImage(ref, inline);
        if (image != null) {
            if (!inline) {
                final Line line = new Line(params.content, stream, params.maxLineWidth, params.jm);
                line.applyJustification(JustificationMode.Center);
                lines.add(line);
            }
            AbstractLineElement.addToLines(stream, MarkupTag.Image, -1, image, image.width, image.height, lines, params);
        }
    }

}
