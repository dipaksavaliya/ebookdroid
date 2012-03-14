package org.ebookdroid.core.codec;

import android.graphics.PointF;
import android.graphics.RectF;

public class PageLink {

    public String url;

    /**
     * 1 - rect
     * 2 - oval
     * 3 - poly
     */
    public int rectType;
    public RectF sourceRect;

    public int targetPage;
    public PointF targetPoint;

    public PageLink() {
    }

    public PageLink(final String l, final int type, final int[] source) {
        rectType = type;
        url = l;
        sourceRect = new RectF(source[0], source[1], source[2], source[3]);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
        buf.append("[");
        buf.append("url").append("=").append(url);
        buf.append(", ");
        buf.append("type").append("=").append(rectType);
        buf.append(": ");
        buf.append(sourceRect);
        buf.append("]");
        return buf.toString();
    }
}
