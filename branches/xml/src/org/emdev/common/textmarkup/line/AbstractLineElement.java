package org.emdev.common.textmarkup.line;

import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.droids.fb2.codec.LineCreationParams;
import org.ebookdroid.droids.fb2.codec.ParsedContent;

import android.graphics.RectF;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.MarkupStream;
import org.emdev.common.textmarkup.MarkupStream.IMarkupStreamCallback;
import org.emdev.common.textmarkup.MarkupTag;
import org.emdev.common.textmarkup.RenderingStyle;
import org.emdev.common.textmarkup.text.ITextProvider;
import org.emdev.utils.HyphenationUtils;
import org.emdev.utils.bytes.ByteArray.DataArrayInputStream;

public abstract class AbstractLineElement implements LineElement {

    public final MarkupTag tag;
    public final int height;
    public float width;

    public AbstractLineElement(final MarkupTag tag, final float width, final int height) {
        this.tag = tag;
        this.height = height;
        this.width = width;
    }

    public AbstractLineElement(final MarkupTag tag, final RectF rect) {
        this(tag, rect.width(), (int) rect.height());
    }

    public static void write(DataOutputStream out, MarkupTag tag, float width, int height) throws IOException {
        out.writeByte(tag.ordinal());
        out.writeFloat(width);
        out.writeInt(height);
    }

    @Override
    public final void publishToLines(MarkupStream stream, ArrayList<Line> lines, LineCreationParams params) {
        throw new RuntimeException("Cannot be called");
    }

    public static final void addToLines(MarkupStream stream, MarkupTag tag, int position, IMarkupStreamCallback callback, float ewidth,
            int eheight, ArrayList<Line> lines, LineCreationParams params) throws IOException {
        Line line = Line.getLastLine(params.content, stream, lines, params);
        final LineWhiteSpace space = RenderingStyle.getTextPaint(params.content, Math.max(line.getHeight(), eheight)).space;
        float remaining = params.maxLineWidth - (line.width + space.width);
        if (remaining <= 0) {
            line = new Line(params.content, stream, params.maxLineWidth, params.jm);
            lines.add(line);
            remaining = params.maxLineWidth;
        }
        if (params.extraSpace > 0 && line.elements.size() == 0) {
            line.append(MarkupTag.LineFixedWhiteSpace, params.extraSpace, 0);
        }

        if (ewidth <= remaining) {
            if (line.hasNonWhiteSpaces() && params.insertSpace) {
                line.append(MarkupTag.LineWhiteSpace, space.width, space.height);
            }
            if (position >= 0) {
                line.append(tag, ewidth, eheight, position, false);
            } else {
                line.append(tag, ewidth, eheight, callback);
            }
        } else {
            boolean splitted = false;
            if (tag == MarkupTag.TextElement && AppSettings.current().fb2HyphenEnabled) {
                splitted = split(stream, params, lines, line, space, position, remaining);
            } else if (tag == MarkupTag.TextPreElement && AppSettings.current().fb2HyphenEnabled) {
                splitted = splitPre(stream, params, lines, line, space, position, remaining);
            }
            if (!splitted) {
                line = new Line(params.content, stream, params.maxLineWidth, params.jm);
                if (params.extraSpace > 0 && line.elements.size() == 0) {
                    line.append(MarkupTag.LineFixedWhiteSpace, params.extraSpace, 0);
                }
                lines.add(line);
                if (position >= 0) {
                    line.append(tag, ewidth, eheight, position, false);
                } else {
                    line.append(tag, ewidth, eheight, callback);
                }
            }

        }
        params.insertSpace = true;
    }

    public static boolean split(MarkupStream stream, LineCreationParams params, ArrayList<Line> lines, Line line, final LineWhiteSpace space, int position, final float remaining) throws IOException {
        ParsedContent content = params.content;
        DataArrayInputStream in = stream.in;
        int prev = in.position(position + 1);

        try {
            final float ewidth = in.readFloat();
            final int eheight = in.readInt();
            final long id = in.readLong();
            final int start = in.readInt();
            final int length = in.readInt();
            final int offset = in.readInt();
            final long key = in.readLong();

            final ITextProvider chars = content.getTextProvider(id);
            final RenderingStyle style = RenderingStyle.get(content, key);

            final int count = HyphenationUtils.hyphenateWord(chars.text(), start, length, TextElement.starts, TextElement.lengths);
            if (count == 0) {
                return false;
            }

            final float dwidth = style.defis.width;
            final int firstStart = start;
            int firstLen = 0;

            float summ = dwidth;
            int next = 0;

            for (; next < TextElement.parts.length; next++) {
                final float width = style.paint.measureText(chars.text(), TextElement.starts[next], TextElement.lengths[next]);
                final float total = summ + width;
                if (total > remaining) {
                    break;
                }
                summ = total;
                firstLen += TextElement.lengths[next];
            }

            if (next == 0) {
                return false;
            }

            final int secondStart = TextElement.starts[next];
            final int secondLength = length - (TextElement.starts[next] - start);

            if (line.hasNonWhiteSpaces() && params.insertSpace) {
                line.append(MarkupTag.LineWhiteSpace, space.width, space.height);
            }

            line.addText(chars, summ - dwidth, eheight, firstStart, firstLen, offset, style);

            content.addTextProvider(style.defis.chars);
            line.addText(style.defis.chars, style.defis.width, style.defis.height, style.defis.start, style.defis.length, style.defis.offset, style.defis.style);

            Line newline = new Line(params.content, stream, params.maxLineWidth, params.jm);
            if (params.extraSpace > 0 && line.elements.size() == 0) {
                newline.append(MarkupTag.LineFixedWhiteSpace, params.extraSpace, 0);
            }
            lines.add(newline);

            newline.addText(chars, ewidth - (summ - dwidth), eheight, secondStart, secondLength, offset, style);

            return true;
        } finally {
            in.position(prev);
        }
    }

    public static boolean splitPre(MarkupStream stream, LineCreationParams params, ArrayList<Line> lines, Line line, final LineWhiteSpace space, int position, final float remaining) throws IOException {
        ParsedContent content = params.content;
        DataArrayInputStream in = stream.in;
        int prev = in.position(position + 1);

        try {
            final float ewidth = in.readFloat();
            final int eheight = in.readInt();
            final long id = in.readLong();
            final int start = in.readInt();
            final int length = in.readInt();
            final int offset = in.readInt();
            final long key = in.readLong();

            final ITextProvider chars = content.getTextProvider(id);
            final RenderingStyle style = RenderingStyle.get(content, key);

            final int firstStart = start;
            int firstLen = 0;
            int secondStart = start;

            float summ = 0;

            for (int i = start; i < start + length; i++) {
                final float width = style.paint.measureText(chars.text(), i, 1);
                final float total = summ + width;
                if (total > remaining) {
                    break;
                }
                summ = total;
                firstLen++;
                secondStart++;
            }

            if (secondStart == firstStart) {
                return false;
            }

            final int secondLength = length - firstLen;

            if (line.hasNonWhiteSpaces() && params.insertSpace) {
                line.append(MarkupTag.LineWhiteSpace, space.width, space.height);
            }

            line.addText(chars, summ, eheight, firstStart, firstLen, offset, style);

            Line newline = new Line(params.content, stream, params.maxLineWidth, params.jm);
            if (params.extraSpace > 0 && line.elements.size() == 0) {
                newline.append(MarkupTag.LineFixedWhiteSpace, params.extraSpace, 0);
            }
            lines.add(newline);

            newline.addText(chars, ewidth - summ, eheight, secondStart, secondLength, offset, style);

            return true;
        } finally {
            in.position(prev);
        }
    }

}
