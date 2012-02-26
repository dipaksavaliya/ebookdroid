package org.ebookdroid.core;

import org.ebookdroid.common.settings.SettingsManager;

import java.util.Queue;

public abstract class AbstractEventZoom<E extends AbstractEventZoom<E>> extends AbstractEvent {

    private final Queue<E> eventQueue;

    public float oldZoom;
    public float newZoom;

    public PageTreeLevel oldLevel;
    public PageTreeLevel newLevel;

    public boolean committed;

    protected AbstractEventZoom(Queue<E> eventQueue) {
        this.eventQueue = eventQueue;
    }

    final void init(final AbstractViewController ctrl, final float oldZoom, final float newZoom, final boolean committed) {
        this.viewState = new ViewState(ctrl, newZoom);
        this.ctrl = ctrl;
        this.model = viewState.model;
        this.view = viewState.view;

        this.oldZoom = oldZoom;
        this.newZoom = newZoom;

        this.oldLevel = PageTreeLevel.getLevel(oldZoom);
        this.newLevel = PageTreeLevel.getLevel(newZoom);

        this.committed = committed;

    }

    @SuppressWarnings("unchecked")
    final void release() {
        this.ctrl = null;
        this.model = null;
        this.viewState = null;
        this.oldLevel = null;
        this.newLevel = null;
        this.bitmapsToRecycle.clear();
        this.nodesToDecode.clear();
        eventQueue.offer((E) this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.AbstractEvent#process()
     */
    @Override
    public final ViewState process() {
        try {
            if (!committed) {
                view.invalidateScroll(newZoom, oldZoom);
                viewState = new ViewState(ctrl);
            }

            viewState = super.process();

            if (!committed) {
                ctrl.redrawView(viewState);
            } else {
                SettingsManager.zoomChanged(newZoom, true);
                ctrl.updatePosition(model.getCurrentPageObject(), viewState);
            }
            return viewState;
        } finally {
            release();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.PageTree)
     */
    @Override
    public final boolean process(final PageTree nodes) {
        return process(nodes, newLevel);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.AbstractEvent#calculatePageVisibility(org.ebookdroid.core.ViewState)
     */
    @Override
    protected final ViewState calculatePageVisibility(final ViewState initial) {
        final int viewIndex = model.getCurrentViewPageIndex();
        int firstVisiblePage = viewIndex;
        int lastVisiblePage = viewIndex;

        final Page[] pages = model.getPages();

        while (firstVisiblePage > 0) {
            final int index = firstVisiblePage - 1;
            if (!ctrl.isPageVisible(pages[index], initial)) {
                break;
            }
            firstVisiblePage = index;
        }
        while (lastVisiblePage < pages.length - 1) {
            final int index = lastVisiblePage + 1;
            if (!ctrl.isPageVisible(pages[index], initial)) {
                break;
            }
            lastVisiblePage = index;
        }

        return new ViewState(initial, firstVisiblePage, lastVisiblePage);
    }
}
