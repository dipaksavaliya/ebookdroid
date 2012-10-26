package org.emdev.common.textmarkup;


import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Line;
import org.emdev.utils.LengthUtils;

public class MarkupParagraphEnd implements MarkupElement {

    public static final MarkupParagraphEnd E = new MarkupParagraphEnd();

    private MarkupParagraphEnd() {
    }

    @Override
    public void publishToLines(ArrayList<Line> lines, LineCreationParams params) {
        if (LengthUtils.isEmpty(lines)) {
            return;
        }
        final Line last = lines.get(lines.size() - 1);
        final JustificationMode lastJm = params.jm == JustificationMode.Justify ? JustificationMode.Left : params.jm;
        for (int i = lines.size() - 1; i >= 0; i--) {
            Line l = lines.get(i);
            if (l.committed) {
                break;
            }
            l.applyJustification(l != last ? params.jm : lastJm);
        }
    }

    @Override
    public void publishToStream(DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupParagraphEnd.ordinal());
    }
}
