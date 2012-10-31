package org.emdev.common.textmarkup.line;

import org.ebookdroid.droids.fb2.codec.FB2Page;
import org.ebookdroid.droids.fb2.codec.LineCreationParams;
import org.ebookdroid.droids.fb2.codec.ParsedContent;

import android.graphics.Canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.emdev.common.textmarkup.JustificationMode;
import org.emdev.common.textmarkup.MarkupStream;
import org.emdev.common.textmarkup.MarkupStream.IMarkupStreamCallback;
import org.emdev.common.textmarkup.MarkupTag;
import org.emdev.common.textmarkup.MarkupTitle;
import org.emdev.common.textmarkup.RenderingStyle;
import org.emdev.common.textmarkup.TextStyle;
import org.emdev.common.textmarkup.text.ITextProvider;
import org.emdev.utils.LengthUtils;
import org.emdev.utils.bytes.ByteArray.DataArrayInputStream;

import com.ximpleware.FastIntBuffer;

public class Line {

    private int height;
    float width = 0;
    private boolean hasNonWhiteSpaces = false;
    private List<Line> footnotes;
    public boolean committed;
    private int sizeableCount;
    public float spaceWidth;
    private boolean justified;
    private JustificationMode justification = JustificationMode.Justify;
    private MarkupTitle title;
    private final int maxLineWidth;
    private volatile boolean recycled;

    private final ParsedContent content;
    private final MarkupStream stream;
    public final FastIntBuffer elements = new FastIntBuffer(5);
    private LineFixedWhiteSpace prefix;

    public Line(final ParsedContent content, final MarkupStream stream, final int lineWidth, final JustificationMode jm) {
        this.content = content;
        this.stream = stream;
        this.maxLineWidth = lineWidth;
        justification = jm;
    }

    public void recycle() {
        recycled = true;
        if (footnotes != null) {
            for (final Line l : footnotes) {
                l.recycle();
            }
            footnotes.clear();
            footnotes = null;
        }
    }

    public Line addText(final ITextProvider chars, final float width, final int height, final int start,
            final int length, final int offset, final RenderingStyle style) {

        final int position = content.postMarkup.text(chars, width, height, start, length, offset, style);
        if (position != -1) {
            append(MarkupTag.TextElement, width, height, position, true);
        }
        return this;
    }

    public Line append(final MarkupTag tag, final float ewidth, final int eheight, final IMarkupStreamCallback callback) {
        final int position = content.postMarkup.add(callback);
        if (position != -1) {
            append(tag, ewidth, eheight, position, true);
        }
        return this;
    }

    public Line append(final MarkupTag tag, final float ewidth, final int eheight) {
        final int position = content.postMarkup.lineElement(tag, ewidth, eheight);
        if (position != -1) {
            append(tag, ewidth, eheight, position, true);
        }
        return this;
    }

    public Line append(final MarkupTag tag, final float ewidth, final int eheight, final int position) {
        return append(tag, ewidth, eheight, position, false);
    }

    public Line append(final MarkupTag tag, final float ewidth, final int eheight, final int position,
            final boolean internal) {
        final int ordinal = tag.ordinal();
        elements.append(ordinal | (internal ? 0x80 : 0x00));
        elements.append(position);

        if (eheight > height) {
            height = eheight;
        }
        if (tag == MarkupTag.LineFixedWhiteSpace) {
            // Do nothing
        } else if (tag == MarkupTag.LineWhiteSpace) {
            sizeableCount++;
        } else {
            hasNonWhiteSpaces = true;
        }
        width += ewidth;
        return this;
    }

    public int getTotalHeight() {
        int h = height;
        for (int i = 0, n = Math.min(2, LengthUtils.length(footnotes)); i < n; i++) {
            final Line line = footnotes.get(i);
            h += line.height;
        }
        return h;
    }

    public void render(final Canvas c, final int x, final int y, final float left, final float right,
            final int nightmode) {
        ensureJustification();
        float x1 = x;

        if (prefix != null) {
            x1 += prefix.width;
        }

        for (int i = 0, n = elements.size(); i < n && !recycled;) {
            final int ttag = elements.intAt(i++);
            final boolean internal = (ttag & 0x80) != 0;
            final MarkupTag tag = MarkupTag.values()[ttag & 0x7F];
            final int position = elements.intAt(i++);

            try {
                final DataArrayInputStream in = internal ? content.postMarkup.in : stream.in;
                in.position(position + 1);
                x1 = renderTag(tag, in, c, y, left, right, nightmode, x1);
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public float renderTag(final MarkupTag tag, final DataArrayInputStream in, final Canvas c, final int y,
            final float left, final float right, final int nightmode, float x1) throws IOException {
        switch (tag) {
            case HorizontalRule:
                x1 += HorizontalRule.render(in, c, y, (int) x1, spaceWidth, left, right, nightmode);
                break;
            case Image:
                x1 += Image.render(content, in, c, y, (int) x1, spaceWidth, left, right, nightmode);
                break;
            case LineFixedWhiteSpace: {
                final float width = in.readFloat();
                final int height = in.readInt();
                x1 += width;
            }
                break;
            case LineWhiteSpace: {
                final float width = in.readFloat();
                final int height = in.readInt();
                x1 += width + spaceWidth;
            }
                break;
            case TextElement:
                x1 += TextElement.render(content, in, c, y, (int) x1, spaceWidth, left, right, nightmode);
                break;
            case TextPreElement:
                x1 += TextPreElement.render(content, in, c, y, (int) x1, spaceWidth, left, right, nightmode);
                break;
        }
        return x1;
    }

    public static Line getLastLine(final ParsedContent content, final MarkupStream stream, final ArrayList<Line> lines,
            final LineCreationParams params) {
        if (lines.size() == 0) {
            lines.add(new Line(content, stream, params.maxLineWidth, params.jm));
        }
        Line fb2Line = lines.get(lines.size() - 1);
        if (fb2Line.committed) {
            fb2Line = new Line(content, stream, params.maxLineWidth, params.jm);
            lines.add(fb2Line);
        }
        return fb2Line;
    }

    public void ensureJustification() {
        if (!justified) {
            switch (justification) {
                case Center:
                    final float x = (maxLineWidth - (width)) / 2;
                    prefix = new LineFixedWhiteSpace(x, height);
                    break;
                case Left:
                    break;
                case Justify:
                    if (sizeableCount > 0) {
                        spaceWidth = (maxLineWidth - (width)) / sizeableCount;
                    } else {
                        spaceWidth = 0;
                    }
                    break;
                case Right:
                    final float x1 = (maxLineWidth - (width));
                    prefix = new LineFixedWhiteSpace(x1, height);
                    break;
            }
            justified = true;
        }
    }

    public boolean hasNonWhiteSpaces() {
        return hasNonWhiteSpaces;
    }

    public List<Line> getFootNotes() {
        return footnotes;
    }

    public void addNote(final MarkupStream stream, final List<Line> noteLines) {
        if (noteLines == null) {
            return;
        }
        if (footnotes == null) {
            footnotes = new ArrayList<Line>();
            final Line lastLine = new Line(content, stream, FB2Page.PAGE_WIDTH / 4, justification);
            footnotes.add(lastLine);
            lastLine.append(MarkupTag.HorizontalRule, FB2Page.PAGE_WIDTH / 4, TextStyle.FOOTNOTE.getFontSize());
            lastLine.applyJustification(JustificationMode.Left);
        }
        footnotes.addAll(noteLines);
    }

    public void applyJustification(final JustificationMode jm) {
        if (committed) {
            return;
        }
        justification = jm;
        committed = true;
    }

    public int getHeight() {
        return height;
    }

    public boolean appendable() {
        return true;
    }

    public void setTitle(final MarkupTitle fb2MarkupTitle) {
        this.title = fb2MarkupTitle;
    }

    public MarkupTitle getTitle() {
        return this.title;
    }

    public boolean isEmpty() {
        if (elements.size() == 0) {
            return true;
        }
        return !hasNonWhiteSpaces;
    }

}
