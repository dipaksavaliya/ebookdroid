package org.ebookdroid.core;

import android.graphics.RectF;

public class CmdZoomOut extends CmdZoom {

    public CmdZoomOut(final AbstractViewController ctrl, final float oldZoom, final float newZoom,
            final boolean committed) {
        super(ctrl, oldZoom, newZoom, committed);
    }

    @Override
    public boolean execute(final ViewState viewState, final PageTree nodes) {
        if (newLevel.next != null) {
            nodes.recycleNodes(newLevel.next, bitmapsToRecycle);
        }
        return execute(viewState, nodes, newLevel);
    }

    @Override
    public boolean execute(final ViewState viewState, final PageTreeNode node) {

        final RectF pageBounds = viewState.getBounds(node.page);

        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycle(bitmapsToRecycle);
            return false;
        }

        if (viewState.isNodeVisible(node, pageBounds) && (!node.holder.hasBitmaps() || committed)) {
            node.decodePageTreeNode(nodesToDecode, viewState);
        }

        return true;
    }
}
