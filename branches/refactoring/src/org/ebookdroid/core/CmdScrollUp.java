package org.ebookdroid.core;

public class CmdScrollUp extends CmdScroll {

    public CmdScrollUp(final AbstractViewController ctrl) {
        super(ctrl);
    }

    @Override
    protected ViewState calculatePageVisibility(final ViewState initial) {
        int firstVisiblePage = initial.firstVisible;
        int lastVisiblePage = initial.lastVisible;

        if (lastVisiblePage != -1) {
            final Page[] pages = model.getPages();
            for (int i = lastVisiblePage; i >= 0; i--) {
                if (!ctrl.isPageVisibleImpl(pages[i], initial)) {
                    continue;
                } else {
                    lastVisiblePage = i;
                    break;
                }
            }
            firstVisiblePage = lastVisiblePage;
            while (firstVisiblePage > 0) {
                final int index = firstVisiblePage - 1;
                if (!ctrl.isPageVisibleImpl(pages[index], initial)) {
                    break;
                }
                firstVisiblePage = index;
            }

            return new ViewState(initial, firstVisiblePage, lastVisiblePage);
        }

        return super.calculatePageVisibility(initial);
    }

}
