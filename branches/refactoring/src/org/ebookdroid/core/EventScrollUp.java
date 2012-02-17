package org.ebookdroid.core;

public class EventScrollUp extends AbstractEventScroll {

    public EventScrollUp(final AbstractViewController ctrl) {
        super(ctrl);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.AbstractEvent#calculatePageVisibility(org.ebookdroid.core.ViewState)
     */
    @Override
    protected ViewState calculatePageVisibility(final ViewState initial) {
        int firstVisiblePage = initial.pages.firstVisible;
        int lastVisiblePage = initial.pages.lastVisible;

        if (lastVisiblePage != -1) {
            final Page[] pages = model.getPages();
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
