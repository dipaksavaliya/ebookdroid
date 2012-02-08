package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.ui.viewer.IViewController.InvalidateSizeReason;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class EventReset extends AbstractEvent {

    protected final PageTreeLevel level;
    protected final InvalidateSizeReason reason;
    protected final boolean clearPages;

    public EventReset(final AbstractViewController ctrl, final InvalidateSizeReason reason, final boolean clearPages) {
        super(ctrl);
        this.level = PageTreeLevel.getLevel(viewState.zoom);
        this.reason = reason;
        this.clearPages = clearPages;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.AbstractEvent#process()
     */
    @Override
    public ViewState process() {
        if (clearPages) {
            final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
            for (final Page page : model.getPages()) {
                page.nodes.recycleAll(bitmapsToRecycle, true);
            }
            BitmapManager.release(bitmapsToRecycle);
        }
        if (reason != null) {
            ctrl.invalidatePageSizes(InvalidateSizeReason.LAYOUT, null);
            ctrl.invalidateScroll();
            viewState = new ViewState(ctrl);
        }

        return super.process();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.PageTree)
     */
    @Override
    public boolean process(final PageTree nodes) {
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
