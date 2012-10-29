package org.emdev.common.textmarkup.line;

import org.ebookdroid.droids.fb2.codec.ParsedContent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelXorXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.MarkupStream.IMarkupStreamCallback;
import org.emdev.common.textmarkup.MarkupTag;
import org.emdev.common.textmarkup.image.IImageData;
import org.emdev.utils.bytes.ByteArray.DataArrayInputStream;

public class Image implements IMarkupStreamCallback {

    public final int height;
    public float width;
    public final int ref;
    public final boolean inline;
    public final IImageData data;
    private final Paint paint;

    public Image(final int ref, final IImageData data, final boolean inline) {
        RectF imageRect = data.getImageRect(inline);
        this.width = imageRect.width();
        this.height = (int)imageRect.height();
        this.ref = ref;
        this.data = data;
        this.inline = inline;
        this.paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    public float render(final Canvas c, final int y, final int x, final float additionalWidth, final float left,
            final float right, final int nightmode) {
        if (left < x + width && x < right) {
            final Bitmap bmp = data.getBitmap();

            paint.setXfermode(null);
            if (nightmode != 0) {
                paint.setXfermode(new PixelXorXfermode(-1));
                c.drawRect(x, y - height, (int) (x + width), y, paint);
            }

            if (bmp != null) {
                c.drawBitmap(bmp, null, new Rect(x, y - height, (int) (x + width), y), paint);
                bmp.recycle();
            } else {
                c.drawRect(new Rect(x, y - height, (int) (x + width), y), paint);
            }
        }
        return width;
    }

    @Override
    public void write(final DataOutputStream out) throws IOException {
        out.writeByte(MarkupTag.Image.ordinal());
        out.writeFloat(width);
        out.writeInt(height);
        out.writeInt(ref);
        out.writeBoolean(inline);
    }

    public static float render(final ParsedContent content, final DataArrayInputStream in, final Canvas c, final int y,
            final int x, final float spaceWidth, final float left, final float right, final int nightmode)
            throws IOException {
        final float width = in.readFloat();
        final int height = in.readInt();
        final int ref = in.readInt();
        final boolean inline = in.readBoolean();

        final Image image = content.getImage(ref, inline);
        if (image != null) {
            return image.render(c, y, x, spaceWidth, left, right, nightmode);
        }
        return width;
    }
}
