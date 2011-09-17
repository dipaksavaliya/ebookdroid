package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.fb2droid.codec.FB2Document.JustificationMode;

import android.graphics.Canvas;

import java.util.ArrayList;

public class FB2Line {

    private final ArrayList<FB2LineElement> elements = new ArrayList<FB2LineElement>();
    private int height;
    private boolean hasNonWhiteSpaces = false;

    public FB2Line() {
    }

    public void append(FB2LineElement element) {
        elements.add(element);
        if (element.getHeight() > height) {
            height = element.getHeight();
        }
        if (!(element instanceof FB2LineWhiteSpace)) {
            hasNonWhiteSpaces = true;
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        int x = 0;
        for (FB2LineElement e : elements) {
            x += e.getWidth();
        }
        return x;
    }

    public void render(Canvas c, int y) {
        int x = FB2Page.MARGIN_X;
        for (FB2LineElement e : elements) {
            e.render(c, y, x);
            x += e.getWidth();
        }
    }

    public static FB2Line getLastLine(ArrayList<FB2Line> lines) {
        if (lines.size() == 0) {
            lines.add(new FB2Line());
        }
        return lines.get(lines.size() - 1);
    }

    public void applyJustification(JustificationMode jm) {
        switch (jm) {
            case Center:
                int x = (FB2Page.PAGE_WIDTH - (getWidth() + 2 * FB2Page.MARGIN_X)) / 2;
                elements.add(0, new FB2LineWhiteSpace(x, height, true));
                elements.add(new FB2LineWhiteSpace(x, height, true));
                break;
            case Left:
                break;
            case Justify:
                int ws = 0;
                for (FB2LineElement e : elements) {
                    if (e instanceof FB2LineWhiteSpace && e.isSizeable()) {
                        ws++;
                    }
                }
                if (ws > 0) {
                    int wsx = (FB2Page.PAGE_WIDTH - (getWidth() + 2 * FB2Page.MARGIN_X)) / ws;
                    for (FB2LineElement e : elements) {
                        if (e instanceof FB2LineWhiteSpace && e.isSizeable()) {
                            e.adjustWidth(wsx);
                        }
                    }
                }
                break;
            case Right:
                int x1 = (FB2Page.PAGE_WIDTH - (getWidth() + 2 * FB2Page.MARGIN_X));
                elements.add(0, new FB2LineWhiteSpace(x1, height, true));
                break;
        }
    }

    public boolean hasNonWhiteSpaces() {
        return hasNonWhiteSpaces ;
    }
}
