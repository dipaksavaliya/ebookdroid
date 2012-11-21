package org.ebookdroid.common.bitmaps;

import org.ebookdroid.core.PagePaint;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.emdev.common.log.LogContext;
import org.emdev.common.log.LogManager;
import org.emdev.ui.gl.ByteBufferTexture;
import org.emdev.ui.gl.GLCanvas;
import org.emdev.utils.MathUtils;

public class BBBitmaps {

    protected static final LogContext LCTX = LogManager.root().lctx("Bitmaps", false);

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Rect bounds;
    public String nodeId;

    private ByteBufferBitmap bitmap;

    private ByteBufferTexture texture;

    public BBBitmaps(String nodeId, ByteBufferBitmap orig, Rect bitmapBounds, boolean invert) {
        this.nodeId = nodeId;
        this.bitmap = orig;
        this.bounds = bitmapBounds;
        if (invert) {
            this.bitmap.invert();
        }
    }

    public boolean drawGL(GLCanvas canvas, PagePaint paint, PointF vb, RectF tr, RectF cr) {
        lock.writeLock().lock();
        try {
            if (texture == null) {
                if (this.bitmap == null) {
                    return false;
                }
                texture = new ByteBufferTexture(bitmap);
            }

            if (LCTX.isDebugEnabled()) {
                LCTX.d(nodeId + ".drawGL(): >>>>");
            }

            final RectF actual = new RectF(cr.left - vb.x, cr.top - vb.y, cr.right - vb.x, cr.bottom - vb.y);
            MathUtils.round(actual);
            canvas.setClipRect(actual);

            final RectF src = new RectF(0, 0, texture.getWidth(), texture.getHeight());
            final RectF r = new RectF(tr.left - vb.x, tr.top - vb.y, (tr.left - vb.x) + tr.width(), (tr.top - vb.y)
                    + tr.height());

            final boolean res = canvas.drawTexture(texture, src, r);

            if (res && bitmap != null) {
                BBManager.release(bitmap);
                bitmap = null;
            }

            if (LCTX.isDebugEnabled()) {
                LCTX.d(nodeId + ".drawGL(): <<<<<");
            }

            canvas.clearClipRect();

            return res;
        } finally {
            lock.writeLock().unlock();
        }
    }

    ByteBufferBitmap clear() {
        lock.writeLock().lock();
        try {
            if (texture != null) {
                texture.recycle();
                texture = null;
            }

            final ByteBufferBitmap ref = this.bitmap;
            this.bitmap = null;
            return ref;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasBitmaps() {
        lock.readLock().lock();
        try {
            if (texture != null) {
                return true;
            }
            if (bitmap == null) {
                return false;
            }
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

}
