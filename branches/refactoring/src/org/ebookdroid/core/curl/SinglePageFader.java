package org.ebookdroid.core.curl;

import org.ebookdroid.core.EventDraw;
import org.ebookdroid.core.Page;
import org.ebookdroid.core.SinglePageController;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class SinglePageFader extends AbstractPageSlider {

    public SinglePageFader(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.FADER, singlePageDocumentView);
    }

    /**
     * Draw the foreground
     * 
     * @param canvas
     * @param rect
     * @param paint
     */
    @Override
    protected void drawForeground(EventDraw event) {
        Page page = view.getBase().getDocumentModel().getPageObject(foreIndex);
        if (page == null) {
            page = view.getBase().getDocumentModel().getCurrentPageObject();
        }
        if (page != null) {
            event.process(event.viewState, page);
        }
    }

    /**
     * Draw the background image.
     * 
     * @param canvas
     * @param rect
     * @param paint
     */
    @Override
    protected void drawBackground(EventDraw event) {
        final Page page = view.getBase().getDocumentModel().getPageObject(backIndex);
        if (page != null) {

            updateBackBitmap(event, page);

            RectF viewRect = event.viewState.viewRect;

            final Rect src = new Rect(0, 0, (int) viewRect.width(), (int) viewRect.height());
            final RectF dst = new RectF(0, 0, viewRect.width(), viewRect.height());

            final Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setAlpha(255 * (int) mA.x / (int) viewRect.width());

            event.canvas.drawBitmap(backBitmap.getBitmap(), src, dst, paint);
        }

    }

}
