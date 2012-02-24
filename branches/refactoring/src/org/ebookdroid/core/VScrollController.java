package org.ebookdroid.core;

import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.ui.viewer.IActivityController;

import android.graphics.Rect;
import android.graphics.RectF;

public class VScrollController extends AbstractScrollController {

    public VScrollController(final IActivityController base) {
        super(base, DocumentViewMode.VERTICALL_SCROLL);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.ui.viewer.IViewController#calculateCurrentPage(org.ebookdroid.core.ViewState)
     */
    @Override
    public final int calculateCurrentPage(final ViewState viewState, final int firstVisible, final int lastVisible) {
        int result = 0;
        long bestDistance = Long.MAX_VALUE;

        final int viewY = Math.round(viewState.viewRect.centerY());

        final Iterable<Page> pages = firstVisible != -1 ? viewState.model.getPages(firstVisible, lastVisible + 1)
                : viewState.model.getPages(0);

        for (final Page page : pages) {
            final RectF bounds = viewState.getBounds(page);
            final int pageY = Math.round(bounds.centerY());
            final long dist = Math.abs(pageY - viewY);
            if (dist < bestDistance) {
                bestDistance = dist;
                result = page.index.viewIndex;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.ui.viewer.IViewController#verticalConfigScroll(int)
     */
    @Override
    public final void verticalConfigScroll(final int direction) {
        final int scrollheight = SettingsManager.getAppSettings().getScrollHeight();
        final int dy = (int) (direction * getHeight() * (scrollheight / 100.0));

        view.startPageScroll(0, dy);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.ui.viewer.IViewController#getScrollLimits()
     */
    @Override
    public final Rect getScrollLimits() {
        final DocumentModel dm = getBase().getDocumentModel();
        if (dm != null) {
            final int width = getWidth();
            final int height = getHeight();
            final Page lpo = dm.getLastPageObject();
            final float zoom = getBase().getZoomModel().getZoom();

            final int bottom = lpo != null ? (int) lpo.getBounds(zoom).bottom - height : 0;
            final int right = (int) (width * zoom) - width;

            return new Rect(0, 0, right, bottom);
        }
        return new Rect(0, 0, 0, 0);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.ui.viewer.IViewController#invalidatePageSizes(org.ebookdroid.ui.viewer.IViewController.InvalidateSizeReason,
     *      org.ebookdroid.core.Page)
     */
    @Override
    public synchronized final void invalidatePageSizes(final InvalidateSizeReason reason, final Page changedPage) {
        if (!isInitialized) {
            return;
        }

        if (reason == InvalidateSizeReason.PAGE_ALIGN) {
            return;
        }

        final DocumentModel model = getBase().getDocumentModel();
        if (model == null) {
            return;
        }

        final int width = getWidth();

        if (changedPage == null) {
            float heightAccum = 0;
            for (final Page page : model.getPages()) {
                final float pageHeight = width / page.getAspectRatio();
                page.setBounds(0, heightAccum, width, heightAccum + pageHeight);
                heightAccum += pageHeight + 1;
            }
        } else {
            float heightAccum = changedPage.getBounds(1.0f).top;
            for (final Page page : model.getPages(changedPage.index.viewIndex)) {
                final float pageHeight = width / page.getAspectRatio();
                page.setBounds(0, heightAccum, width, heightAccum + pageHeight);
                heightAccum += pageHeight + 1;
            }
        }
    }
}
