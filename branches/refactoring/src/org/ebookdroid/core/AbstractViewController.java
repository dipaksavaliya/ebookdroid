package org.ebookdroid.core;

import org.ebookdroid.R;
import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.core.touch.DefaultGestureDetector;
import org.ebookdroid.core.touch.IGestureDetector;
import org.ebookdroid.core.touch.IMultiTouchZoom;
import org.ebookdroid.core.touch.TouchManager;
import org.ebookdroid.ui.viewer.IActivityController;
import org.ebookdroid.ui.viewer.IActivityController.IBookLoadTask;
import org.ebookdroid.ui.viewer.IView;
import org.ebookdroid.ui.viewer.IViewController;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.emdev.ui.actions.AbstractComponentController;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.actions.ActionMethodDef;
import org.emdev.ui.actions.ActionTarget;
import org.emdev.ui.actions.params.Constant;

@ActionTarget(
// action list
actions = {
        // actions
        @ActionMethodDef(id = R.id.actions_verticalConfigScrollUp, method = "verticalConfigScroll"),
        @ActionMethodDef(id = R.id.actions_verticalConfigScrollDown, method = "verticalConfigScroll")
// no more
})
public abstract class AbstractViewController extends AbstractComponentController<IView> implements IViewController {

    protected static final LogContext LCTX = LogContext.ROOT.lctx("View", true);

    public static final int DOUBLE_TAP_TIME = 500;

    protected final IActivityController base;

    protected final IView view;

    protected final DocumentViewMode mode;

    protected boolean isInitialized = false;

    protected boolean isShown = false;

    protected final AtomicBoolean inZoom = new AtomicBoolean();

    protected final PageIndex pageToGo;

    protected int firstVisiblePage;

    protected int lastVisiblePage;

    protected boolean layoutLocked;

    private List<IGestureDetector> detectors;

    public AbstractViewController(final IActivityController baseActivity, final DocumentViewMode mode) {
        super(baseActivity.getActivity(), baseActivity.getActionController(), baseActivity.getView());

        this.base = baseActivity;
        this.view = base.getView();
        this.mode = mode;

        this.firstVisiblePage = -1;
        this.lastVisiblePage = -1;

        this.pageToGo = SettingsManager.getBookSettings().getCurrentPage();

        createAction(R.id.actions_verticalConfigScrollUp, new Constant("direction", -1));
        createAction(R.id.actions_verticalConfigScrollDown, new Constant("direction", +1));
    }

    protected List<IGestureDetector> getGestureDetectors() {
        if (detectors == null) {
            detectors = initGestureDetectors(new ArrayList<IGestureDetector>(4));
        }
        return detectors;
    }

    protected List<IGestureDetector> initGestureDetectors(final List<IGestureDetector> list) {
        list.add(IMultiTouchZoom.Factory.createImpl(base.getZoomModel()));
        list.add(new DefaultGestureDetector(base.getContext(), new GestureListener()));
        return list;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#getView()
     */
    @Override
    public final IView getView() {
        return view;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#getBase()
     */
    @Override
    public final IActivityController getBase() {
        return base;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#init(org.ebookdroid.ui.viewer.IActivityController.IBookLoadTask)
     */
    @Override
    public final void init(final IBookLoadTask task) {
        if (!isInitialized) {
            try {
                getBase().getDocumentModel().initPages(base, task);
            } finally {
                isInitialized = true;
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#show()
     */
    @Override
    public final void show() {
        if (isInitialized && !isShown) {
            isShown = true;
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Showing view content...");
            }

            invalidatePageSizes(InvalidateSizeReason.INIT, null);

            final BookSettings bs = SettingsManager.getBookSettings();
            final Page page = pageToGo.getActualPage(base.getDocumentModel(), bs);
            final int toPage = page != null ? page.index.viewIndex : 0;

            new EventScrollTo(this, toPage).process();
            goToPageImpl(toPage, bs.offsetX, bs.offsetY);

        } else {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("View is not initialized yet");
            }
        }
    }

    protected abstract void goToPageImpl(final int toPage);

    protected final void updatePosition(final DocumentModel dm, final Page page, final ViewState viewState) {
        final int left = view.getScrollX();
        final int top = view.getScrollY();

        final RectF cpBounds = viewState.getBounds(page);
        final float offsetX = (left - cpBounds.left) / cpBounds.width();
        final float offsetY = (top - cpBounds.top) / cpBounds.height();
        SettingsManager.positionChanged(offsetX, offsetY);
    }

    protected void goToPageImpl(final int toPage, final float offsetX, final float offsetY) {
        final DocumentModel dm = getBase().getDocumentModel();
        final int pageCount = dm.getPageCount();
        if (toPage >= 0 && toPage < pageCount) {
            final Page page = dm.getPageObject(toPage);
            if (page != null) {
                dm.setCurrentPageIndex(page.index);
                final RectF bounds = page.getBounds(getBase().getZoomModel().getZoom());
                final float left = bounds.left + offsetX * bounds.width();
                final float top = bounds.top + offsetY * bounds.height();
                view.scrollTo((int) left, (int) top);
            } else {
                if (LCTX.isDebugEnabled()) {
                    LCTX.d("goToPageImpl(): No page found for index: " + toPage);
                }
            }
        } else {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("goToPageImpl(): Bad page index: " + toPage + ", page count: " + pageCount);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#onScrollChanged(int, int)
     */
    @Override
    public void onScrollChanged(final int dX, final int dY) {
        if (inZoom.get()) {
            return;
        }

        final int d = mode == DocumentViewMode.VERTICALL_SCROLL ? dY : dX;

        final AbstractEventScroll cmd = d > 0 ? new EventScrollDown(this) : new EventScrollUp(this);

        final Runnable r = new Runnable() {

            @Override
            public void run() {
                final ViewState viewState = cmd.process();
                final DocumentModel dm = getBase().getDocumentModel();
                final Page page = dm.getPageObject(viewState.currentIndex);
                if (page != null) {
                    dm.setCurrentPageIndex(page.index);
                    updatePosition(dm, page, viewState);
                    view.redrawView(viewState);
                }
            }
        };
        base.getActivity().runOnUiThread(r);
    }

    protected void pageUpdated(final int viewIndex) {
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.events.ZoomListener#zoomChanged(float, float, boolean)
     */
    @Override
    public final void zoomChanged(final float oldZoom, final float newZoom, final boolean committed) {
        if (!isShown) {
            return;
        }

        inZoom.set(!committed);
        final AbstractEventZoom cmd = newZoom > oldZoom ? new EventZoomIn(this, oldZoom, newZoom, committed)
                : new EventZoomOut(this, oldZoom, newZoom, committed);

        if (!committed) {
            view.invalidateScroll(newZoom, oldZoom);
        }
        final ViewState newState = cmd.process();
        if (!committed) {
            redrawView(newState);
        } else {
            SettingsManager.zoomChanged(newZoom, true);
            updatePosition(cmd.model, cmd.model.getCurrentPageObject(), newState);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#updateMemorySettings()
     */
    @Override
    public final void updateMemorySettings() {
        new EventZoomIn(this).process();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#goToPage(int)
     */
    @Override
    public final void goToPage(final int toPage) {
        if (isShown) {
            goToPageImpl(toPage);
        }
    }

    public int getScrollX() {
        return view.getScrollX();
    }

    public int getWidth() {
        return view.getWidth();
    }

    public int getScrollY() {
        return view.getScrollY();
    }

    public int getHeight() {
        return view.getHeight();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#dispatchKeyEvent(android.view.KeyEvent)
     */
    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_VOLUME_UP:
                    verticalConfigScroll(-1);
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    verticalConfigScroll(1);
                    return true;
            }
        }
        // Avoid sound
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public final boolean onTouchEvent(final MotionEvent ev) {
        try {
            Thread.sleep(16);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }

        for (final IGestureDetector d : getGestureDetectors()) {
            if (d.enabled() && d.onTouchEvent(ev)) {
                return true;
            }
        }
        return false;

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#onLayoutChanged(boolean, boolean, android.graphics.Rect,
     *      android.graphics.Rect)
     */
    @Override
    public boolean onLayoutChanged(final boolean layoutChanged, final boolean layoutLocked, final Rect oldLaout,
            final Rect newLayout) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onLayoutChanged(" + layoutChanged + ", " + layoutLocked + "," + oldLaout + ", " + newLayout + ")");
        }
        if (layoutChanged && !layoutLocked) {
            if (isShown) {
                final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
                for (final Page page : base.getDocumentModel().getPages()) {
                    page.nodes.recycleAll(bitmapsToRecycle, true);
                }
                BitmapManager.release(bitmapsToRecycle);

                invalidatePageSizes(InvalidateSizeReason.LAYOUT, null);
                invalidateScroll();

                new EventZoomIn(this).process();
                return true;
            }
        }
        return false;
    }

    @Override
    public void toggleNightMode(final boolean nightMode) {
        final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
        for (final Page page : base.getDocumentModel().getPages()) {
            page.nodes.recycleAll(bitmapsToRecycle, true);
        }
        BitmapManager.release(bitmapsToRecycle);

        new EventZoomIn(this).process();
    }

    protected final void invalidateScroll() {
        if (!isShown) {
            return;
        }
        view.invalidateScroll();
    }

    /**
     * Sets the page align flag.
     * 
     * @param align
     *            the new flag indicating align
     */
    @Override
    public final void setAlign(final PageAlign align) {
        invalidatePageSizes(InvalidateSizeReason.PAGE_ALIGN, null);
        invalidateScroll();
        new EventZoomIn(this).process();
    }

    /**
     * Checks if view is initialized.
     * 
     * @return true, if is initialized
     */
    protected final boolean isShown() {
        return isShown;
    }

    protected abstract boolean isPageVisibleImpl(final Page page, final ViewState viewState);

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#getFirstVisiblePage()
     */
    @Override
    public final int getFirstVisiblePage() {
        return firstVisiblePage;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#getLastVisiblePage()
     */
    @Override
    public final int getLastVisiblePage() {
        return lastVisiblePage;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#redrawView()
     */
    @Override
    public final void redrawView() {
        view.redrawView(new ViewState(this));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.ui.viewer.IViewController#redrawView(org.ebookdroid.core.ViewState)
     */
    @Override
    public final void redrawView(final ViewState viewState) {
        view.redrawView(viewState);
    }

    @ActionMethod(ids = { R.id.actions_verticalConfigScrollUp, R.id.actions_verticalConfigScrollDown })
    public void verticalConfigScroll(final ActionEx action) {
        final Integer direction = action.getParameter("direction");
        verticalConfigScroll(direction);
    }

    protected boolean processTap(final TouchManager.Touch type, final MotionEvent e) {
        final Integer actionId = TouchManager.getAction(type, e.getX(), e.getY(), getWidth(), getHeight());
        final ActionEx action = actionId != null ? getOrCreateAction(actionId) : null;
        if (action != null) {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Touch action: " + action.name + ", " + action.getMethod().toString());
            }
            action.run();
            return true;
        } else {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Touch action not found");
            }
        }
        return false;
    }

    protected class GestureListener extends SimpleOnGestureListener {

        /**
         * {@inheritDoc}
         * 
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
         */
        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            // LCTX.d("onDoubleTap(" + e + ")");
            return processTap(TouchManager.Touch.DoubleTap, e);
        }

        /**
         * {@inheritDoc}
         * 
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDown(android.view.MotionEvent)
         */
        @Override
        public boolean onDown(final MotionEvent e) {
            view.forceFinishScroll();
            // LCTX.d("onDown(" + e + ")");
            return true;
        }

        /**
         * {@inheritDoc}
         * 
         * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent,
         *      android.view.MotionEvent, float, float)
         */
        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float vX, final float vY) {
            final Rect l = getScrollLimits();
            float x = vX, y = vY;
            if (Math.abs(vX / vY) < 0.5) {
                x = 0;
            }
            if (Math.abs(vY / vX) < 0.5) {
                y = 0;
            }
            // LCTX.d("onFling(" + x + ", " + y + ")");
            view.startFling(x, y, l);
            view.redrawView();
            return true;
        }

        /**
         * {@inheritDoc}
         * 
         * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent,
         *      android.view.MotionEvent, float, float)
         */
        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
            float x = distanceX, y = distanceY;
            if (Math.abs(distanceX / distanceY) < 0.5) {
                x = 0;
            }
            if (Math.abs(distanceY / distanceX) < 0.5) {
                y = 0;
            }
            // LCTX.d("onScroll(" + x + ", " + y + ")");
            view.scrollBy((int) x, (int) y);
            return true;
        }

        /**
         * {@inheritDoc}
         * 
         * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
         */
        @Override
        public boolean onSingleTapConfirmed(final MotionEvent e) {
            // LCTX.d("onSingleTapConfirmed(" + e + ")");
            return processTap(TouchManager.Touch.SingleTap, e);
        }

        /**
         * {@inheritDoc}
         * 
         * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
         */
        @Override
        public void onLongPress(final MotionEvent e) {
            // LongTap operation cause side-effects
            // processTap(TouchManager.Touch.LongTap, e);
        }
    }
}
