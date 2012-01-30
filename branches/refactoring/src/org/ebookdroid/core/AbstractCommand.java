package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.ui.viewer.IView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCommand {

    public final LogContext LCTX = LogContext.ROOT.lctx(getClass().getSimpleName());

    public final AbstractViewController ctrl;
    public final DocumentModel model;
    public final IView view;

    protected final List<PageTreeNode> nodesToDecode = new ArrayList<PageTreeNode>();
    protected final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();

    public AbstractCommand(final AbstractViewController ctrl) {
        this.ctrl = ctrl;
        this.model = ctrl.getBase().getDocumentModel();
        this.view = ctrl.getView();
    }

    public ViewState execute() {
        return execute(new ViewState(ctrl));
    }

    public ViewState execute(final ViewState initial) {
        final ViewState viewState = calculatePageVisibility(initial);
        ctrl.firstVisiblePage = viewState.firstVisible;
        ctrl.lastVisiblePage = viewState.lastVisible;

        for (final Page page : model.getPages()) {
            execute(viewState, page);
        }

        BitmapManager.release(bitmapsToRecycle);

        if (!nodesToDecode.isEmpty()) {
            decodePageTreeNodes(viewState, nodesToDecode);
            if (LCTX.isDebugEnabled()) {
                LCTX.d(viewState + " => " + nodesToDecode.size());
            }
        }

        return viewState;
    }

    public boolean execute(final ViewState viewState, final Page page) {
        if (page.recycled) {
            return false;
        }
        if (viewState.isPageKeptInMemory(page) || viewState.isPageVisible(page)) {
            return execute(viewState, page.nodes.root);
        }

        final int oldSize = bitmapsToRecycle.size();
        if (page.nodes.recycleAll(bitmapsToRecycle, true)) {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Recycle page " + page.index + " " + viewState.firstCached + ":" + viewState.lastCached + " = "
                        + (bitmapsToRecycle.size() - oldSize));
            }
        }

        return false;
    }

    public void execute(final ViewState viewState, final PageTree nodes, final PageTreeNode parent) {
        int childId = nodes.getFirstChildId(parent.id);
        for (final int end = childId + PageTree.splitMasks.length; childId < end; childId++) {
            final PageTreeNode child = nodes.nodes.get(childId);
            if (child != null) {
                execute(viewState, child);
            }
        }
    }

    public boolean execute(final ViewState viewState, final PageTreeNode node) {
        return false;
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
            ds.decodePage(viewState, best, best.croppedBounds != null ? best.croppedBounds : best.pageSliceBounds);

            for (final PageTreeNode node : nodesToDecode) {
                if (node != best) {
                    ds.decodePage(viewState, node, node.croppedBounds != null ? node.croppedBounds
                            : node.pageSliceBounds);
                }
            }
        }
    }

}
