package org.ebookdroid.core;

import org.ebookdroid.common.settings.types.DecodeMode;

import android.graphics.RectF;

public class CmdZoomIn extends CmdZoom {

    public CmdZoomIn(AbstractViewController ctrl, float oldZoom, float newZoom, boolean committed) {
        super(ctrl, oldZoom, newZoom, committed);
    }

    public CmdZoomIn(AbstractViewController ctrl) {
        super(ctrl);
    }

    @Override
    public boolean execute(ViewState viewState, PageTreeNode node) {

        final RectF pageBounds = viewState.getBounds(node.page);

        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycleWithChildren(bitmapsToRecycle);
            return false;
        }

        final boolean childrenRequired = node.isChildrenRequired(viewState);
        final boolean hasChildren = node.page.nodes.hasChildren(node);

        if (childrenRequired) {
            if (!hasChildren) {
                if (node.id != 0 || viewState.decodeMode == DecodeMode.LOW_MEMORY) {
                    node.stopDecodingThisNode("children should be created");
                }
                node.page.nodes.createChildren(node);
            }

            execute(viewState, node.page.nodes, node);

            return true;
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
