package org.ebookdroid.core;

import android.graphics.RectF;

public class CmdZoomOut extends CmdZoom {

    public CmdZoomOut(AbstractViewController ctrl, float oldZoom, float newZoom, boolean committed) {
        super(ctrl, oldZoom, newZoom, committed);
    }

    @Override
    public boolean execute(ViewState viewState, PageTreeNode node) {

        final RectF pageBounds = viewState.getBounds(node.page);

        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycleWithChildren(bitmapsToRecycle);
            return false;
        }

        final boolean childrenRequired = node.page.nodes.isChildrenRequired(viewState, node);
        final boolean hasChildren = node.page.nodes.hasChildren(node);

        if (!childrenRequired) {
            if (hasChildren) {
                node.page.nodes.recycleChildren(node, bitmapsToRecycle);
            }
            if (viewState.isNodeVisible(node, pageBounds) && (!node.holder.hasBitmaps() || committed)) {
                node.decodePageTreeNode(nodesToDecode, viewState);
            }
        }
        return true;
    }
}
