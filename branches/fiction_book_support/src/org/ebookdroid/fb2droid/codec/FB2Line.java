package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FB2Line {

    private final ArrayList<FB2LineElement> elements = new ArrayList<FB2LineElement>();
    private int height;
    private int width = 0;
    private boolean hasNonWhiteSpaces = false;
    private List<FB2Line> footnotes;

    public FB2Line() {
    }

    public FB2Line append(final FB2LineElement element) {
        elements.add(element);
        if (element.getHeight() > height) {
            height = element.getHeight();
        }
        if (!(element instanceof FB2LineWhiteSpace)) {
            hasNonWhiteSpaces = true;
        }
        width += element.getWidth();
        return this;
    }

    public int getTotalHeight() {
        int h = height;
        if (footnotes != null) {
            h += footnotes.get(0).getHeight();
        }
        return h;
    }

    public int getWidth() {
        return width;
    }

    public int recalculateWidth() {
        width = 0;
        for (final FB2LineElement e : elements) {
            width += e.getWidth();
        }
        return width;
    }

    public void render(final Canvas c, final int y) {
        int x = FB2Page.MARGIN_X;
        for (final FB2LineElement e : elements) {
            e.render(c, y, x);
            x += e.getWidth();
        }
    }

    public static FB2Line getLastLine(final ArrayList<FB2Line> lines) {
        if (lines.size() == 0) {
            lines.add(new FB2Line());
        }
        return lines.get(lines.size() - 1);
    }

    public void applyJustification(final JustificationMode jm) {
        switch (jm) {
            case Center:
                final int x = (FB2Page.PAGE_WIDTH - (width + 2 * FB2Page.MARGIN_X)) / 2;
                elements.add(0, new FB2LineWhiteSpace(x, height, true));
                elements.add(new FB2LineWhiteSpace(x, height, true));
                break;
            case Left:
                final int x2 = (FB2Page.PAGE_WIDTH - (width + 2 * FB2Page.MARGIN_X));
                elements.add(new FB2LineWhiteSpace(x2, height, true));
                break;
            case Justify:
                int ws = 0;
                for (final FB2LineElement e : elements) {
                    if (e instanceof FB2LineWhiteSpace && e.isSizeable()) {
                        ws++;
                    }
                }
                if (ws > 0) {
                    final int wsx = (FB2Page.PAGE_WIDTH - (width + 2 * FB2Page.MARGIN_X)) / ws;
                    for (final FB2LineElement e : elements) {
                        if (e instanceof FB2LineWhiteSpace && e.isSizeable()) {
                            e.adjustWidth(wsx);
                        }
                    }
                }
                break;
            case Right:
                final int x1 = (FB2Page.PAGE_WIDTH - (width + 2 * FB2Page.MARGIN_X));
                elements.add(0, new FB2LineWhiteSpace(x1, height, true));
                break;
        }
        recalculateWidth();
    }

    public boolean hasNonWhiteSpaces() {
        return hasNonWhiteSpaces;
    }

    public List<FB2Line> getFootNotes() {
        return footnotes;
    }

    public void addNote(final List<FB2Line> noteLines) {
        if (noteLines == null) {
            return;
        }
        if (footnotes == null) {
            footnotes = new ArrayList<FB2Line>();
        }
        footnotes.addAll(noteLines);
    }

    public int getHeight() {
        return height;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(height);
        out.writeInt(width);
        out.writeBoolean(hasNonWhiteSpaces);
        out.writeInt(elements.size());
        for (FB2LineElement element : elements) {
            element.serialize(out);
        }
    }

    public static FB2Line deserialize(DataInputStream in) throws IOException {
        FB2Line line = new FB2Line();
        line.height = in.readInt();
        line.width = in.readInt();
        line.hasNonWhiteSpaces = in.readBoolean();
        int elCount = in.readInt();
        for (int i = 0; i < elCount; i++) {
            line.append(AbstractFB2LineElement.deserialize(in));
        }
        return line;
    }
}
