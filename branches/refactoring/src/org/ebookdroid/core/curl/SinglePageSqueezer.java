package org.ebookdroid.core.curl;

import org.ebookdroid.core.EventDraw;
import org.ebookdroid.core.Page;
import org.ebookdroid.core.SinglePageController;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class SinglePageSqueezer extends AbstractPageSlider {

    public SinglePageSqueezer(final SinglePageController singlePageDocumentView) {
        super(PageAnimationType.SQUEEZER, singlePageDocumentView);
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

            final Canvas canvas = event.canvas;
            final RectF viewRect = event.viewState.viewRect;

            final Rect src = new Rect(0, 0, (int) viewRect.width(), (int) viewRect.height());
            final RectF dst = new RectF(0, 0, viewRect.width() - mA.x, viewRect.height());

            final Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            paint.setDither(true);

            canvas.drawBitmap(foreBitmap.getBitmap(), src, dst, paint);
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

            final Canvas canvas = event.canvas;
            final RectF viewRect = event.viewState.viewRect;

            final Rect src = new Rect(0, 0, (int) viewRect.width(), (int) viewRect.height());
            final RectF dst = new RectF(viewRect.width() - mA.x, 0, viewRect.width(), viewRect.height());

            final Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            paint.setDither(true);

            canvas.drawBitmap(backBitmap.getBitmap(), src, dst, paint);
        }

    }

}
