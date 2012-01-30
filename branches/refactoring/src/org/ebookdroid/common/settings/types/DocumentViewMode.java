package org.ebookdroid.common.settings.types;

import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.core.ContiniousDocumentView;
import org.ebookdroid.core.HScrollDocumentView;
import org.ebookdroid.core.SinglePageDocumentView;
import org.ebookdroid.ui.viewer.IActivityController;
import org.ebookdroid.ui.viewer.IViewController;

import java.lang.reflect.Constructor;

public enum DocumentViewMode {

    VERTICALL_SCROLL("Vertical scroll", PageAlign.WIDTH, ContiniousDocumentView.class),

    HORIZONTAL_SCROLL("Horizontal scroll", PageAlign.HEIGHT, HScrollDocumentView.class),

    SINGLE_PAGE("Single page", null, SinglePageDocumentView.class);

    private final LogContext LCTX = LogContext.ROOT.lctx("View");

    /** The resource value. */
    private final String resValue;

    private final PageAlign pageAlign;

    private Constructor<? extends IViewController> c;

    private DocumentViewMode(final String res, final PageAlign pageAlign,
            final Class<? extends IViewController> clazz) {
        this.resValue = res;
        this.pageAlign = pageAlign;
        try {
            this.c = clazz.getConstructor(IActivityController.class);
        } catch (final Exception e) {
            LCTX.e("Cannot find appropriate view controller constructor: ", e);
            this.c = null;
        }
    }

    public IViewController create(final IActivityController base) {
        if (c != null) {
            try {
                return c.newInstance(base);
            } catch (final Exception e) {
                LCTX.e("Cannot find instanciate view controller: ", e);
            }
        }
        return null;
    }

    public String getResValue() {
        return resValue;
    }

    public static PageAlign getPageAlign(final BookSettings bs) {
        if (bs == null || bs.viewMode == null) {
            return PageAlign.AUTO;
        }
        final PageAlign defAlign = bs.viewMode.pageAlign;
        return defAlign != null ? defAlign : bs.pageAlign;
    }

    /**
     * Gets the by resource value.
     * 
     * @param resValue
     *            the resource value
     * @return the enum value or @null
     */
    public static DocumentViewMode getByResValue(final String resValue) {
        for (final DocumentViewMode vm : values()) {
            if (vm.resValue.equals(resValue)) {
                return vm;
            }
        }
        return null;
    }

    public static DocumentViewMode getByOrdinal(final int ord) {
        if (0 <= ord && ord < values().length) {
            return values()[ord];
        }
        return VERTICALL_SCROLL;
    }
}
