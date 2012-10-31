package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.Line;
import org.emdev.common.textmarkup.line.LineStream;
import org.emdev.utils.LengthUtils;

public class MarkupParagraphEnd implements MarkupElement {

    public static final MarkupParagraphEnd E = new MarkupParagraphEnd();

    private MarkupParagraphEnd() {
    }

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
        if (LengthUtils.isEmpty(lines)) {
            return;
        }
        final Line last = lines.last();
        final JustificationMode lastJm = lines.params.jm == JustificationMode.Justify ? JustificationMode.Left
                : lines.params.jm;
        for (int i = lines.size() - 1; i >= 0; i--) {
            final Line l = lines.get(i);
            if (l.committed) {
                break;
            }
            l.applyJustification(l != last ? lines.params.jm : lastJm);
        }
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(final DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupParagraphEnd.ordinal());
    }
}
