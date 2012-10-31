package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.AbstractLineElement;
import org.emdev.common.textmarkup.line.Image;
import org.emdev.common.textmarkup.line.Line;
import org.emdev.common.textmarkup.line.LineStream;

public class MarkupImageRef implements MarkupElement {

    private final int ref;
    private final boolean inline;

    public MarkupImageRef(final int ref, final boolean inline) {
        this.ref = ref;
        this.inline = inline;
    }

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
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

    public static void addToLines(final MarkupStream stream, final LineStream lines) throws IOException {
        final int ref = stream.in.readInt();
        final boolean inline = stream.in.readBoolean();

        final Image image = lines.params.content.getImage(ref, inline);
        if (image != null) {
            if (!inline) {
                final Line line = lines.add(stream);
                line.applyJustification(JustificationMode.Center);
            }
            AbstractLineElement.addToLines(stream, MarkupTag.Image, -1, image, image.width, image.height, lines);
        }
    }

}
