package org.emdev.common.textmarkup.line;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;
import org.ebookdroid.droids.fb2.codec.ParsedContent;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.MarkupStream;
import org.emdev.common.textmarkup.MarkupTag;
import org.emdev.common.textmarkup.RenderingStyle;
import org.emdev.common.textmarkup.RenderingStyle.Script;
import org.emdev.common.textmarkup.RenderingStyle.Strike;
import org.emdev.common.textmarkup.text.ITextProvider;
import org.emdev.utils.bytes.ByteArray.DataArrayInputStream;

public class TextElement extends AbstractLineElement {

    static final int[] starts = new int[100];
    static final int[] lengths = new int[100];
    static final float[] parts = new float[100];

    public final ITextProvider chars;
    public final int start;
    public final int length;
    public final int offset;

    public final RenderingStyle style;

    public TextElement(final ITextProvider ch, final int st, final int len, final RenderingStyle style) {
        super(MarkupTag.TextElement, style.paint.measureText(ch.text(), st, len),
                style.script == Script.SUPER ? style.textSize * 5 / 2 : style.textSize);
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.style = style;
        this.offset = style.script == Script.SUPER ? (-style.textSize)
                : style.script == Script.SUB ? style.textSize / 2 : 0;
    }

    @Override
    public void publishToStream(final DataOutputStream out) throws IOException {
        write(out, width, height, chars, start, length, offset, style);
    }

    public static void write(final DataOutputStream out, final float width, final int height,
            final ITextProvider chars, final int start, final int length, final int offset, final RenderingStyle style)
            throws IOException {
        write(out, MarkupTag.TextElement, width, height);
        out.writeLong(chars.id());
        out.writeInt(start);
        out.writeInt(length);
        out.writeInt(offset);
        out.writeLong(style.key);
    }

    public static void addToLines(MarkupStream stream, final LineStream lines)
            throws IOException {
        final int position = stream.in.position() - 1;
        final float width = stream.in.readFloat();
        final int height = stream.in.readInt();

        stream.in.position(position + MarkupTag.TextElement.size + 1);

        AbstractLineElement.addToLines(stream, MarkupTag.TextElement, position, null, width, height, lines);
    }

    @Override
    public float render(final Canvas c, final int y, final int x, final float additionalWidth, final float left,
            final float right, final int nightmode) {
        if (left < x + width && x < right) {
            final int yy = y + offset;
            c.drawText(chars.text(), start, length, x, yy, style.paint);
            if (style.strike == Strike.THROUGH) {
                c.drawLine(x, yy - style.textSize / 4, x + width, yy - style.textSize / 4, style.paint);
                c.drawRect(x, yy - style.textSize / 4, x + width, yy - style.textSize / 4 + 1, style.paint);
            }
        }
        return width;
    }

    public static float render(ParsedContent content, DataArrayInputStream in, Canvas c, int y, int x,
            float spaceWidth, float left, float right, int nightmode) throws IOException {

        final float width = in.readFloat();
        in.skipInt();
        final long id = in.readLong();
        final int start = in.readInt();
        final int length = in.readInt();
        final int offset = in.readInt();
        final long key = in.readLong();

        final ITextProvider chars = content.getTextProvider(id);
        final RenderingStyle style = RenderingStyle.get(content, key);

        if (left < x + width && x < right) {
            final int yy = y + offset;
            c.drawText(chars.text(), start, length, x, yy, style.paint);
            if (style.strike == Strike.THROUGH) {
                c.drawLine(x, yy - style.textSize / 4, x + width, yy - style.textSize / 4, style.paint);
                c.drawRect(x, yy - style.textSize / 4, x + width, yy - style.textSize / 4 + 1, style.paint);
            }
        }
        return width;
    }

    public int indexOf(final char[] pattern) {
        int start1 = start;
        if (pattern.length > 0) {
            if (pattern.length + start1 - start > length) {
                return -1;
            }
            final char[] text = chars.text();
            while (true) {
                int i = start1;
                boolean found = false;
                for (; i < start + length; i++) {
                    if (text[i] == pattern[0]) {
                        found = true;
                        break;
                    }
                }
                if (!found || pattern.length + i - start > length) {
                    return -1; // handles subCount > count || start >= count
                }
                int o1 = i, o2 = 0;
                while (++o2 < pattern.length && text[++o1] == pattern[o2]) {
                    // Intentionally empty
                }
                if (o2 == pattern.length) {
                    return i - start;
                }
                start1 = i + 1;
            }
        }
        return (start1 < length || start1 == 0) ? start1 - start : length - start;
    }

    public int indexOfIgnoreCases(final char[] pattern) {
        int start1 = start;
        if (pattern.length > 0) {
            if (pattern.length + start1 - start > length) {
                return -1;
            }
            final char[] text = chars.text();
            while (true) {
                int i = start1;
                boolean found = false;
                for (; i < start + length; i++) {
                    if (Character.toLowerCase(text[i]) == pattern[0]) {
                        found = true;
                        break;
                    }
                }
                if (!found || pattern.length + i - start > length) {
                    return -1; // handles subCount > count || start >= count
                }
                int o1 = i, o2 = 0;
                while (++o2 < pattern.length && Character.toLowerCase(text[++o1]) == pattern[o2]) {
                    // Intentionally empty
                }
                if (o2 == pattern.length) {
                    return i - start;
                }
                start1 = i + 1;
            }
        }
        return (start1 < length || start1 == 0) ? start1 - start : length - start;
    }

    @Override
    public String toString() {
        return new String(chars.text(), start, length);
    }

    public void getTextBounds(final Rect bounds) {
        style.paint.getTextBounds(chars.text(), start, length, bounds);
    }
}
