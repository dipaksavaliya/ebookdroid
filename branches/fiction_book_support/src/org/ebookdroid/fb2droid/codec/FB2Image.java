package org.ebookdroid.fb2droid.codec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;


public class FB2Image implements FB2LineElement {

    private final int width;
    private final int height;
    private final byte[] data;
    private final int origWidth;
    private final int origHeight;
    public FB2Image(byte[] data) {
        this.data = data;
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        origWidth = opts.outWidth;
        origHeight = opts.outHeight;
        
        if (origWidth <= FB2Page.PAGE_WIDTH && origHeight <= FB2Page.PAGE_HEIGHT) {
            width = origWidth;
            height = origHeight;
        } else {
            int w = 0, h = 0;
            if (origWidth > FB2Page.PAGE_WIDTH) {
                w = FB2Page.PAGE_WIDTH;
                h = origHeight * w / origWidth;
            } else {
                w = origWidth;
                h = origHeight;
            }
            if (h > FB2Page.PAGE_HEIGHT) {
                w = w * FB2Page.PAGE_HEIGHT / h;
                h = FB2Page.PAGE_HEIGHT;
            }
            width = w;
            height = h;
        }
    }

    @Override
    public void adjustWidth(int w) {
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public boolean isSizeable() {
        return false;
    }

    @Override
    public void render(Canvas c, int y, int x) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        c.drawBitmap(bmp, null, new Rect(x, y - height, x+width, y), null);
        bmp.recycle();
    }
    
    public byte[] getData() {
        return data;
    }

}
