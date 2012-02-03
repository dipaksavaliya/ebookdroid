package org.ebookdroid.core;

import android.graphics.RectF;

public abstract class CmdScroll extends AbstractCommand {

    protected PageTreeLevel level;

    protected CmdScroll(final AbstractViewController ctrl) {
        super(ctrl);
    }

    @Override
    public ViewState execute() {
        ViewState initial = new ViewState(ctrl);
        level = PageTreeLevel.getLevel(initial.zoom);
        return execute(initial);
    }

    public boolean execute(final ViewState viewState, final PageTree nodes) {
        if (level.next != null) {
            nodes.recycleNodes(level.next, bitmapsToRecycle);
        }
        return execute(viewState, nodes, level);
    }

    @Override
    public boolean execute(final ViewState viewState, final PageTreeNode node) {

        final RectF pageBounds = viewState.getBounds(node.page);

        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycle(bitmapsToRecycle);
            return false;
        }

        if (!node.holder.hasBitmaps()) {
            node.decodePageTreeNode(nodesToDecode, viewState);
        }

        return true;
    }
}
