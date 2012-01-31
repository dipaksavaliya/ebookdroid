package org.ebookdroid.core;

public abstract class CmdZoom extends AbstractCommand {

    public final float oldZoom;
    public final float newZoom;
    public final boolean committed;

    protected CmdZoom(final AbstractViewController ctrl, final float oldZoom, final float newZoom,
            final boolean committed) {
        super(ctrl);
        this.oldZoom = oldZoom;
        this.newZoom = newZoom;
        this.committed = committed;
    }

    protected CmdZoom(final AbstractViewController ctrl) {
        super(ctrl);
        this.oldZoom = 0;
        this.newZoom = ctrl.getBase().getZoomModel().getZoom();
        this.committed = true;
    }

    @Override
    public ViewState execute() {
        return execute(new ViewState(ctrl, newZoom));
    }

    @Override
    protected ViewState calculatePageVisibility(ViewState initial) {
        int viewIndex = model.getCurrentViewPageIndex();
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
