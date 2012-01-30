package org.ebookdroid.core;

public class CmdScrollDown extends CmdScroll {

    public CmdScrollDown(final AbstractViewController ctrl) {
        super(ctrl);
    }

    @Override
    protected ViewState calculatePageVisibility(final ViewState initial) {

        int firstVisiblePage = initial.firstVisible;
        int lastVisiblePage = initial.lastVisible;

        if (firstVisiblePage != -1) {
            final Page[] pages = model.getPages();
            for (int i = firstVisiblePage; i < pages.length; i++) {
                if (!ctrl.isPageVisibleImpl(pages[i], initial)) {
                    continue;
                } else {
                    firstVisiblePage = i;
                    break;
                }
            }
            lastVisiblePage = firstVisiblePage;
            while (lastVisiblePage < pages.length - 1) {
                final int index = lastVisiblePage + 1;
                if (!ctrl.isPageVisibleImpl(pages[index], initial)) {
                    break;
                }
                lastVisiblePage = index;
            }
            return new ViewState(initial, firstVisiblePage, lastVisiblePage);
        }

        return super.calculatePageVisibility(initial);
    }

}
