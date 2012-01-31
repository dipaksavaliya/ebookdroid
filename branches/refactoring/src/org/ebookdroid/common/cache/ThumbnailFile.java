package org.ebookdroid.common.cache;

import org.ebookdroid.R;
import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.BitmapRef;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ThumbnailFile extends File {

    private static final long serialVersionUID = 4540533658351961301L;

    private BitmapRef ref;

    ThumbnailFile(final File dir, final String name) {
        super(dir, name);
        ref = null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (ref != null) {
            BitmapManager.release(ref);
            ref = null;
        }
        super.finalize();
    }

    public Bitmap getImage() {
        if (ref == null) {
            try {
                Bitmap stored = null;
                try {
                    stored = load();
                    ref = paint(stored);
                } finally {
                    if (stored != null) {
                        stored.recycle();
                    }
                }
            } catch (final OutOfMemoryError ex) {
            }
        }
        return ref != null ? ref.getBitmap() : null;
    }

    public void setImage(final Bitmap image) {
        if (ref != null) {
            BitmapManager.release(ref);
        }
        if (image != null) {
            ref = paint(image);
            store(image);
        } else {
            this.delete();
        }
    }

    private Bitmap load() {
        Bitmap stored = null;
        if (this.exists()) {
            stored = BitmapFactory.decodeFile(this.getPath());
            if (stored == null) {
                this.delete();
                stored = Bitmap.createBitmap(160, 200, Bitmap.Config.ARGB_8888);
                stored.eraseColor(Color.WHITE);
            }
        } else {
            stored = Bitmap.createBitmap(160, 200, Bitmap.Config.ARGB_8888);
            stored.eraseColor(Color.WHITE);
        }
        return stored;
    }

    private void store(final Bitmap image) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(this);
            image.compress(Bitmap.CompressFormat.JPEG, 50, out);
        } catch (final IOException e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException ex) {
                }
            }
        }
    }

    private BitmapRef paint(final Bitmap image) {
        final int left = 15;
        final int top = 10;
        final int width = image.getWidth() + left;
        final int height = image.getHeight() + top;

        final BitmapRef newRef = BitmapManager.getBitmap(getName(), width, height, Bitmap.Config.ARGB_8888);
        final Bitmap bmp = newRef.getBitmap();

        bmp.eraseColor(Color.TRANSPARENT);

        final Canvas c = new Canvas(bmp);

        final Bitmap cornerBmp = BitmapManager.getResource(R.drawable.bt_corner);
        final Bitmap leftBmp = BitmapManager.getResource(R.drawable.bt_left);
        final Bitmap topBmp = BitmapManager.getResource(R.drawable.bt_top);

        c.drawBitmap(cornerBmp, null, new Rect(0, 0, left, top), null);
        c.drawBitmap(topBmp, null, new Rect(left, 0, width, top), null);
        c.drawBitmap(leftBmp, null, new Rect(0, top, left, height), null);
        c.drawBitmap(image, null, new Rect(left, top, width, height), null);

        return newRef;
    }

}
