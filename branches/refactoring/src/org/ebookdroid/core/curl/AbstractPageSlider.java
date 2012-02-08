package org.ebookdroid.core.curl;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.BitmapRef;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.core.EventDraw;
import org.ebookdroid.core.Page;
import org.ebookdroid.core.PagePaint;
import org.ebookdroid.core.SinglePageController;
import org.ebookdroid.core.ViewState;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public abstract class AbstractPageSlider extends AbstractPageAnimator {

    public AbstractPageSlider(final PageAnimationType type, final SinglePageController singlePageDocumentView) {
        super(type, singlePageDocumentView);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.curl.AbstractPageAnimator#init()
     */
    @Override
    public void init() {
        super.init();
        mInitialEdgeOffset = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.curl.AbstractPageAnimator#onFirstDrawEvent(android.graphics.Canvas, org.ebookdroid.core.ViewState)
     */
    @Override
    protected void onFirstDrawEvent(final Canvas canvas, final ViewState viewState) {
        lock.writeLock().lock();
        try {
            updateValues();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.curl.AbstractPageAnimator#resetClipEdge()
     */
    @Override
    protected void resetClipEdge() {
        // Set our base movement
        mMovement.x = mInitialEdgeOffset;
        mMovement.y = mInitialEdgeOffset;
        mOldMovement.x = 0;
        mOldMovement.y = 0;

        // Now set the points
        mA = new Vector2D(mInitialEdgeOffset, 0);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.curl.AbstractPageAnimator#updateValues()
     */
    @Override
    protected void updateValues() {
        // Calculate point A
        mA.x = mMovement.x;
        mA.y = 0;
    }

    protected BitmapRef getBitmap(final ViewState viewState, final BitmapRef ref) {
        BitmapRef bitmap = ref;
        final float width = viewState.viewRect.width();
        final float height = viewState.viewRect.height();

        if (ref == null || ref.isRecycled() || ref.width != width || ref.height != height) {
            BitmapManager.release(ref);
            bitmap = BitmapManager.getBitmap("Curler image", (int) width, (int) height, Bitmap.Config.RGB_565);
        }
        final PagePaint paint = viewState.nightMode ? PagePaint.NIGHT : PagePaint.DAY;
        bitmap.getBitmap().eraseColor(paint.backgroundFillPaint.getColor());
        return bitmap;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.curl.AbstractPageAnimator#drawExtraObjects(org.ebookdroid.core.EventDraw)
     */
    @Override
    protected void drawExtraObjects(EventDraw event) {
        if (SettingsManager.getAppSettings().getShowAnimIcon()) {
            event.canvas.drawBitmap(arrowsBitmap, view.getWidth() - arrowsBitmap.getWidth(), view.getHeight()
                    - arrowsBitmap.getHeight(), PAINT);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.curl.AbstractPageAnimator#fixMovement(org.ebookdroid.core.curl.Vector2D, boolean)
     */
    @Override
    protected Vector2D fixMovement(final Vector2D movement, final boolean bMaintainMoveDir) {
        return movement;
    }

    protected final void updateForeBitmap(EventDraw event, Page page) {
        if (foreBitmapIndex != foreIndex || foreBitmap == null) {
            foreBitmap = getBitmap(event.viewState, foreBitmap);

            new EventDraw(event, new Canvas(foreBitmap.getBitmap())).process(page);
            foreBitmapIndex = foreIndex;
        }
    }

    protected final void updateBackBitmap(EventDraw event, Page page) {
        if (backBitmapIndex != backIndex || backBitmap == null) {
            backBitmap = getBitmap(event.viewState, backBitmap);

            new EventDraw(event, new Canvas(backBitmap.getBitmap())).process(page);
            backBitmapIndex = backIndex;
        }
    }
}
