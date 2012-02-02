package org.ebookdroid.core;

import android.graphics.RectF;

public class CmdZoomIn extends CmdZoom {

    public CmdZoomIn(final AbstractViewController ctrl, final float oldZoom, final float newZoom,
            final boolean committed) {
        super(ctrl, oldZoom, newZoom, committed);
    }

    public CmdZoomIn(final AbstractViewController ctrl) {
        super(ctrl);
    }

    @Override
    public boolean execute(final ViewState viewState, final PageTree nodes) {
        return execute(viewState, nodes, newLevel);
    }

    @Override
    public boolean execute(final ViewState viewState, final PageTreeNode node) {

        final RectF pageBounds = viewState.getBounds(node.page);

        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycle(bitmapsToRecycle);
            return false;
        }

        if (oldLevel != newLevel) {
            node.page.nodes.recycleParents(node);
        }

        if (node.isReDecodingRequired(committed, viewState)) {
            node.stopDecodingThisNode("Zoom changed");
            node.decodePageTreeNode(nodesToDecode, viewState);
        } else if (!node.holder.hasBitmaps()) {
            node.decodePageTreeNode(nodesToDecode, viewState);
        }
        return true;
    }

}
