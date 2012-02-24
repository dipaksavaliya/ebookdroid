package org.ebookdroid.core;

import org.emdev.utils.LengthUtils;

public class EventScrollUp extends AbstractEventScroll {

    public EventScrollUp(final AbstractViewController ctrl) {
        super(ctrl);
    }

    EventScrollUp reuse(final AbstractViewController ctrl) {
        super.reuseImpl(ctrl);
        return this;
    }

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
        final Page[] pages = model.getPages();

        int firstVisiblePage = initial.pages.firstVisible;
        int lastVisiblePage = initial.pages.lastVisible;

        if (LengthUtils.isNotEmpty(pages) && lastVisiblePage != -1) {
            for (int i = lastVisiblePage; i >= 0; i--) {
                if (!ctrl.isPageVisible(pages[i], initial)) {
                    continue;
                } else {
                    lastVisiblePage = i;
                    break;
                }
            }
            firstVisiblePage = lastVisiblePage;
            while (firstVisiblePage > 0) {
                final int index = firstVisiblePage - 1;
                if (!ctrl.isPageVisible(pages[index], initial)) {
                    break;
                }
                firstVisiblePage = index;
            }

            return new ViewState(initial, firstVisiblePage, lastVisiblePage);
        }

        return super.calculatePageVisibility(initial);
    }

}
