package org.ebookdroid.droids.fb2.codec;

import org.emdev.common.textmarkup.JustificationMode;

public class LineCreationParams {

    public ParsedContent content;
    public JustificationMode jm;
    public int maxLineWidth;

    public boolean insertSpace = true;
    public int extraSpace = 0;

    public LineCreationParams(final ParsedContent content, final JustificationMode jm, final int maxLineWidth) {
        this.jm = jm;
        this.maxLineWidth = maxLineWidth;
        this.content = content;
    }

}
