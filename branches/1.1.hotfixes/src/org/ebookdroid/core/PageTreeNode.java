package org.ebookdroid.core;

import org.ebookdroid.core.codec.CodecPage;
import org.ebookdroid.core.log.LogContext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PageTreeNode implements DecodeService.DecodeCallback {

    private static final LogContext LCTX = LogContext.ROOT.lctx("Decoding");

    // private static final int SLICE_SIZE = 65535;
    private static final int SLICE_SIZE = 131070;

    private static final AtomicLong SEQ = new AtomicLong();

    private static RectF[] splitMasks = {
            // Left Top
            new RectF(0, 0, 0.5f, 0.5f),
            // Right top
            new RectF(0.5f, 0, 1.0f, 0.5f),
            // Left Bottom
            new RectF(0, 0.5f, 0.5f, 1.0f),
            // Right Bottom
            new RectF(0.5f, 0.5f, 1.0f, 1.0f), };

    final long id;
    final AtomicBoolean decodingNow = new AtomicBoolean();
    final RectF pageSliceBounds;
    final Page page;
    final IViewerActivity base;
    PageTreeNode[] children;
    final float childrenZoomThreshold;
    final Matrix matrix = new Matrix();
    Rect targetRect;
    RectF targetRectF;
    final boolean slice_limit;
    final PageTreeNode parent;

    private final BitmapHolder holder = new BitmapHolder();

    PageTreeNode(final IViewerActivity base, final RectF localPageSliceBounds, final Page page,
            final float childrenZoomThreshold, final PageTreeNode parent, final boolean sliceLimit) {
        this.id = SEQ.incrementAndGet();
        this.base = base;
        this.parent = parent;
        this.pageSliceBounds = evaluatePageSliceBounds(localPageSliceBounds, parent);
        this.page = page;
        this.childrenZoomThreshold = childrenZoomThreshold;
        this.slice_limit = sliceLimit;
    }

    public IViewerActivity getBase() {
        return base;
    }

    /**
     * Gets the parent node.
     *
     * @return the parent node
     */
    public PageTreeNode getParent() {
        return parent;
    }

    public RectF getPageSliceBounds() {
        return pageSliceBounds;
    }

    public void updateVisibility(final List<PageTreeNode> nodesToDecode) {
        invalidateChildren();
        if (children != null) {
            for (final PageTreeNode child : children) {
                child.updateVisibility(nodesToDecode);
            }
        }
        if (!isKeptInMemory() || isHiddenByChildren()) {
            stopDecodingThisNode("node hidden");
            holder.clearDirectRef();
        } else if (isKeptInMemory()) {
            if (!thresholdHit()) {
                if (getBitmap() == null) {
                    decodePageTreeNode(nodesToDecode);
                }
            }
        }
    }

    public void invalidate(final List<PageTreeNode> nodesToDecode) {
        invalidateChildren();
        invalidateRecursive();
        updateVisibility(nodesToDecode);
    }

    private void invalidateRecursive() {
        if (children != null) {
            for (final PageTreeNode child : children) {
                child.invalidateRecursive();
            }
        }
    }

    void invalidateNodeBounds() {
        targetRect = null;
        targetRectF = null;
        if (children != null) {
            for (final PageTreeNode child : children) {
                child.invalidateNodeBounds();
            }
        }
    }

    void draw(final Canvas canvas, final PagePaint paint) {
        final Rect tr = getTargetRect();
        final Bitmap bitmap = holder.getBitmap();
        if (bitmap != null) {
            canvas.drawRect(tr, paint.getFillPaint());
            canvas.drawBitmap(bitmap, null, tr, paint.getBitmapPaint());
        }

        drawBrightnessFilter(canvas);

        drawChildren(canvas, paint);
    }

    void drawBrightnessFilter(final Canvas canvas) {
        final int brightness = getBase().getAppSettings().getBrightness();
        if (brightness < 100) {
            final Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setAlpha(255 - brightness * 255 / 100);
            canvas.drawRect(getTargetRect(), p);
        }
    }

    void drawChildren(final Canvas canvas, final PagePaint paint) {
        if (children != null) {
            for (final PageTreeNode child : children) {
                child.draw(canvas, paint);
            }
        }
    }

    private boolean isKeptInMemory() {
        return base.getDocumentController().shouldKeptInMemory(this);
    }

    public RectF getTargetRectF() {
        if (targetRectF == null) {
            targetRectF = new RectF(getTargetRect());
        }
        return targetRectF;
    }

    private void invalidateChildren() {
        if (thresholdHit() && children == null && isKeptInMemory()) {
            final float newThreshold = childrenZoomThreshold * 2;
            children = new PageTreeNode[splitMasks.length];
            for (int i = 0; i < children.length; i++) {
                children[i] = new PageTreeNode(base, splitMasks[i], page, newThreshold, this, slice_limit);
            }
        }
        if (!thresholdHit() && getBitmap() != null || !isKeptInMemory()) {
            recycleChildren();
        }
    }

    private boolean thresholdHit() {
        if (slice_limit) {
            final float zoom = base.getZoomModel().getZoom();
            final int mainWidth = base.getDocumentController().getView().getWidth();
            final float height = page.getPageHeight(mainWidth, zoom);
            return (mainWidth * zoom * height) / (childrenZoomThreshold * childrenZoomThreshold) > SLICE_SIZE;
        } else {
            return base.getZoomModel().getZoom() > childrenZoomThreshold;
        }
    }

    public Bitmap getBitmap() {
        return holder.getBitmap();
    }

    private void decodePageTreeNode(final List<PageTreeNode> nodesToDecode) {
        if (setDecodingNow(true)) {
            nodesToDecode.add(this);
        }
    }

    @Override
    public void decodeComplete(final CodecPage codecPage, final Bitmap bitmap) {
        base.getView().post(new Runnable() {

            @Override
            public void run() {
                holder.setBitmap(bitmap);
                setDecodingNow(false);

                page.setAspectRatio(codecPage);

                invalidateChildren();
            }
        });
    }

    private static RectF evaluatePageSliceBounds(final RectF localPageSliceBounds, final PageTreeNode parent) {
        if (parent == null) {
            return localPageSliceBounds;
        }
        final Matrix matrix = new Matrix();
        matrix.postScale(parent.pageSliceBounds.width(), parent.pageSliceBounds.height());
        matrix.postTranslate(parent.pageSliceBounds.left, parent.pageSliceBounds.top);
        final RectF sliceBounds = new RectF();
        matrix.mapRect(sliceBounds, localPageSliceBounds);
        return sliceBounds;
    }

    private boolean setDecodingNow(final boolean decodingNow) {
        if (this.decodingNow.compareAndSet(!decodingNow, decodingNow)) {
            if (decodingNow) {
                base.getDecodingProgressModel().increase();
            } else {
                base.getDecodingProgressModel().decrease();
            }
            return true;
        }
        return false;
    }

    private Rect getTargetRect() {
        if (targetRect == null) {
            matrix.reset();
            matrix.postScale(page.getBounds().width() * page.getTargetRectScale(), page.getBounds().height());
            matrix.postTranslate(page.getBounds().left - page.getBounds().width() * page.getTargetTranslate(),
                    page.getBounds().top);
            final RectF targetRectF = new RectF();
            matrix.mapRect(targetRectF, pageSliceBounds);
            targetRect = new Rect((int) targetRectF.left, (int) targetRectF.top, (int) targetRectF.right,
                    (int) targetRectF.bottom);
        }
        return targetRect;
    }

    private void stopDecodingThisNode(final String reason) {
        if (setDecodingNow(false)) {
            base.getDecodeService().stopDecoding(this, reason);
        }
    }

    private boolean isHiddenByChildren() {
        if (children == null) {
            return false;
        }
        for (final PageTreeNode child : children) {
            if (child.getBitmap() == null) {
                return false;
            }
        }
        return true;
    }

    private void recycleChildren() {
        if (children != null) {
            for (final PageTreeNode child : children) {
                child.recycle();
            }
            children = null;
        }
    }

    private boolean containsBitmaps() {
        return getBitmap() != null || childrenContainBitmaps();
    }

    private boolean childrenContainBitmaps() {
        if (children != null) {
            for (final PageTreeNode child : children) {
                if (child.containsBitmaps()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void recycle() {
        stopDecodingThisNode("node recycling");
        holder.recycle();
        recycleChildren();
    }

    public int getPageIndex() {
        return page.getIndex();
    }

    @Override
    public int hashCode() {
        return ((page == null) ? 0 : page.getIndex());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof PageTreeNode) {
            final PageTreeNode that = (PageTreeNode) obj;
            if (this.page == null) {
                return that.page == null;
            }
            return this.page.getIndex() == that.getPageIndex() && this.pageSliceBounds.equals(that.pageSliceBounds);
        }

        return false;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("PageTreeNode");
        buf.append("[");

        buf.append("id").append("=").append(id);
        buf.append(", ");
        buf.append("page").append("=").append(page.getIndex());
        buf.append(", ");
        buf.append("rect").append("=").append(this.pageSliceBounds);
        buf.append(", ");
        buf.append("hasBitmap").append("=").append(getBitmap() != null);

        buf.append("]");
        return buf.toString();
    }

    public int getDocumentPageIndex() {
        return page.getDocumentPageIndex();
    }

    class BitmapHolder {

        Bitmap bitmap;
        SoftReference<Bitmap> bitmapWeakReference;

        public Bitmap getBitmap() {
            Bitmap bmp = bitmap;
            if (bmp == null) {
                bmp = bitmapWeakReference != null ? bitmapWeakReference.get() : null;
            }
            if (bmp != null && !bmp.isRecycled()) {
                bitmapWeakReference = new SoftReference<Bitmap>(bmp);
                return bmp;
            } else {
                bitmap = null;
                bitmapWeakReference = null;
            }
            return null;
        }

        public void clearDirectRef() {
            if (bitmap != null) {
                if (LCTX.isDebugEnabled()) {
                    LCTX.d("Clear bitmap reference: " + PageTreeNode.this);
                }
                bitmapWeakReference = new SoftReference<Bitmap>(bitmap);
                bitmap = null;
            }
        }

        public void recycle() {
            if (bitmap != null && !bitmap.isRecycled()) {
                if (LCTX.isDebugEnabled()) {
                    LCTX.d("Recycle bitmap reference: " + PageTreeNode.this);
                }
                bitmap.recycle();
            }
            bitmap = null;
            bitmapWeakReference = null;
        }

        public void setBitmap(final Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            if (bitmap.getWidth() == -1 && bitmap.getHeight() == -1) {
                return;
            }
            if (this.bitmap != bitmap) {
                recycle();
                this.bitmap = bitmap;
                this.bitmapWeakReference = new SoftReference<Bitmap>(bitmap);
                base.getView().postInvalidate();
            } else {
                this.bitmapWeakReference = new SoftReference<Bitmap>(bitmap);
            }
        }
    }
}
