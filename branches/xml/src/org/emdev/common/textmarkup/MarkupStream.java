package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.ParsedContent;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.RenderingStyle.Script;
import org.emdev.common.textmarkup.line.AbstractLineElement;
import org.emdev.common.textmarkup.line.LineFixedWhiteSpace;
import org.emdev.common.textmarkup.line.LineStream;
import org.emdev.common.textmarkup.line.LineWhiteSpace;
import org.emdev.common.textmarkup.line.TextElement;
import org.emdev.common.textmarkup.line.TextPreElement;
import org.emdev.common.textmarkup.text.ITextProvider;
import org.emdev.utils.bytes.ByteArray;

public class MarkupStream extends ByteArray {

    public final String name;

    private MarkupTag lastTag;

    public MarkupStream(final String name) {
        this.name = name;
    }

    public MarkupStream justify(final JustificationMode jm) {
        try {
            jm.publishToStream(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.JustificationMode;
        return this;
    }

    public int add(final IMarkupStreamCallback callback) {
        try {
            final int position = size();
            callback.write(out);
            return position;
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public int lineElement(final MarkupTag tag, final float width, final int height) {
        try {
            final int position = size();
            AbstractLineElement.write(out, tag, width, height);
            return position;
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public MarkupStream endDocument() {
        try {
            MarkupEndDocument.write(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupEndDocument;
        return this;
    }

    public MarkupStream endPage() {
        try {
            MarkupEndPage.write(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupEndPage;
        return this;
    }

    public MarkupStream extraSpace(final int extraSpace) {
        try {
            MarkupExtraSpace.write(out, extraSpace);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupExtraSpace;
        return this;
    }

    public MarkupStream imageRef(final int ref, final boolean inline) {
        try {
            MarkupImageRef.write(out, ref, inline);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupImageRef;
        return this;
    }

    public MarkupStream noSpace() {
        try {
            MarkupNoSpace.write(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupNoSpace;
        return this;
    }

    public MarkupStream noSpaceOff() {
        try {
            MarkupSpace.write(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupSpace;
        return this;
    }

    public MarkupStream note(final int ref) {
        try {
            MarkupNote.write(out, ref);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupNote;
        return this;
    }

    public MarkupStream endParagraph() {
        try {
            MarkupParagraphEnd.write(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupParagraphEnd;
        return this;
    }

    public MarkupStream title(final String title, final int level) {
        try {
            MarkupTitle.write(out, title, level);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.MarkupTitle;
        return this;
    }

    public MarkupStream horizontalRule(final float width, final int height) {
        try {
            AbstractLineElement.write(out, MarkupTag.HorizontalRule, width, height);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.HorizontalRule;
        return this;
    }

    public MarkupStream fixedWhiteSpace(final LineFixedWhiteSpace space) {
        try {
            space.publishToStream(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.LineFixedWhiteSpace;
        return this;
    }

    public MarkupStream fixedWhiteSpace(final float width, final int height) {
        try {
            AbstractLineElement.write(out, MarkupTag.LineFixedWhiteSpace, width, height);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.LineFixedWhiteSpace;
        return this;
    }

    public MarkupStream whiteSpace(final LineWhiteSpace space) {
        try {
            space.publishToStream(out);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.LineWhiteSpace;
        return this;
    }

    public MarkupStream whiteSpace(final float width, final int height) {
        try {
            AbstractLineElement.write(out, MarkupTag.LineWhiteSpace, width, height);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        lastTag = MarkupTag.LineWhiteSpace;
        return this;
    }

    public MarkupStream text(final ParsedContent content, final ITextProvider chars, final RenderingStyle style) {
        final int start = 0;
        final int length = chars.size();
        final float width = style.paint.measureText(chars.text(), start, length);
        final int height = style.script == Script.SUPER ? style.textSize * 5 / 2 : style.textSize;
        final int offset = style.script == Script.SUPER ? (-style.textSize)
                : style.script == Script.SUB ? style.textSize / 2 : 0;

        try {
            TextElement.write(out, width, height, chars, start, length, offset, style);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        content.addTextProvider(chars);

        lastTag = MarkupTag.TextElement;
        return this;
    }

    public MarkupStream textPre(final ParsedContent content, final ITextProvider chars, final int start,
            final int length, final RenderingStyle style) {
        final float width = style.paint.measureText(chars.text(), start, length);
        final int height = style.script == Script.SUPER ? style.textSize * 5 / 2 : style.textSize;
        final int offset = style.script == Script.SUPER ? (-style.textSize)
                : style.script == Script.SUB ? style.textSize / 2 : 0;

        try {
            TextPreElement.write(out, width, height, chars, start, length, offset, style);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        content.addTextProvider(chars);

        lastTag = MarkupTag.TextPreElement;
        return this;
    }

    public MarkupStream text(final ParsedContent content, final ITextProvider chars, final int start, final int length,
            final RenderingStyle style) {
        final float width = style.paint.measureText(chars.text(), start, length);
        final int height = style.script == Script.SUPER ? style.textSize * 5 / 2 : style.textSize;
        final int offset = style.script == Script.SUPER ? (-style.textSize)
                : style.script == Script.SUB ? style.textSize / 2 : 0;

        try {
            TextElement.write(out, width, height, chars, start, length, offset, style);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        content.addTextProvider(chars);

        lastTag = MarkupTag.TextElement;
        return this;
    }

    public int text(final ITextProvider chars, final float width, final int height, final int start, final int length,
            final int offset, final RenderingStyle style) {
        final int position = size();

        try {
            TextElement.write(out, width, height, chars, start, length, offset, style);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }

        lastTag = MarkupTag.TextElement;
        return position;
    }

    public void clear() {
    }

    public MarkupTag lastTag() {
        return lastTag;
    }

    public boolean isEmpty() {
        return lastTag == null;
    }

    public static boolean isNotEmpty(final MarkupStream s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isEmpty(final MarkupStream s) {
        return s == null || s.isEmpty();
    }

    public void publishToLines(final LineStream lines) throws IOException {

        while (in.available() > 0) {
            final int ord = in.readByte() & 0x7F;
            final MarkupTag tag = MarkupTag.values()[ord];
            switch (tag) {
                case MarkupEndDocument:
                    return;
                case JustificationMode:
                    JustificationMode.addToLines(this, lines);
                    break;
                case MarkupEndPage:
                    MarkupEndPage.E.publishToLines(this, lines);
                    break;
                case MarkupExtraSpace:
                    MarkupExtraSpace.addToLines(this, lines);
                    break;
                case MarkupImageRef:
                    MarkupImageRef.addToLines(this, lines);
                    break;
                case MarkupNoSpace:
                    MarkupNoSpace.E.publishToLines(this, lines);
                    break;
                case MarkupSpace:
                    MarkupSpace.E.publishToLines(this, lines);
                    break;
                case MarkupNote:
                    MarkupNote.addToLines(this, lines);
                    break;
                case MarkupParagraphEnd:
                    MarkupParagraphEnd.E.publishToLines(this, lines);
                    break;
                case MarkupTitle:
                    MarkupTitle.addToLines(this, lines);
                    break;
                case LineFixedWhiteSpace:
                    LineFixedWhiteSpace.addToLines(this, lines);
                    break;
                case LineWhiteSpace:
                    LineWhiteSpace.addToLines(this, lines);
                    break;
                case TextElement:
                    TextElement.addToLines(this, lines);
                    break;
                case TextPreElement:
                    TextPreElement.addToLines(this, lines);
                    break;
            }
        }

    }

    public static interface IMarkupStreamCallback {

        void write(DataOutputStream out) throws IOException;
    }
}
