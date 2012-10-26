package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.ParsedContent;

import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;

import org.emdev.common.fonts.data.FontStyle;
import org.emdev.common.fonts.typeface.TypefaceEx;
import org.emdev.common.textmarkup.line.TextElement;
import org.emdev.common.textmarkup.text.TextProvider;

public class RenderingStyle {

    private static final TextProvider STP = new TextProvider('-');

    private static final LongSparseArray<RenderingStyle> styles = new LongSparseArray<RenderingStyle>();
    private static final SparseArray<CustomTextPaint> paints = new SparseArray<CustomTextPaint>();

    public final CustomTextPaint paint;

    public final TextElement defis;

    public final long key;
    public final int textSize;
    public final JustificationMode jm;
    public final TypefaceEx face;
    public final Script script;
    public final Strike strike;

    public static RenderingStyle get(final ParsedContent content, final TextStyle text) {
        final TypefaceEx face = content.fonts[FontStyle.REGULAR.ordinal()];
        final CustomTextPaint paint = getTextPaint(face, text.getFontSize());

        final long key = paint.key + (JustificationMode.Justify.ordinal() << 32);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(content, text);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final ParsedContent content, final TextStyle text) {
        this.textSize = text.getFontSize();
        this.jm = JustificationMode.Justify;
        this.face = content.fonts[FontStyle.REGULAR.ordinal()];
        this.paint = getTextPaint(face, this.textSize);
        this.script = Script.NONE;
        this.strike = Strike.NONE;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32) + (script.ordinal() << 34) + (strike.ordinal() << 36);
    }

    public static RenderingStyle get(final RenderingStyle old, final Script script) {
        final int textSize = ((script == Script.NONE) || (script != Script.NONE && old.script != Script.NONE)) ? old.textSize
                : old.textSize / 2;
        final CustomTextPaint paint = getTextPaint(old.face, textSize);

        final long key = paint.key + (old.jm.ordinal() << 32) + (script.ordinal() << 34);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(old, script, textSize);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final RenderingStyle old, final Script script, final int textSize) {
        this.textSize = textSize;
        this.jm = old.jm;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize);
        this.script = script;
        this.strike = Strike.NONE;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32) + (script.ordinal() << 34) + (strike.ordinal() << 36);
    }

    public static RenderingStyle get(final RenderingStyle old, final Strike strike) {
        final int textSize = old.textSize;
        final CustomTextPaint paint = getTextPaint(old.face, textSize);

        final long key = paint.key + (old.jm.ordinal() << 32) + (old.script.ordinal() << 34) + (strike.ordinal() << 36);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(old, strike);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final RenderingStyle old, final Strike strike) {
        this.textSize = old.textSize;
        this.jm = old.jm;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize);
        this.script = old.script;
        this.strike = strike;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32) + (script.ordinal() << 34) + (strike.ordinal() << 36);
    }

    public static RenderingStyle get(final RenderingStyle old, final TextStyle text, final JustificationMode jm) {
        final CustomTextPaint paint = getTextPaint(old.face, text.getFontSize());

        final long key = paint.key + (jm.ordinal() << 32);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(old, text, jm);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final RenderingStyle old, final TextStyle text, final JustificationMode jm) {
        this.textSize = text.getFontSize();
        this.jm = jm;
        this.face = old.face;
        this.paint = getTextPaint(face, textSize);
        this.script = Script.NONE;
        this.strike = Strike.NONE;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32);
    }

    public static RenderingStyle get(final ParsedContent content, final RenderingStyle old, final TextStyle text,
            final JustificationMode jm, final FontStyle fstyle) {
        final CustomTextPaint paint = getTextPaint(content.fonts[fstyle.ordinal()], text.getFontSize());

        final long key = paint.key + (jm.ordinal() << 32);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(content, old, text, jm, fstyle);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final ParsedContent content, final RenderingStyle old, final TextStyle text,
            final JustificationMode jm, final FontStyle style) {
        this.textSize = text.getFontSize();
        this.jm = jm;
        this.face = content.fonts[style.ordinal()];
        this.paint = getTextPaint(face, textSize);
        this.script = Script.NONE;
        this.strike = Strike.NONE;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32);
    }

    public static RenderingStyle get(final ParsedContent content, final RenderingStyle old, final JustificationMode jm,
            final FontStyle fstyle) {
        final CustomTextPaint paint = getTextPaint(content.fonts[fstyle.ordinal()], old.textSize);

        final long key = paint.key + (jm.ordinal() << 32);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(content, old, jm, fstyle);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final ParsedContent content, final RenderingStyle old, final JustificationMode jm,
            final FontStyle style) {
        this.textSize = old.textSize;
        this.jm = jm;
        this.face = content.fonts[style.ordinal()];
        this.paint = getTextPaint(face, textSize);
        this.script = Script.NONE;
        this.strike = Strike.NONE;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32) + (script.ordinal() << 34) + (strike.ordinal() << 36);
    }

    public static RenderingStyle get(final ParsedContent content, final RenderingStyle old, final boolean bold) {
        final TypefaceEx face = content.fonts[(bold ? old.face.style.getBold() : old.face.style.getBase()).ordinal()];
        final CustomTextPaint paint = getTextPaint(face, old.textSize);

        final long key = paint.key + (old.jm.ordinal() << 32);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(content, old, bold);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final ParsedContent content, final RenderingStyle old, final boolean bold) {
        this.textSize = old.textSize;
        this.jm = old.jm;
        this.face = content.fonts[(bold ? old.face.style.getBold() : old.face.style.getBase()).ordinal()];
        this.paint = getTextPaint(face, textSize);
        this.script = Script.NONE;
        this.strike = Strike.NONE;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32) + (script.ordinal() << 34) + (strike.ordinal() << 36);
    }

    public static RenderingStyle get(final ParsedContent content, final RenderingStyle old, final FontStyle fstyle) {
        final CustomTextPaint paint = getTextPaint(content.fonts[fstyle.ordinal()], old.textSize);

        final long key = paint.key + (old.jm.ordinal() << 32);
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(content, old, fstyle);
            styles.put(key, style);
        }
        return style;
    }

    private RenderingStyle(final ParsedContent content, final RenderingStyle old, final FontStyle style) {
        this.textSize = old.textSize;
        this.jm = old.jm;
        this.face = content.fonts[style.ordinal()];
        this.paint = getTextPaint(face, textSize);
        this.script = Script.NONE;
        this.strike = Strike.NONE;

        this.defis = new TextElement(STP, 0, 1, this);
        this.key = paint.key + (jm.ordinal() << 32) + (script.ordinal() << 34) + (strike.ordinal() << 36);
    }

    public static RenderingStyle get(ParsedContent content, long key) {
        RenderingStyle style = styles.get(key, null);
        if (style == null) {
            style = new RenderingStyle(content, TextStyle.TEXT);
        }
        return style;
    }

    public static CustomTextPaint getTextPaint(final ParsedContent content, final int textSize) {
        final TypefaceEx tf = content.fonts[FontStyle.REGULAR.ordinal()];
        return getTextPaint(tf, textSize);
    }

    public static final CustomTextPaint getTextPaint(final TypefaceEx face, final int textSize) {
        final int key = (face.id & 0x0000FFFF) + ((textSize & 0x0000FFFF) << 16);
        CustomTextPaint paint = paints.get(key);
        if (paint == null) {
            paint = new CustomTextPaint(key, face, textSize);
            paints.append(key, paint);
        }
        return paint;
    }

    public static enum Script {
        NONE(1), SUB(2), SUPER(2);

        public final int factor;

        private Script(final int factor) {
            this.factor = factor;
        }
    }

    public static enum Strike {
        NONE, THROUGH, UNDER;
    }
}
