package org.ebookdroid.core;

public class EventScrollTo extends AbstractEventScroll {

    public int viewIndex;

    public EventScrollTo(final AbstractViewController ctrl, final int viewIndex) {
        super(ctrl);
        reuse(null, viewIndex);
    }

    EventScrollTo reuse(final AbstractViewController ctrl, final int viewIndex) {
        if (ctrl != null) {
            reuseImpl(ctrl);
        }
        this.viewIndex = viewIndex;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.AbstractEvent#process()
     */
    @Override
    public ViewState process() {
        try {
            return super.process();
        } finally {
            EventPool.release(this);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.AbstractEvent#calculatePageVisibility(org.ebookdroid.core.ViewState)
     */
    @Override
    protected ViewState calculatePageVisibility(final ViewState initial) {
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
