package org.ebookdroid.core;

import org.ebookdroid.R;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.ui.viewer.IActivityController;
import org.ebookdroid.ui.viewer.views.DragMark;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;

import org.emdev.ui.hwa.IHardwareAcceleration;

public abstract class AbstractScrollController extends AbstractViewController {

    protected static Bitmap dragBitmap;

    protected AbstractScrollController(final IActivityController base, DocumentViewMode mode) {
        super(base, mode);
        if (dragBitmap == null) {
            dragBitmap = BitmapFactory.decodeResource(base.getContext().getResources(), R.drawable.drag);
        }
        IHardwareAcceleration.Factory.getInstance().setMode(getView().getView(),
                SettingsManager.getAppSettings().isHWAEnabled(), true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#goToPage(int)
     */
    public final ViewState goToPage(final int toPage) {
        return new EventGotoPage(this, toPage).process();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#goToPage(int, float, float)
     */
    public final ViewState goToPage(final int toPage, final float offsetX, final float offsetY) {
        return new EventGotoPage(this, toPage, offsetX, offsetY).process();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#drawView(org.ebookdroid.core.EventDraw)
     */
    @Override
    public final void drawView(final EventDraw eventDraw) {
        ViewState viewState = eventDraw.viewState;
        if (viewState.model == null) {
            return;
        }

        for (final Page page : viewState.pages.getVisiblePages()) {
            if (page != null) {
                eventDraw.process(page);
            }
        }

        if (SettingsManager.getAppSettings().getShowAnimIcon()) {
            DragMark.draw(eventDraw.canvas, viewState);
        }
        view.continueScroll();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.AbstractViewController#onLayoutChanged(boolean, boolean, android.graphics.Rect,
     *      android.graphics.Rect)
     */
    @Override
    public final boolean onLayoutChanged(final boolean layoutChanged, final boolean layoutLocked, final Rect oldLaout,
            final Rect newLayout) {
        final DocumentModel dm = base.getDocumentModel();
        final BookSettings bs = SettingsManager.getBookSettings();
        if (dm == null || bs == null) {
            return false;
        }

        int page = dm.getCurrentViewPageIndex();
        float offsetX = bs.offsetX;
        float offsetY = bs.offsetY;

        if (super.onLayoutChanged(layoutChanged, layoutLocked, oldLaout, newLayout)) {
            if (isShown && layoutChanged) {
                goToPage(page, offsetX, offsetY);
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#onScrollChanged(int, int)
     */
    @Override
    public final void onScrollChanged(final int dX, final int dY) {
        if (inZoom.get()) {
            return;
        }

        EventPool.newEventScroll(this, mode == DocumentViewMode.VERTICALL_SCROLL ? dY : dX).process();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.AbstractViewController#isPageVisible(org.ebookdroid.core.Page,
     *      org.ebookdroid.core.ViewState)
     */
    @Override
    public final boolean isPageVisible(final Page page, final ViewState viewState) {
        return RectF.intersects(viewState.viewRect, viewState.getBounds(page));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#pageUpdated(org.ebookdroid.core.ViewState,
     *      org.ebookdroid.core.Page)
     */
    @Override
    public void pageUpdated(final ViewState viewState, final Page page) {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#updateAnimationType()
     */
    @Override
    public void updateAnimationType() {
    }
}
