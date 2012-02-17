package org.ebookdroid.core;

public class EventScrollDown extends AbstractEventScroll {

    public EventScrollDown(final AbstractViewController ctrl) {
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

        if (firstVisiblePage != -1) {
            final Page[] pages = model.getPages();
            for (int i = firstVisiblePage; i < pages.length; i++) {
                if (!ctrl.isPageVisible(pages[i], initial)) {
                    continue;
                } else {
                    firstVisiblePage = i;
                    break;
                }
            }
            lastVisiblePage = firstVisiblePage;
            while (lastVisiblePage < pages.length - 1) {
                final int index = lastVisiblePage + 1;
                if (!ctrl.isPageVisible(pages[index], initial)) {
                    break;
                }
                lastVisiblePage = index;
            }
            return new ViewState(initial, firstVisiblePage, lastVisiblePage);
        }

        return super.calculatePageVisibility(initial);
    }

}
