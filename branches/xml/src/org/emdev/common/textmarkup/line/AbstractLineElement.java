package org.emdev.common.textmarkup.line;

import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.droids.fb2.codec.ParsedContent;

import android.graphics.RectF;

import java.io.DataOutputStream;
import java.io.IOException;

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

    public static void write(final DataOutputStream out, final MarkupTag tag, final float width, final int height)
            throws IOException {
        out.writeByte(tag.ordinal());
        out.writeFloat(width);
        out.writeInt(height);
    }

    @Override
    public final void publishToLines(final MarkupStream stream, final LineStream lines) {
        throw new RuntimeException("Cannot be called");
    }

    public static final void addToLines(final MarkupStream stream, final MarkupTag tag, final int position,
            final IMarkupStreamCallback callback, final float ewidth, final int eheight, final LineStream lines)
            throws IOException {

        Line line = lines.last(stream);
        final LineWhiteSpace space = RenderingStyle.getTextPaint(lines.params.content,
                Math.max(line.getHeight(), eheight)).space;
        float remaining = lines.params.maxLineWidth - (line.width + space.width);
        if (remaining <= 0) {
            line = lines.add(stream);
            remaining = lines.params.maxLineWidth;
        }
        if (lines.params.extraSpace > 0 && line.elements.size() == 0) {
            line.append(MarkupTag.LineFixedWhiteSpace, lines.params.extraSpace, 0);
        }

        if (ewidth <= remaining) {
            if (line.hasNonWhiteSpaces() && lines.params.insertSpace) {
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
                splitted = split(stream, lines, line, space, position, remaining);
            } else if (tag == MarkupTag.TextPreElement && AppSettings.current().fb2HyphenEnabled) {
                splitted = splitPre(stream, lines, line, space, position, remaining);
            }
            if (!splitted) {
                line = lines.add(stream);
                if (lines.params.extraSpace > 0 && line.elements.size() == 0) {
                    line.append(MarkupTag.LineFixedWhiteSpace, lines.params.extraSpace, 0);
                }
                if (position >= 0) {
                    line.append(tag, ewidth, eheight, position, false);
                } else {
                    line.append(tag, ewidth, eheight, callback);
                }
            }

        }
        lines.params.insertSpace = true;
    }

    public static boolean split(final MarkupStream stream, final LineStream lines, final Line line,
            final LineWhiteSpace space, final int position, final float remaining) throws IOException {

        final ParsedContent content = lines.params.content;
        final DataArrayInputStream in = stream.in;
        final int prev = in.position(position + 1);

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

            final int count = HyphenationUtils.hyphenateWord(chars.text(), start, length, TextElement.starts,
                    TextElement.lengths);
            if (count == 0) {
                return false;
            }

            final float dwidth = style.defis.width;
            final int firstStart = start;
            int firstLen = 0;

            float summ = dwidth;
            int next = 0;

            for (; next < TextElement.parts.length; next++) {
                final float width = style.paint.measureText(chars.text(), TextElement.starts[next],
                        TextElement.lengths[next]);
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

            if (line.hasNonWhiteSpaces() && lines.params.insertSpace) {
                line.append(MarkupTag.LineWhiteSpace, space.width, space.height);
            }

            line.addText(chars, summ - dwidth, eheight, firstStart, firstLen, offset, style);

            content.addTextProvider(style.defis.chars);
            line.addText(style.defis.chars, style.defis.width, style.defis.height, style.defis.start,
                    style.defis.length, style.defis.offset, style.defis.style);

            final Line newline = lines.add(stream);
            if (lines.params.extraSpace > 0 && line.elements.size() == 0) {
                newline.append(MarkupTag.LineFixedWhiteSpace, lines.params.extraSpace, 0);
            }

            newline.addText(chars, ewidth - (summ - dwidth), eheight, secondStart, secondLength, offset, style);

            return true;
        } finally {
            in.position(prev);
        }
    }

    public static boolean splitPre(final MarkupStream stream, final LineStream lines, final Line line,
            final LineWhiteSpace space, final int position, final float remaining) throws IOException {

        final ParsedContent content = lines.params.content;
        final DataArrayInputStream in = stream.in;
        final int prev = in.position(position + 1);

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

            if (line.hasNonWhiteSpaces() && lines.params.insertSpace) {
                line.append(MarkupTag.LineWhiteSpace, space.width, space.height);
            }

            line.addText(chars, summ, eheight, firstStart, firstLen, offset, style);

            final Line newline = lines.add(stream);
            if (lines.params.extraSpace > 0 && line.elements.size() == 0) {
                newline.append(MarkupTag.LineFixedWhiteSpace, lines.params.extraSpace, 0);
            }

            newline.addText(chars, ewidth - summ, eheight, secondStart, secondLength, offset, style);

            return true;
        } finally {
            in.position(prev);
        }
    }

}
