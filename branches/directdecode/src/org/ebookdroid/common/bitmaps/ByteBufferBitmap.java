package org.ebookdroid.common.bitmaps;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class ByteBufferBitmap {

    private static final AtomicInteger SEQ = new AtomicInteger();

    public final int id = SEQ.incrementAndGet();
    final AtomicBoolean used = new AtomicBoolean(true);
    long gen;

    volatile ByteBuffer pixels;
    final int size;
    int width;
    int height;

    ByteBufferBitmap(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.size = 4 * width * height;
        this.pixels = create(size).order(ByteOrder.nativeOrder());
    }

    @Override
    protected void finalize() throws Throwable {
        recycle();
    }

    public static ByteBufferBitmap get(final IBitmapRef bitmap) {
        final Bitmap bmp = bitmap.getBitmap();
        final ByteBufferBitmap b = BBManager.getBitmap(bmp.getWidth(), bmp.getHeight());
        bmp.copyPixelsToBuffer(b.pixels);
        return b;
    }

    public static ByteBufferBitmap get(final Bitmap bitmap, final Rect srcRect) {
        final ByteBufferBitmap full = BBManager.getBitmap(bitmap.getWidth(), bitmap.getHeight());
        bitmap.copyPixelsToBuffer(full.pixels);

        final ByteBufferBitmap part = BBManager.getBitmap(srcRect.width(), srcRect.height());
        nativeFillRect(full.pixels, part.pixels, full.width, srcRect.left, srcRect.top, part.width, part.height);

        BBManager.release(full);

        return part;
    }

    public void recycle() {
        ByteBuffer buf = pixels;
        pixels = null;
        free(buf);
        buf = null;
    }

    public void copyPixelsFrom(final ByteBufferBitmap src, int left, int top, int width, int height) {
        if (width > this.width) {
            throw new IllegalArgumentException("width > this.width");
        }
        if (height > this.height) {
            throw new IllegalArgumentException("height > this.height");
        }
        if (left + width > src.width) {
            throw new IllegalArgumentException("left + width > src.width");
        }
        if (top + height > src.height) {
            throw new IllegalArgumentException("top + height > src.height");
        }
        nativeFillRect2(src.pixels, src.width, this.pixels, this.width, left, top, width, height);
    }

    public ByteBuffer getPixels() {
        return pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public IBitmapRef toBitmap() {
        final IBitmapRef bitmap = BitmapManager.getBitmap("RawBitmap", width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels);
        return bitmap;
    }

    public void fillAlpha(final int v) {
        nativeFillAlpha(pixels, width, height, v);
    }

    public void invert() {
        nativeInvert(pixels, width, height);
    }

    public int getAvgLum() {
        return nativeAvgLum(pixels, width, height);
    }

    public void contrast(final int contrast) {
        nativeContrast(pixels, width, height, contrast * 256 / 100);
    }

    public void exposure(final int exposure) {
        nativeExposure(pixels, width, height, exposure * 128 / 100);
    }

    public void autoLevels() {
        nativeAutoLevels2(pixels, width, height);
    }

    public static ByteBufferBitmap scaleHq4x(final Bitmap bitmap, final Rect srcRect) {
        final ByteBufferBitmap src = get(bitmap, srcRect);
        src.fillAlpha(0x00);

        final ByteBufferBitmap dest = BBManager.getBitmap(src.width * 4, src.height * 4);
        nativeHq4x(src.pixels, dest.pixels, src.width, src.height);
        dest.fillAlpha(0xFF);

        BBManager.release(src);
        return dest;
    }

    public static ByteBufferBitmap scaleHq3x(final Bitmap bitmap, final Rect srcRect) {
        final ByteBufferBitmap src = get(bitmap, srcRect);
        src.fillAlpha(0x00);

        final ByteBufferBitmap dest = BBManager.getBitmap(src.width * 3, src.height * 3);
        nativeHq3x(src.pixels, dest.pixels, src.width, src.height);
        dest.fillAlpha(0xFF);

        BBManager.release(src);
        return dest;
    }

    public static ByteBufferBitmap scaleHq2x(final Bitmap bitmap, final Rect srcRect) {
        final ByteBufferBitmap src = get(bitmap, srcRect);
        src.fillAlpha(0x00);

        final ByteBufferBitmap dest = BBManager.getBitmap(src.width * 2, src.height * 2);
        nativeHq2x(src.pixels, dest.pixels, src.width, src.height);
        dest.fillAlpha(0xFF);

        BBManager.release(src);
        return dest;
    }

    private static native void nativeHq2x(ByteBuffer src, ByteBuffer dst, int width, int height);

    private static native void nativeHq3x(ByteBuffer src, ByteBuffer dst, int width, int height);

    private static native void nativeHq4x(ByteBuffer src, ByteBuffer dst, int width, int height);

    private static native void nativeInvert(ByteBuffer src, int width, int height);

    private static native void nativeFillAlpha(ByteBuffer src, int width, int height, int value);

    /* contrast value 256 - normal */
    private static native void nativeContrast(ByteBuffer src, int width, int height, int contrast);

    /* Exposure correction values -128...+128 */
    private static native void nativeExposure(ByteBuffer src, int width, int height, int exposure);

    private static native void nativeAutoLevels(ByteBuffer src, int width, int height);

    private static native void nativeAutoLevels2(ByteBuffer src, int width, int height);

    private static native int nativeAvgLum(ByteBuffer src, int width, int height);

    private static native void nativeFillRect(ByteBuffer src, ByteBuffer dst, int srcWidth, int x, int y, int dstWidth,
            int dstHeight);

    private static native void nativeFillRect2(ByteBuffer src, int srcWidth, ByteBuffer dst, int dstWidth, int x, int y,
            int width, int height);

    private static native ByteBuffer create(int size);

    private static native void free(ByteBuffer buf);

}
