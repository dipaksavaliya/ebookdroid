package org.ebookdroid.core.codec;

import android.graphics.RectF;

public class PageLink extends RectF {

    /**
     * 1 - rect
     * 2 - oval
     * 3 - poly
     */
    public final int rectType;
    public final String url;

    PageLink(final String l, final int type, final int[] data) {
        super(data[0], data[1], data[2], data[3]);
        rectType = type;
        url = l;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(this.getClass().getSimpleName());
        buf.append("[");
        buf.append("url").append("=").append(url);
        buf.append(", ");
        buf.append("type").append("=").append(rectType);
        buf.append(": ");
        buf.append(super.toString());
        buf.append("]");
        return buf.toString();
    }

}
