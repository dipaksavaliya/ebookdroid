package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.types.PageType;
import org.ebookdroid.core.codec.CodecPageInfo;
import org.ebookdroid.ui.viewer.IActivityController;

import android.graphics.RectF;
import android.util.FloatMath;

import java.util.List;

import org.emdev.utils.MathUtils;

public class Page {

    static final LogContext LCTX = LogContext.ROOT.lctx("Page", true);

    public final PageIndex index;
    public final PageType type;

    final IActivityController base;
    final PageTree nodes;

    RectF bounds;
    int aspectRatio;
    boolean recycled;
    float storedZoom;
    RectF zoomedBounds;

    int zoomLevel = 1;

    public Page(final IActivityController base, final PageIndex index, final PageType pt, final CodecPageInfo cpi) {
        this.base = base;
        this.index = index;
        this.type = pt != null ? pt : PageType.FULL_PAGE;
        this.bounds = new RectF(0, 0, cpi.width / type.getWidthScale(), cpi.height);

        setAspectRatio(cpi);

        nodes = new PageTree(this);
    }

    public void recycle(List<Bitmaps> bitmapsToRecycle) {
        recycled = true;
        nodes.recycleAll(bitmapsToRecycle, true);
    }

    public float getAspectRatio() {
        return aspectRatio / 128.0f;
    }

    private boolean setAspectRatio(final float aspectRatio) {
        int newAspectRatio = (int)FloatMath.floor(aspectRatio * 128);
        if (this.aspectRatio != newAspectRatio) {
            this.aspectRatio = newAspectRatio;
            return true;
        }
        return false;
    }

    public boolean setAspectRatio(final CodecPageInfo page) {
        if (page != null) {
            return this.setAspectRatio(page.width / type.getWidthScale(), page.height);
        }
        return false;
    }

    public boolean setAspectRatio(final float width, final float height) {
        return setAspectRatio(width / height);
    }

    public void setBounds(final RectF pageBounds) {
        storedZoom = 0.0f;
        zoomedBounds = null;
        bounds = pageBounds;
    }

    public RectF getBounds(final float zoom) {
        if (zoom != storedZoom) {
            storedZoom = zoom;
            zoomedBounds = MathUtils.zoom(bounds, zoom);
        }
        return zoomedBounds;
    }

    public float getTargetRectScale() {
        return type.getWidthScale();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("Page");
        buf.append("[");

        buf.append("index").append("=").append(index);
        buf.append(", ");
        buf.append("bounds").append("=").append(bounds);
        buf.append(", ");
        buf.append("aspectRatio").append("=").append(aspectRatio);
        buf.append(", ");
        buf.append("type").append("=").append(type.name());
        buf.append("]");
        return buf.toString();
    }

}
