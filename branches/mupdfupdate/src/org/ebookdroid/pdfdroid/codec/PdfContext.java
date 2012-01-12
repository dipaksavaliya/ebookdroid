package org.ebookdroid.pdfdroid.codec;

import org.ebookdroid.core.BaseViewerActivity;
import org.ebookdroid.core.EBookDroidLibraryLoader;
import org.ebookdroid.core.codec.AbstractCodecContext;
import org.ebookdroid.core.codec.CodecDocument;

import android.graphics.Bitmap;

import java.lang.reflect.Field;

public class PdfContext extends AbstractCodecContext {

    public static final boolean useNativeGraphics;

    public static final Bitmap.Config BITMAP_CFG = Bitmap.Config.RGB_565;

    public static final Bitmap.Config NATIVE_BITMAP_CFG = Bitmap.Config.ARGB_8888;

    private static Integer densityDPI;

    static {
        EBookDroidLibraryLoader.load();
        useNativeGraphics = isNativeGraphicsAvailable();
    }

    @Override
    public Bitmap.Config getBitmapConfig() {
        return useNativeGraphics ? NATIVE_BITMAP_CFG : BITMAP_CFG;
    }

    @Override
    public CodecDocument openDocument(final String fileName, final String password) {
        return new PdfDocument(this, fileName, password);
    }

    public static int getWidthInPixels(final float pdfWidth) {
        return getSizeInPixels(pdfWidth, BaseViewerActivity.DM.xdpi);
    }

    public static int getHeightInPixels(final float pdfHeight) {
        return getSizeInPixels(pdfHeight, BaseViewerActivity.DM.ydpi);
    }

    public static int getSizeInPixels(final float pdfHeight, float dpi) {
        if (dpi == 0) {
            // Archos fix
            dpi = getDensityDPI();
        }
        if (dpi < 72) { // Density lover then 72 is to small
            dpi = 72; // Set default density to 72
        }
        return (int) (pdfHeight * dpi / 72);
    }

    private static int getDensityDPI() {
        if (densityDPI == null) {
            try {
                Field f = BaseViewerActivity.DM.getClass().getDeclaredField("densityDpi");
                densityDPI = ((Integer) f.get(BaseViewerActivity.DM));
            } catch (final Throwable ex) {
                densityDPI = Integer.valueOf(120);
            }
        }
        return densityDPI.intValue();
    }

    private static native boolean isNativeGraphicsAvailable();
}
