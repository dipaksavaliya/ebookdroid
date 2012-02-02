package org.ebookdroid.core;

import android.graphics.RectF;

public abstract class CmdScroll extends AbstractCommand {

    protected CmdScroll(final AbstractViewController ctrl) {
        super(ctrl);
    }

    @Override
    public boolean execute(final ViewState viewState, final PageTreeNode node) {

        final RectF pageBounds = viewState.getBounds(node.page);

        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycleWithChildren(bitmapsToRecycle);
            return false;
        }

        final boolean childrenRequired = node.page.nodes.isChildrenRequired(viewState, node);
        final boolean hasChildren = node.page.nodes.hasChildren(node);

        if (hasChildren) {
            execute(viewState, node.page.nodes, node);
            return true;
        }

        if (childrenRequired) {
            if (node.id != 0) {
                node.stopDecodingThisNode("children created");
            }
            node.page.nodes.createChildren(node);
            execute(viewState, node.page.nodes, node);
            return true;
        }

        if (!node.holder.hasBitmaps()) {
            node.decodePageTreeNode(nodesToDecode, viewState);
        }
        return true;
    }

}
