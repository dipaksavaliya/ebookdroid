package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.ui.viewer.IView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEvent implements IEvent {

    public final LogContext LCTX = LogContext.ROOT.lctx(getClass().getSimpleName());

    public final AbstractViewController ctrl;
    public final DocumentModel model;
    public final IView view;

    protected final List<PageTreeNode> nodesToDecode = new ArrayList<PageTreeNode>();
    protected final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();

    protected ViewState viewState;
    
    protected AbstractEvent(final AbstractViewController ctrl) {
        this.ctrl = ctrl;
        this.model = ctrl.getBase().getDocumentModel();
        this.view = ctrl.getView();
        this.viewState = new ViewState(ctrl);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.IEvent#process()
     */
    @Override
    public ViewState process() {
        viewState = calculatePageVisibility(viewState);

        ctrl.firstVisiblePage = viewState.firstVisible;
        ctrl.lastVisiblePage = viewState.lastVisible;

        for (final Page page : model.getPages()) {
            process(page);
        }

        BitmapManager.release(bitmapsToRecycle);

        if (!nodesToDecode.isEmpty()) {
            ctrl.base.getDecodingProgressModel().increase(nodesToDecode.size());
            decodePageTreeNodes(viewState, nodesToDecode);
            if (LCTX.isDebugEnabled()) {
                LCTX.d(viewState + " => " + nodesToDecode.size());
            }
        }

        return viewState;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.Page)
     */
    @Override
    public final boolean process(final Page page) {
        if (page.recycled) {
            return false;
        }
        if (viewState.isPageKeptInMemory(page) || viewState.isPageVisible(page)) {
            return process(page.nodes);
        }

        recyclePage(viewState, page);
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.PageTree,
     *      org.ebookdroid.core.PageTreeLevel)
     */
    @Override
    public boolean process(final PageTree nodes, final PageTreeLevel level) {
        boolean res = false;
        for (int nodeIndex = level.start; nodeIndex < level.end; nodeIndex++) {
            if (nodes.nodes[nodeIndex] == null) {
                nodes.createChildren(nodes.getParent(nodeIndex, true));
            }
            res |= process(nodes.nodes[nodeIndex]);
        }
        return res;
    }

    protected ViewState calculatePageVisibility(final ViewState initial) {
        int firstVisiblePage = -1;
        int lastVisiblePage = -1;
        for (final Page page : model.getPages()) {
            if (ctrl.isPageVisibleImpl(page, initial)) {
                if (firstVisiblePage == -1) {
                    firstVisiblePage = page.index.viewIndex;
                }
                lastVisiblePage = page.index.viewIndex;
            } else if (firstVisiblePage != -1) {
                break;
            }
        }
        return new ViewState(initial, firstVisiblePage, lastVisiblePage);
    }

    protected final void decodePageTreeNodes(final ViewState viewState, final List<PageTreeNode> nodesToDecode) {
        final PageTreeNode best = Collections.min(nodesToDecode, new PageTreeNodeComparator(viewState));
        final DecodeService ds = ctrl.getBase().getDecodeService();

        if (ds != null) {
            ds.decodePage(viewState, best);

            for (final PageTreeNode node : nodesToDecode) {
                if (node != best) {
                    ds.decodePage(viewState, node);
                }
            }
        }
    }

    protected final void recyclePage(final ViewState viewState, final Page page) {
        final int oldSize = bitmapsToRecycle.size();
        page.nodes.recycleAll(bitmapsToRecycle, true);
        if (LCTX.isDebugEnabled()) {
            final int nodesCount = page.nodes.getNodesCount();
            if (nodesCount > 1) {
                LCTX.d("Recycle page " + page.index + " " + viewState.firstCached + ":" + viewState.lastCached + " = "
                        + (bitmapsToRecycle.size() - oldSize) + " " + nodesCount);
            }
        }

    }
}
