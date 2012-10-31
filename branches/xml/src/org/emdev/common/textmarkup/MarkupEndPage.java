package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.FB2Page;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.Line;
import org.emdev.common.textmarkup.line.LineStream;

public class MarkupEndPage extends Line implements MarkupElement {

    public static final MarkupElement E = new MarkupEndPage();

    private MarkupEndPage() {
        super(null, null, 0, JustificationMode.Left);
    }

    @Override
    public void publishToLines(final MarkupStream stream, final LineStream lines) {
        if (lines.last() == E) {
            return;
        }
        lines.add(this);
    }

    @Override
    public int getTotalHeight() {
        return 2 * FB2Page.PAGE_HEIGHT; // to be sure
    }

    @Override
    public boolean appendable() {
        return false;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out);
    }

    public static void write(final DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.MarkupEndPage.ordinal());
    }
}
