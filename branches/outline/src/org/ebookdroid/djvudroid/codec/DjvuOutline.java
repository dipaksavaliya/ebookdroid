package org.ebookdroid.djvudroid.codec;

import org.ebookdroid.core.OutlineLink;

import java.util.ArrayList;
import java.util.List;

public class DjvuOutline {

    private long docHandle;

    public List<OutlineLink> getOutline(final long dochandle) {
        final List<OutlineLink> ls = new ArrayList<OutlineLink>();
        docHandle = dochandle;
        final long outline = open(docHandle);
        ttOutline(ls, outline, 0);
        return ls;
    }

    private void ttOutline(final List<OutlineLink> ls, long outline, int level) {
        while (expConsp(outline)) {
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

    private static native long open(long dochandle);

    private static native boolean expConsp(long outline);

    private static native String getTitle(long outline);

    private static native String getLink(long outline, long dochandle);

    private static native long getNext(long outline);

    private static native long getChild(long outline);

}
