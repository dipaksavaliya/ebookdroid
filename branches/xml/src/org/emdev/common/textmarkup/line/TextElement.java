package org.emdev.common.textmarkup.line;

import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.MarkupTag;
import org.emdev.common.textmarkup.RenderingStyle;
import org.emdev.common.textmarkup.RenderingStyle.Script;
import org.emdev.common.textmarkup.RenderingStyle.Strike;
import org.emdev.common.textmarkup.text.ITextProvider;
import org.emdev.utils.HyphenationUtils;

public class TextElement extends AbstractLineElement {

    private static final int[] starts = new int[100];
    private static final int[] lengths = new int[100];
    private static final float[] parts = new float[100];

    public final ITextProvider chars;
    public final int start;
    public final int length;
    public final int offset;

    public final RenderingStyle style;

    public TextElement(final ITextProvider ch, final RenderingStyle style) {
        super(style.paint.measureText(ch.text(), 0, ch.size()), style.script == Script.SUPER ? style.textSize * 5 / 2
                : style.textSize);
        this.chars = ch;
        this.start = 0;
        this.length = ch.size();
        this.style = style;
        this.offset = style.script == Script.SUPER ? (-style.textSize)
                : style.script == Script.SUB ? style.textSize / 2 : 0;
    }

    public TextElement(final ITextProvider ch, final int st, final int len, final RenderingStyle style) {
        super(style.paint.measureText(ch.text(), st, len), style.script == Script.SUPER ? style.textSize * 5 / 2
                : style.textSize);
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.style = style;
        this.offset = style.script == Script.SUPER ? (-style.textSize)
                : style.script == Script.SUB ? style.textSize / 2 : 0;
    }

    TextElement(final TextElement original, final int st, final int len, final float width) {
        super(width, original.style.textSize);
        this.chars = original.chars;
        this.start = st;
        this.length = len;
        this.style = original.style;
        this.offset = original.offset;
    }

    public TextElement(ITextProvider p, float width, int height, int start2, int length2, int offset2, RenderingStyle style) {
        super(width, height);
        this.chars = p;
        this.start = start2;
        this.length = length2;
        this.offset = offset2;
        this.style= style;
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

    public static void publishToLines(DataInputStream in, ArrayList<Line> lines, LineCreationParams params) throws IOException {
        float width = in.readFloat();
        int height = in.readInt();
        long id = in.readLong();
        int start = in.readInt();
        int length = in.readInt();
        int offset = in.readInt();
        long key = in.readLong();

        ITextProvider p = params.content.getTextProvider(id);
        RenderingStyle style = RenderingStyle.get(params.content, key);
        new TextElement(p, width, height, start, length, offset, style).publishToLines(lines, params);

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

    @Override
    public AbstractLineElement[] split(final float remaining) {
        if (!AppSettings.current().fb2HyphenEnabled) {
            return null;
        }
        final int count = HyphenationUtils.hyphenateWord(chars.text(), start, length, starts, lengths);
        if (count == 0) {
            return null;
        }

        final float dwidth = this.style.defis.width;
        final int firstStart = this.start;
        int firstLen = 0;

        float summ = dwidth;
        int next = 0;

        for (; next < parts.length; next++) {
            final float width = style.paint.measureText(chars.text(), starts[next], lengths[next]);
            final float total = summ + width;
            if (total > remaining) {
                break;
            }
            summ = total;
            firstLen += lengths[next];
        }

        if (next == 0) {
            return null;
        }

        final int secondStart = starts[next];
        final int secondLength = this.length - (starts[next] - this.start);

        final TextElement first = new TextElement(this, firstStart, firstLen, summ - dwidth);
        final TextElement second = new TextElement(this, secondStart, secondLength, this.width - first.width);

        final AbstractLineElement[] result = { first, this.style.defis, second };
        return result;
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
