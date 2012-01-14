package org.ebookdroid.xpsdroid.codec;

import org.ebookdroid.core.OutlineLink;

import java.util.ArrayList;
import java.util.List;

public class XpsOutline {

    private long docHandle;

    public List<OutlineLink> getOutline(final long dochandle) {
        final List<OutlineLink> ls = new ArrayList<OutlineLink>();
        docHandle = dochandle;
        final long outline = open(dochandle);
        ttOutline(ls, outline, 0);
        // WARN: Possible memory leak here.
        // free(dochandle);
        return ls;
    }

    private void ttOutline(final List<OutlineLink> ls, long outline, int level) {
        while (outline > 0) {
            final String title = getTitle(outline);
            final String link = getLink(outline, docHandle);
            OutlineLink lnk = null;
            if (title != null) {
                lnk = new OutlineLink(title, link, level);
                ls.add(lnk);
            }
            final long child = getChild(outline);
            ttOutline(lnk != null ? lnk.children : ls, child, level + 1);
            outline = getNext(outline);
        }
    }

    private static native String getTitle(long outlinehandle);

    private static native String getLink(long outlinehandle, long dochandle);

    private static native long getNext(long outlinehandle);

    private static native long getChild(long outlinehandle);

    private static native long open(long dochandle);

    private static native void free(long dochandle);
}
