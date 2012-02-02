package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.BitmapRef;
import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.types.DecodeMode;
import org.ebookdroid.core.codec.CodecPage;
import org.ebookdroid.core.crop.PageCropper;
import org.ebookdroid.core.models.DecodingProgressModel;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.ui.viewer.IViewController;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PageTreeNode implements DecodeService.DecodeCallback {

    private static final LogContext LCTX = Page.LCTX;

    final Page page;
    final PageTreeNode parent;
    final long id;
    final int level;
    final String shortId;
    final String fullId;

    final AtomicBoolean decodingNow = new AtomicBoolean();
    final BitmapHolder holder = new BitmapHolder();

    final float childrenZoomThreshold;
    final RectF pageSliceBounds;

    float bitmapZoom = 1;
    RectF croppedBounds = null;

    PageTreeNode(final Page page, final float childrenZoomThreshold) {
        assert page != null;

        this.page = page;
        this.parent = null;
        this.id = 0;
        this.level = 0;
        this.shortId = page.index.viewIndex + ":0";
        this.fullId = page.index + ":0";
        this.pageSliceBounds = page.type.getInitialRect();
        this.croppedBounds = null;
        this.childrenZoomThreshold = childrenZoomThreshold;
    }

    PageTreeNode(final Page page, final PageTreeNode parent, final long id, final RectF localPageSliceBounds,
            final float childrenZoomThreshold) {
        assert id != 0;
        assert page != null;
        assert parent != null;

        this.page = page;
        this.parent = parent;
        this.id = id;
        this.level = parent.level + 1;
        this.shortId = page.index.viewIndex + ":" + id;
        this.fullId = page.index + ":" + id;
        this.pageSliceBounds = evaluatePageSliceBounds(localPageSliceBounds, parent);
        this.croppedBounds = evaluateCroppedPageSliceBounds(localPageSliceBounds, parent);
        this.childrenZoomThreshold = childrenZoomThreshold;
    }

    @Override
    protected void finalize() throws Throwable {
        holder.recycle(null);
    }

    public boolean recycle(final List<Bitmaps> bitmapsToRecycle) {
        stopDecodingThisNode("node recycling");
        return holder.recycle(bitmapsToRecycle);
    }

    public boolean recycleWithChildren(final List<Bitmaps> bitmapsToRecycle) {
        stopDecodingThisNode("node recycling");
        boolean res = holder.recycle(bitmapsToRecycle);
        if (id == 0) {
            res |= page.nodes.recycleAll(bitmapsToRecycle, false);
        } else {
            res |= page.nodes.recycleChildren(this, bitmapsToRecycle);
        }
        return res;
    }

    protected boolean isReDecodingRequired(final boolean committed, final ViewState viewState) {
        return (committed && viewState.zoom != bitmapZoom) || viewState.zoom > 1.2 * bitmapZoom;
    }

    protected void onChildLoaded(final PageTreeNode child, final ViewState viewState, final RectF bounds) {
        if (viewState.decodeMode == DecodeMode.LOW_MEMORY || viewState.zoom > 1.5) {
            boolean hiddenByChildren = page.nodes.isHiddenByChildren(this, viewState, bounds);
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Node " + fullId + "is: " + (hiddenByChildren ? "" : "not") + " hidden by children");
            }
            if (!viewState.isNodeVisible(this, bounds) || hiddenByChildren) {
                final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
                this.recycle(bitmapsToRecycle);
                for (PageTreeNode parent = this.parent; parent != null; parent = parent.parent) {
                    parent.recycle(bitmapsToRecycle);
                }
                BitmapManager.release(bitmapsToRecycle);
                if (LCTX.isDebugEnabled()) {
                    LCTX.d("Recycle parent nodes for: " + child.fullId + " : " + bitmapsToRecycle.size());
                }
            }
        }
    }

    protected boolean isChildrenRequired(final ViewState viewState) {
        if (viewState.decodeMode == DecodeMode.NORMAL && viewState.zoom >= childrenZoomThreshold) {
            return true;
        }

        final DecodeService ds = page.base.getDecodeService();
        if (ds == null) {
            return false;
        }

        long memoryLimit = Long.MAX_VALUE;
        if (viewState.decodeMode == DecodeMode.LOW_MEMORY) {
            memoryLimit = Math.min(memoryLimit, SettingsManager.getAppSettings().getMaxImageSize());
        }
        memoryLimit = Math.max(64 * 1024, memoryLimit);

        final Rect rect = getActualRect(viewState, ds);

        final long size = BitmapManager.getBitmapBufferSize(rect.width(), rect.height(), ds.getBitmapConfig());

        final boolean textureSizeExceedeed = (rect.width() > 2048) || (rect.height() > 2048);
        final boolean memoryLimitExceeded = size + 4096 >= memoryLimit;

        return textureSizeExceedeed || memoryLimitExceeded;
    }

    private Rect getActualRect(final ViewState viewState, final DecodeService ds) {
        final RectF actual = croppedBounds != null ? croppedBounds : pageSliceBounds;
        final float widthScale = page.getTargetRectScale();
        final float pageWidth = page.bounds.width() * widthScale;

        if (viewState.decodeMode == DecodeMode.NATIVE_RESOLUTION) {
            return ds.getNativeSize(pageWidth, page.bounds.height(), actual, widthScale);
        }

        return ds.getScaledSize(viewState, pageWidth, page.bounds.height(), actual, widthScale, getSliceGeneration());
    }

    public int getSliceGeneration() {
        return (int) (parent != null ? parent.childrenZoomThreshold : 1);
    }

    protected void decodePageTreeNode(final List<PageTreeNode> nodesToDecode, final ViewState viewState) {
        if (setDecodingNow(true)) {
            bitmapZoom = viewState.zoom;
            nodesToDecode.add(this);
        }
    }

    @Override
    public void decodeComplete(final CodecPage codecPage, final BitmapRef bitmap, final Rect bitmapBounds) {

        try {
            if (bitmap == null || bitmapBounds == null) {
                page.base.getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        setDecodingNow(false);
                    }
                });
                return;
            }

            BookSettings bs = SettingsManager.getBookSettings();
            if (bs != null && bs.cropPages) {
                if (id == 0 && croppedBounds == null) {
                    croppedBounds = PageCropper.getCropBounds(bitmap, bitmapBounds, pageSliceBounds);
                    final DecodeService decodeService = page.base.getDecodeService();
                    if (decodeService != null) {
                        if (LCTX.isDebugEnabled()) {
                            LCTX.d(fullId + ": cropped image requested: " + croppedBounds);
                        }
                        decodeService.decodePage(new ViewState(PageTreeNode.this), PageTreeNode.this);
                    }
                }
            }

            final Bitmaps bitmaps = holder.reuse(fullId, bitmap, bitmapBounds);

            page.base.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    holder.setBitmap(bitmaps);
                    setDecodingNow(false);

                    final IViewController dc = page.base.getDocumentController();
                    final DocumentModel dm = page.base.getDocumentModel();

                    if (dc != null && dm != null) {
                        ViewState viewState = dc.updatePageSize(dm, page, bitmapBounds);

                        final RectF bounds = viewState.getBounds(page);
                        if (parent != null) {
                            parent.onChildLoaded(PageTreeNode.this, viewState, bounds);
                        }

                        if (viewState.isNodeVisible(PageTreeNode.this, bounds)) {
                            dc.redrawView(viewState);
                        }
                    }
                }
            });
        } catch (OutOfMemoryError ex) {
            LCTX.e("No memory: ", ex);
            BitmapManager.clear("PageTreeNode OutOfMemoryError: ");
            page.base.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    setDecodingNow(false);
                }
            });
        } finally {
            BitmapManager.release(bitmap);
        }
    }

    private boolean setDecodingNow(final boolean decodingNow) {
        if (this.decodingNow.compareAndSet(!decodingNow, decodingNow)) {
            final DecodingProgressModel dpm = page.base.getDecodingProgressModel();
            if (dpm != null) {
                if (decodingNow) {
                    dpm.increase();
                } else {
                    dpm.decrease();
                }
            }
            return true;
        }
        return false;
    }

    void stopDecodingThisNode(final String reason) {
        if (setDecodingNow(false)) {
            final DecodeService ds = page.base.getDecodeService();
            if (ds != null) {
                ds.stopDecoding(this, reason);
            }
        }
    }

    void draw(final Canvas canvas, final ViewState viewState, final RectF pageBounds, final PagePaint paint) {
        final RectF tr = getTargetRect(viewState, viewState.viewRect, pageBounds);
        if (!viewState.isNodeVisible(this, pageBounds)) {
            return;
        }

        if (!page.nodes.allChildrenHasBitmap(viewState, this, paint)) {
            holder.drawBitmap(viewState, canvas, paint, tr);

            drawBrightnessFilter(canvas, tr);
        }

        page.nodes.drawChildren(canvas, viewState, pageBounds, this, paint);
    }

    void drawBrightnessFilter(final Canvas canvas, final RectF tr) {
        final int brightness = SettingsManager.getAppSettings().getBrightness();
        if (brightness < 100) {
            final Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setAlpha(255 - brightness * 255 / 100);
            canvas.drawRect(tr, p);
        }
    }

    public RectF getTargetRect(final ViewState viewState, final RectF viewRect, final RectF pageBounds) {
        final Matrix matrix = new Matrix();

        final RectF bounds = viewState.view.getAdjustedPageBounds(viewState, pageBounds);

        matrix.postScale(bounds.width() * page.getTargetRectScale(), bounds.height());
        matrix.postTranslate(bounds.left - bounds.width() * page.type.getLeftPos(), bounds.top);

        final RectF targetRectF = new RectF();
        matrix.mapRect(targetRectF, pageSliceBounds);
        return new RectF(targetRectF);
    }

    private static RectF evaluatePageSliceBounds(final RectF localPageSliceBounds, final PageTreeNode parent) {
        final Matrix matrix = new Matrix();
        matrix.postScale(parent.pageSliceBounds.width(), parent.pageSliceBounds.height());
        matrix.postTranslate(parent.pageSliceBounds.left, parent.pageSliceBounds.top);
        final RectF sliceBounds = new RectF();
        matrix.mapRect(sliceBounds, localPageSliceBounds);
        return sliceBounds;
    }

    private static RectF evaluateCroppedPageSliceBounds(final RectF localPageSliceBounds, final PageTreeNode parent) {
        if (parent.croppedBounds == null) {
            return null;
        }
        final Matrix matrix = new Matrix();
        matrix.postScale(parent.croppedBounds.width(), parent.croppedBounds.height());
        matrix.postTranslate(parent.croppedBounds.left, parent.croppedBounds.top);
        final RectF sliceBounds = new RectF();
        matrix.mapRect(sliceBounds, localPageSliceBounds);
        return sliceBounds;
    }

    @Override
    public int hashCode() {
        return (page == null) ? 0 : page.index.viewIndex;
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
            return this.page.index.viewIndex == that.page.index.viewIndex
                    && this.pageSliceBounds.equals(that.pageSliceBounds);
        }

        return false;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("PageTreeNode");
        buf.append("[");

        buf.append("id").append("=").append(page.index.viewIndex).append(":").append(id);
        buf.append(", ");
        buf.append("rect").append("=").append(this.pageSliceBounds);
        buf.append(", ");
        buf.append("hasBitmap").append("=").append(holder.hasBitmaps());

        buf.append("]");
        return buf.toString();
    }

    class BitmapHolder {

        Bitmaps day;

        public synchronized void drawBitmap(final ViewState viewState, final Canvas canvas, final PagePaint paint,
                final RectF tr) {

            if (day != null) {
                day.draw(viewState, canvas, paint, tr);
            }
        }

        public synchronized Bitmaps reuse(String nodeId, BitmapRef bitmap, Rect bitmapBounds) {
            boolean invert = SettingsManager.getAppSettings().getNightMode();
            if (day != null) {
                if (day.reuse(nodeId, bitmap, bitmapBounds, invert)) {
                    return day;
                }
            }
            return new Bitmaps(nodeId, bitmap, bitmapBounds, invert);
        }

        public synchronized boolean hasBitmaps() {
            return day != null ? day.hasBitmaps() : false;
        }

        public synchronized boolean recycle(final List<Bitmaps> bitmapsToRecycle) {
            if (day != null) {
                if (bitmapsToRecycle != null) {
                    bitmapsToRecycle.add(day);
                } else {
                    BitmapManager.release(Arrays.asList(day));
                }
                day = null;
                return true;
            }
            return false;
        }

        public synchronized void setBitmap(final Bitmaps bitmaps) {
            if (bitmaps == null || bitmaps == day) {
                return;
            }
            recycle(null);
            this.day = bitmaps;
        }
    }

}
