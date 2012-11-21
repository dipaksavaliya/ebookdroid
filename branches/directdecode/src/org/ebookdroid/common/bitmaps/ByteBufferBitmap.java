package org.ebookdroid.common.bitmaps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ByteBufferBitmap {

    ByteBuffer pixels;
    int width;
    int height;

    public ByteBufferBitmap(final int width, final int height, ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        this.pixels = buffer;
    }

    public ByteBufferBitmap(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.pixels = ByteBuffer.allocateDirect(4 * width * height).order(ByteOrder.nativeOrder());
    }

    public ByteBufferBitmap(final IBitmapRef bitmap, final Rect srcRect) {
        width = srcRect.width();
        height = srcRect.height();
        this.pixels = ByteBuffer.allocateDirect(4 * width * height).order(ByteOrder.nativeOrder());

        bitmap.getBitmap().copyPixelsToBuffer(pixels);
    }

    public ByteBufferBitmap(final IBitmapRef bitmap) {
        width = bitmap.getBitmap().getWidth();
        height = bitmap.getBitmap().getHeight();
        this.pixels = ByteBuffer.allocateDirect(4 * width * height).order(ByteOrder.nativeOrder());

        bitmap.getBitmap().copyPixelsToBuffer(pixels);
    }

    public ByteBufferBitmap(final Bitmap bitmap, final Rect srcRect) {
        width = srcRect.width();
        height = srcRect.height();
        pixels = ByteBuffer.allocateDirect(4 * width * height).order(ByteOrder.nativeOrder());

        ByteBuffer src = ByteBuffer.allocateDirect(4 * bitmap.getWidth() * bitmap.getHeight()).order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(src);

        nativeFillRect(src, pixels, bitmap.getWidth(), srcRect.left, srcRect.top, width, height);
    }

    public ByteBufferBitmap(final Bitmap bitmap, final int left, final int top, final int width, final int height) {
        this.width = width;
        this.height = height;
        pixels = ByteBuffer.allocateDirect(4 * width * height).order(ByteOrder.nativeOrder());

        ByteBuffer src = ByteBuffer.allocateDirect(4 * bitmap.getWidth() * bitmap.getHeight()).order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(src);

        nativeFillRect(src, pixels, bitmap.getWidth(), left, top, width, height);
    }

    public void retrieve(final Bitmap bitmap, final int left, final int top) {
        ByteBuffer src = ByteBuffer.allocateDirect(4 * bitmap.getWidth() * bitmap.getHeight()).order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(src);
        nativeFillRect(src, pixels, bitmap.getWidth(), left, top, width, height);
    }

    public void retrieve(final Bitmap bitmap, final int left, final int top, final int width, final int height) {
        this.width = width;
        this.height = height;
        ByteBuffer src = ByteBuffer.allocateDirect(4 * bitmap.getWidth() * bitmap.getHeight()).order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(src);
        nativeFillRect(src, pixels, bitmap.getWidth(), left, top, width, height);
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

    public void draw(final Canvas canvas, final float x, final float y, final Paint paint) {
        throw new RuntimeException("Not implemented");
//        canvas.drawBitmap(pixels, 0, width, x, y, width, height, false, paint);
    }

    public void toBitmap(final Bitmap bitmap) {
        bitmap.copyPixelsFromBuffer(pixels);
    }

    public IBitmapRef toBitmap() {
        final IBitmapRef bitmap = BitmapManager.getBitmap("RawBitmap", width, height, Bitmap.Config.RGB_565);
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

    public ByteBufferBitmap scaleHq4x() {
        return scaleHq4x(this);
    }

    public ByteBufferBitmap scaleHq3x() {
        return scaleHq3x(this);
    }

    public ByteBufferBitmap scaleHq2x() {
        return scaleHq2x(this);
    }

    public static ByteBufferBitmap scaleHq4x(final ByteBufferBitmap src) {
        final ByteBufferBitmap dest = new ByteBufferBitmap(src.width * 4, src.height * 4);
        src.fillAlpha(0x00);

        nativeHq4x(src.pixels, dest.pixels, src.width, src.height);
        dest.fillAlpha(0xFF);
        return dest;
    }

    public static ByteBufferBitmap scaleHq3x(final ByteBufferBitmap src) {
        final ByteBufferBitmap dest = new ByteBufferBitmap(src.width * 3, src.height * 3);
        src.fillAlpha(0x00);

        nativeHq3x(src.pixels, dest.pixels, src.width, src.height);
        dest.fillAlpha(0xFF);
        return dest;
    }

    public static ByteBufferBitmap scaleHq2x(final ByteBufferBitmap src) {
        final ByteBufferBitmap dest = new ByteBufferBitmap(src.width * 2, src.height * 2);
        src.fillAlpha(0x00);

        nativeHq2x(src.pixels, dest.pixels, src.width, src.height);
        dest.fillAlpha(0xFF);
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
}
