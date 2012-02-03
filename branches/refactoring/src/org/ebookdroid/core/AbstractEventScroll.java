package org.ebookdroid.core;

import android.graphics.RectF;

public abstract class AbstractEventScroll extends AbstractEvent {

    protected PageTreeLevel level;

    protected AbstractEventScroll(final AbstractViewController ctrl) {
        super(ctrl);
        level = PageTreeLevel.getLevel(viewState.zoom);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.PageTree)
     */
    @Override
    public boolean process(final PageTree nodes) {
        if (level.next != null) {
            nodes.recycleNodes(level.next, bitmapsToRecycle);
        }
        return process(nodes, level);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.PageTreeNode)
     */
    @Override
    public boolean process(final PageTreeNode node) {

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
