package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Image;
import org.emdev.common.textmarkup.line.Line;

public class MarkupImageRef implements MarkupElement {

    private final int ref;
    private final boolean inline;

    public MarkupImageRef(final int ref, final boolean inline) {
        this.ref = ref;
        this.inline = inline;
    }

    @Override
    public void publishToLines(final ArrayList<Line> lines, final LineCreationParams params) {
        final Image image = params.content.getImage(ref, inline);
        if (image != null) {
            if (!inline) {
                final Line line = new Line(params.maxLineWidth, params.jm);
                line.append(image);
                line.applyJustification(JustificationMode.Center);
                lines.add(line);
            } else {
                image.publishToLines(lines, params);
            }
        }
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

    public static void publishToLines(DataInputStream in, ArrayList<Line> lines, LineCreationParams params) throws IOException {
        int ref = in.readInt();
        boolean inline = in.readBoolean();

        final Image image = params.content.getImage(ref, inline);
        if (image != null) {
            if (!inline) {
                final Line line = new Line(params.maxLineWidth, params.jm);
                line.append(image);
                line.applyJustification(JustificationMode.Center);
                lines.add(line);
            } else {
                image.publishToLines(lines, params);
            }
        }
    }

}
