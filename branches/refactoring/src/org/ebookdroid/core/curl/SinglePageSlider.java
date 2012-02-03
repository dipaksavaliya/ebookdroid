package org.ebookdroid.core.curl;

import org.ebookdroid.core.EventDraw;
import org.ebookdroid.core.Page;
import org.ebookdroid.core.SinglePageController;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class SinglePageSlider extends AbstractPageSlider {

    public SinglePageSlider(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.SLIDER, singlePageDocumentView);
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
            updateForeBitmap(event, page);

            final RectF viewRect = event.viewState.viewRect;

            final Rect src = new Rect((int) mA.x, 0, (int) viewRect.width(), (int) viewRect.height());
            final RectF dst = new RectF(0, 0, viewRect.width() - mA.x, viewRect.height());

            final Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            paint.setDither(true);

            event.canvas.drawBitmap(foreBitmap.getBitmap(), src, dst, paint);
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

            final Rect src = new Rect(0, 0, (int) mA.x, view.getHeight());
            final RectF viewRect = event.viewState.viewRect;
            final RectF dst = new RectF(viewRect.width() - mA.x, 0, viewRect.width(), viewRect.height());

            final Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            paint.setDither(true);

            event.canvas.drawBitmap(backBitmap.getBitmap(), src, dst, paint);
        }

    }

}
