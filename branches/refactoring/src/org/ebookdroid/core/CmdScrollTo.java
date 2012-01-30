package org.ebookdroid.core;

public class CmdScrollTo extends CmdScroll {

    public final int viewIndex;

    public CmdScrollTo(AbstractViewController ctrl, int viewIndex) {
        super(ctrl);
        this.viewIndex = viewIndex;
    }

    @Override
    protected ViewState calculatePageVisibility(ViewState initial) {
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
