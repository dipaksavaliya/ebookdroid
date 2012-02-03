package org.ebookdroid.core;

public abstract class AbstractEventZoom extends AbstractEvent {

    public final float oldZoom;
    public final float newZoom;

    public final PageTreeLevel oldLevel;
    public final PageTreeLevel newLevel;

    public final boolean committed;

    protected AbstractEventZoom(final AbstractViewController ctrl, final float oldZoom, final float newZoom,
            final boolean committed) {
        super(ctrl);
        this.oldZoom = oldZoom;
        this.newZoom = newZoom;

        this.oldLevel = PageTreeLevel.getLevel(oldZoom);
        this.newLevel = PageTreeLevel.getLevel(newZoom);

        this.committed = committed;
    }

    protected AbstractEventZoom(final AbstractViewController ctrl) {
        super(ctrl);
        this.oldZoom = 0;
        this.newZoom = ctrl.getBase().getZoomModel().getZoom();

        this.oldLevel = PageTreeLevel.ROOT;
        this.newLevel = PageTreeLevel.getLevel(newZoom);

        this.committed = true;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.AbstractEvent#process()
     */
    @Override
    public ViewState process() {
        return process(new ViewState(ctrl, newZoom));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.AbstractEvent#calculatePageVisibility(org.ebookdroid.core.ViewState)
     */
    @Override
    protected ViewState calculatePageVisibility(final ViewState initial) {
        final int viewIndex = model.getCurrentViewPageIndex();
        int firstVisiblePage = viewIndex;
        int lastVisiblePage = viewIndex;

        final Page[] pages = model.getPages();

        while (firstVisiblePage > 0) {
            final int index = firstVisiblePage - 1;
            if (!ctrl.isPageVisibleImpl(pages[index], initial)) {
                break;
            }
            firstVisiblePage = index;
        }
        while (lastVisiblePage < pages.length - 1) {
            final int index = lastVisiblePage + 1;
            if (!ctrl.isPageVisibleImpl(pages[index], initial)) {
                break;
            }
            lastVisiblePage = index;
        }

        return new ViewState(initial, firstVisiblePage, lastVisiblePage);
    }
}
