package org.ebookdroid.core.crop;

import org.ebookdroid.common.bitmaps.BitmapRef;
import org.ebookdroid.common.bitmaps.RawBitmap;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

public class PageCropper {

    private final static int V_LINE_SIZE = 10;

    private final static int H_LINE_SIZE = 10;
    
    private static final int LINE_MARGIN = 20;

    private static final double WHITE_THRESHOLD = 0.005;

    private PageCropper() {
    }

    public static RectF getCropBounds(final BitmapRef bitmap, final Rect bitmapBounds, final RectF pageSliceBounds) {
        // final long t0 = System.currentTimeMillis();
        final float avgLum = calculateAvgLum(bitmap, bitmapBounds);
        // final long t1 = System.currentTimeMillis();
        final float left = getLeftBound(bitmap, bitmapBounds, avgLum);
        final float right = getRightBound(bitmap, bitmapBounds, avgLum);
        final float top = getTopBound(bitmap, bitmapBounds, avgLum);
        final float bottom = getBottomBound(bitmap, bitmapBounds, avgLum);
        // final long t5 = System.currentTimeMillis();

        // System.out.println("Crop: total=" + (t5 - t0) + "ms, avgLum=" + (t1 - t0) + "ms");

        return new RectF(left * pageSliceBounds.width() + pageSliceBounds.left, top * pageSliceBounds.height()
                + pageSliceBounds.top, right * pageSliceBounds.width() + pageSliceBounds.left, bottom
                * pageSliceBounds.height() + pageSliceBounds.top);
    }

    private static float getLeftBound(final BitmapRef bitmap, final Rect bitmapBounds, final float avgLum) {
        final Bitmap bmp = bitmap.getBitmap();
        final int w = bmp.getWidth() / 3;
        int whiteCount = 0;
        int x = 0;
        for (x = bitmapBounds.left; x < bitmapBounds.left + w; x += V_LINE_SIZE) {
            final boolean white = isRectWhite(bmp, x, bitmapBounds.top + LINE_MARGIN, x + V_LINE_SIZE,
                    bitmapBounds.bottom - LINE_MARGIN, avgLum);
            if (white) {
                whiteCount++;
            } else {
                if (whiteCount >= 1) {
                    return (float) (Math.max(bitmapBounds.left, x - V_LINE_SIZE) - bitmapBounds.left)
                            / bitmapBounds.width();
                }
                whiteCount = 0;
            }
        }
        return whiteCount > 0 ? (float) (Math.max(bitmapBounds.left, x - V_LINE_SIZE) - bitmapBounds.left)
                / bitmapBounds.width() : 0;
    }

    private static float getTopBound(final BitmapRef bitmap, final Rect bitmapBounds, final float avgLum) {
        final Bitmap bmp = bitmap.getBitmap();
        final int h = bmp.getHeight() / 3;
        int whiteCount = 0;
        int y = 0;
        for (y = bitmapBounds.top; y < bitmapBounds.top + h; y += H_LINE_SIZE) {
            final boolean white = isRectWhite(bmp, bitmapBounds.left + LINE_MARGIN, y,
                    bitmapBounds.right - LINE_MARGIN, y + H_LINE_SIZE, avgLum);
            if (white) {
                whiteCount++;
            } else {
                if (whiteCount >= 1) {
                    return (float) (Math.max(bitmapBounds.top, y - H_LINE_SIZE) - bitmapBounds.top)
                            / bitmapBounds.height();
                }
                whiteCount = 0;
            }
        }
        return whiteCount > 0 ? (float) (Math.max(bitmapBounds.top, y - H_LINE_SIZE) - bitmapBounds.top)
                / bitmapBounds.height() : 0;
    }

    private static float getRightBound(final BitmapRef bitmap, final Rect bitmapBounds, final float avgLum) {
        final Bitmap bmp = bitmap.getBitmap();
        final int w = bmp.getWidth() / 3;
        int whiteCount = 0;
        int x = 0;
        for (x = bitmapBounds.right - V_LINE_SIZE; x > bitmapBounds.right - w; x -= V_LINE_SIZE) {
            final boolean white = isRectWhite(bmp, x, bitmapBounds.top + LINE_MARGIN, x + V_LINE_SIZE,
                    bitmapBounds.bottom - LINE_MARGIN, avgLum);
            if (white) {
                whiteCount++;
            } else {
                if (whiteCount >= 1) {
                    return (float) (Math.min(bitmapBounds.right, x + 2 * V_LINE_SIZE) - bitmapBounds.left)
                            / bitmapBounds.width();
                }
                whiteCount = 0;
            }
        }
        return whiteCount > 0 ? (float) (Math.min(bitmapBounds.right, x + 2 * V_LINE_SIZE) - bitmapBounds.left)
                / bitmapBounds.width() : 1;
    }

    private static float getBottomBound(final BitmapRef bitmap, final Rect bitmapBounds, final float avgLum) {
        final Bitmap bmp = bitmap.getBitmap();
        final int h = bmp.getHeight() / 3;
        int whiteCount = 0;
        int y = 0;
        for (y = bitmapBounds.bottom - H_LINE_SIZE; y > bitmapBounds.bottom - h; y -= H_LINE_SIZE) {
            final boolean white = isRectWhite(bmp, bitmapBounds.left + LINE_MARGIN, y,
                    bitmapBounds.right - LINE_MARGIN, y + H_LINE_SIZE, avgLum);
            if (white) {
                whiteCount++;
            } else {
                if (whiteCount >= 1) {
                    return (float) (Math.min(bitmapBounds.bottom, y + 2 * H_LINE_SIZE) - bitmapBounds.top)
                            / bitmapBounds.height();
                }
                whiteCount = 0;
            }
        }
        return whiteCount > 0 ? (float) (Math.min(bitmapBounds.bottom, y + 2 * H_LINE_SIZE) - bitmapBounds.top)
                / bitmapBounds.height() : 1;
    }

    private static boolean isRectWhite(final Bitmap bmp, final int left, final int top, final int right,
            final int bottom, final float avgLum) {
        int count = 0;

        final RawBitmap rb = new RawBitmap(bmp, new Rect(left, top, right, bottom));
        final int[] pixels = rb.getPixels();
        for (final int c : pixels) {
            final float lum = getLum(c);
            if ((lum < avgLum) && ((avgLum - lum) * 10 > avgLum)) {
                count++;
            }
        }
        return ((float) count / pixels.length) < WHITE_THRESHOLD;
    }

    private static float calculateAvgLum(final BitmapRef bitmap, final Rect bitmapBounds) {
        final Bitmap bmp = bitmap.getBitmap();
        if (bmp == null) {
            return 1000;
        }

        float lum = 0f;

        final int sizeX = bitmapBounds.width() / 10;
        final int sizeY = bitmapBounds.height() / 10;
        final int centerX = bitmapBounds.centerX();
        final int centerY = bitmapBounds.centerX();

        final RawBitmap rb = new RawBitmap(bmp, new Rect(centerX - sizeX, centerY - sizeY, centerX + sizeX, centerY
                + sizeY));
        final int[] pixels = rb.getPixels();
        for (final int c : pixels) {
            lum += getLum(c);
        }

        return lum / (pixels.length);
    }

    private static float getLum(final int c) {
        final int r = (c & 0xFF0000) >> 16;
        final int g = (c & 0xFF00) >> 8;
        final int b = c & 0xFF;

        final int min = Math.min(r, Math.min(g, b));
        final int max = Math.max(r, Math.max(g, b));
        return (min + max) / 2;
    }

}
