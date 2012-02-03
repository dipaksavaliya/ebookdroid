package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.ui.viewer.IViewController.InvalidateSizeReason;

import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class EventChildLoaded extends EventScrollTo {

    public final Page page;
    public final PageTree nodes;
    public final PageTreeNode child;

    public final Rect bitmapBounds;

    public EventChildLoaded(final AbstractViewController ctrl, final PageTreeNode child, final Rect bitmapBounds) {
        super(ctrl, child.page.index.viewIndex);
        this.page = child.page;
        this.nodes = page.nodes;
        this.child = child;
        this.bitmapBounds = bitmapBounds;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.AbstractEvent#process()
     */
    @Override
    public ViewState process() {
        ctrl.pageUpdated(this.viewIndex);

        final boolean changed = page.setAspectRatio(bitmapBounds.width(), bitmapBounds.height());

        ViewState viewState = new ViewState(ctrl);
        if (changed) {
            ctrl.invalidatePageSizes(InvalidateSizeReason.PAGE_LOADED, page);
            viewState = super.process(viewState);
        }

        final RectF bounds = viewState.getBounds(page);
        final PageTreeNode parent = child.parent;
        if (parent != null && viewState.zoom > 1.5) {
            final boolean hiddenByChildren = nodes.isHiddenByChildren(parent, viewState, bounds);
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Node " + parent.fullId + " is: " + (hiddenByChildren ? "" : "not") + " hidden by children");
            }
            if (!viewState.isNodeVisible(parent, bounds) || hiddenByChildren) {
                final List<Bitmaps> bitmapsToRecycle = new ArrayList<Bitmaps>();
                nodes.recycleParents(child, bitmapsToRecycle);
                BitmapManager.release(bitmapsToRecycle);
                if (LCTX.isDebugEnabled()) {
                    LCTX.d("Recycle parent nodes for: " + child.fullId + " " + bitmapsToRecycle.size());
                }
            }
        }

        if (viewState.isNodeVisible(child, bounds)) {
            ctrl.redrawView(viewState);
        }
        return viewState;
    }

}
