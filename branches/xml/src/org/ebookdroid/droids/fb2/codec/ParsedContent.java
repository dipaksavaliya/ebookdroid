package org.ebookdroid.droids.fb2.codec;

import static org.ebookdroid.droids.fb2.codec.FB2Page.MARGIN_X;
import static org.ebookdroid.droids.fb2.codec.FB2Page.PAGE_WIDTH;

import org.ebookdroid.common.settings.AppSettings;

import android.graphics.Bitmap;
import android.support.v4.util.LongSparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.emdev.common.fonts.FontManager;
import org.emdev.common.fonts.SystemFontProvider;
import org.emdev.common.fonts.data.FontFamilyType;
import org.emdev.common.fonts.data.FontStyle;
import org.emdev.common.fonts.typeface.TypefaceEx;
import org.emdev.common.textmarkup.JustificationMode;
import org.emdev.common.textmarkup.MarkupStream;
import org.emdev.common.textmarkup.RenderingStyle;
import org.emdev.common.textmarkup.TextStyle;
import org.emdev.common.textmarkup.image.DiskImageData;
import org.emdev.common.textmarkup.image.IImageData;
import org.emdev.common.textmarkup.image.MemoryImageData;
import org.emdev.common.textmarkup.line.Image;
import org.emdev.common.textmarkup.line.LineStream;
import org.emdev.common.textmarkup.text.ITextProvider;

public class ParsedContent {

    final MarkupStream docMarkup = new MarkupStream(null);
    public final MarkupStream postMarkup = new MarkupStream(null);

    final HashMap<String, MarkupStream> streams = new HashMap<String, MarkupStream>();

    private final TreeMap<String, Integer> imageRefs = new TreeMap<String, Integer>();
    private final ArrayList<Image> standaloneImages = new ArrayList<Image>();
    private final ArrayList<Image> inlineImages = new ArrayList<Image>();

    private final TreeMap<Integer, String> noteNames = new TreeMap<Integer, String>();
    private final TreeMap<String, Integer> noteRefs = new TreeMap<String, Integer>();
    private final ArrayList<LineStream> notes = new ArrayList<LineStream>();

    private String cover;

    public TypefaceEx[] fonts;
    public TypefaceEx mono;

    public void loadFonts() {
        final FontStyle[] styles = FontStyle.values();
        fonts = new TypefaceEx[styles.length];
        for (final FontStyle style : styles) {
            final TypefaceEx font = FontManager.getFont(AppSettings.current().fb2FontPack, FontFamilyType.SERIF, style);
            System.out.println("Preloaded: " + font);
            fonts[style.ordinal()] = font;
            RenderingStyle.getTextPaint(font, TextStyle.TEXT.getFontSize());
        }
        mono = FontManager.getFont(SystemFontProvider.SYSTEM_FONT_PACK, FontFamilyType.MONO, FontStyle.REGULAR);
    }

    public void clear() {
        docMarkup.clear();
        for (final Entry<String, MarkupStream> entry : streams.entrySet()) {
            final MarkupStream value = entry.getValue();
            if (value != null) {
                value.clear();
            }
        }
        streams.clear();
    }

    public void recycle() {
        for (final Image image : standaloneImages) {
            image.data.recycle();
        }
        standaloneImages.clear();
        for (final Image image : inlineImages) {
            image.data.recycle();
        }
        inlineImages.clear();
        text.clear();
    }

    public MarkupStream getMarkupStream(final String streamName) {
        if (streamName == null) {
            return docMarkup;
        }
        MarkupStream stream = streams.get(streamName);
        if (stream == null) {
            stream = new MarkupStream(streamName);
            streams.put(streamName, stream);
        }
        return stream;
    }

    public int getImageRef(final String name) {
        Integer existed = imageRefs.get(name);
        if (existed == null && name.startsWith("#")) {
            existed = imageRefs.get(name.substring(1));
        }
        if (existed != null) {
            return existed;
        }

        final int ref = inlineImages.size();
        imageRefs.put(name, ref);
        if (name.startsWith("#")) {
            imageRefs.put(name.substring(1), ref);
        }
        inlineImages.add(null);
        standaloneImages.add(null);
        return ref;
    }

    public int addImage(final String name, final String encoded) {
        if (name != null && encoded != null) {
            IImageData data = new MemoryImageData(encoded);
            if (AppSettings.current().fb2CacheImagesOnDisk) {
                data = new DiskImageData((MemoryImageData) data);
            }
            final int ref = getImageRef(name);
            inlineImages.set(ref, new Image(ref, data, true));
            standaloneImages.set(ref, new Image(ref, data, false));
            return ref;
        }
        return -1;
    }

    public Image getImage(final String name, final boolean inline) {
        if (name == null) {
            return null;
        }

        final int ref = getImageRef(name);
        final ArrayList<Image> arr = inline ? inlineImages : standaloneImages;
        return ref < arr.size() ? arr.get(ref) : null;
    }

    public Image getImage(final int ref, final boolean inline) {
        final ArrayList<Image> arr = inline ? inlineImages : standaloneImages;
        return ref < arr.size() ? arr.get(ref) : null;
    }

    public int getNoteRef(final String noteName) {
        Integer existing = noteRefs.get(noteName);
        if (existing != null) {
            return existing;
        }
        int ref = notes.size();
        notes.add(null);
        noteRefs.put(noteName, ref);
        noteNames.put(ref, noteName);
        return ref;
    }

    public LineStream getNote(final int ref) {
        String noteName = noteNames.get(ref);
        if (noteName == null) {
            return null;
        }
        LineStream note = notes.get(ref);
        if (note != null) {
            return note;
        }

        MarkupStream stream = getMarkupStream(noteName);
        if (MarkupStream.isEmpty(stream) && noteName.startsWith("#")) {
            stream = getMarkupStream(noteName.substring(1));
        }
        if (stream != null) {
            note = createLines(stream, PAGE_WIDTH - 2 * MARGIN_X, JustificationMode.Justify);
            notes.set(ref, note);
        }

        return note;
    }

    LineStream createLines(final MarkupStream markup, final int maxLineWidth, final JustificationMode jm) {
        final LineCreationParams params = new LineCreationParams(this, jm, maxLineWidth);
        final LineStream lines = new LineStream(params);

        if (MarkupStream.isNotEmpty(markup)) {

            try {
                markup.publishToLines(lines);
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
        return lines;
    }

    public void setCover(final String value) {
        this.cover = value;
    }

    public Bitmap getCoverImage() {
        final Image image = getImage(cover, false);
        if (image != null) {
            return image.data.getBitmap();
        }
        return null;
    }

    private LongSparseArray<ITextProvider> text = new LongSparseArray<ITextProvider>();

    public void addTextProvider(ITextProvider p) {
        text.append(p.id(), p);
    }

    public ITextProvider getTextProvider(long id) {
        return text.get(id, null);
    }
}
