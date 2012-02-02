package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.Bitmaps;

import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.List;

public class PageTree {

    // private static final LogContext LCTX = Page.LCTX;

    static RectF[] splitMasks = {
            // Left Top
            new RectF(0, 0, 0.5f, 0.5f),
            // Right top
            new RectF(0.5f, 0, 1.0f, 0.5f),
            // Left Bottom
            new RectF(0, 0.5f, 0.5f, 1.0f),
            // Right Bottom
            new RectF(0.5f, 0.5f, 1.0f, 1.0f), };

    final Page owner;

    final PageTreeNode[] nodes;

    int lastCreatedNode;

    public PageTree(final Page owner) {
        this.owner = owner;
        this.nodes = new PageTreeNode[PageTreeLevel.NODES];
        this.nodes[0] = new PageTreeNode(owner);
        this.lastCreatedNode = 0;
    }

    public boolean createChildren(final PageTreeNode parent) {
        int childId = getFirstChildId(parent.id);
        for (int i = 0; i < splitMasks.length; i++, childId++) {
            if (nodes[childId] == null) {
                nodes[childId] = new PageTreeNode(owner, parent, childId, splitMasks[i]);
            }
        }
        lastCreatedNode = Math.max(lastCreatedNode, childId - 1);
        return true;
    }

    public PageTreeNode getParent(final int nodeIndex, final boolean create) {
        if (nodeIndex == 0) {
            return null;
        }
        final int parentIndex = (nodeIndex - 1) / 4;
        if (nodes[parentIndex] == null && create) {
            createChildren(getParent(parentIndex, true));
        }
        return nodes[parentIndex];
    }

    public boolean recycleAll(final List<Bitmaps> bitmapsToRecycle, final boolean includeRoot) {
        boolean res = false;
        if (includeRoot) {
            res |= nodes[0].recycle(bitmapsToRecycle);
        }
        for (int index = 1; index < lastCreatedNode; index++) {
            if (nodes[index] != null) {
                res |= nodes[index].recycle(bitmapsToRecycle);
                nodes[index] = null;
            }
        }
        lastCreatedNode = 0;
        return res;
    }

    public void recycleParents(final PageTreeNode child) {
        if (child.id == 0) {
            return;
        }
        int childId = child.id;
        for (PageTreeNode p = getParent(childId, false); p != null; p = getParent(childId, false)) {
            if ((childId - 1) % 4 == 0) {
                p.stopDecodingThisNode("Children should be shown");
            }
            childId = p.id;
            if (child.id == 0) {
                return;
            }
        }
    }

    public void recycleNodes(PageTreeLevel level, final List<Bitmaps> bitmapsToRecycle) {
        for (int i = level.start; i < lastCreatedNode; i++) {
            if (nodes[i] != null) {
                nodes[i].recycle(bitmapsToRecycle);
                nodes[i] = null;
            }
        }
        lastCreatedNode = Math.min(lastCreatedNode, level.start - 1);
    }

    public boolean allChildrenHasBitmap(final ViewState viewState, final PageTreeNode parent, final PagePaint paint) {
        int childId = getFirstChildId(parent.id);
        for (final int end = Math.min(nodes.length, childId + splitMasks.length); childId < end; childId++) {
            final PageTreeNode child = nodes[childId];
            if (child == null || !child.holder.hasBitmaps()) {
                return false;
            }
        }
        return true;
    }

    public void drawChildren(final Canvas canvas, final ViewState viewState, final RectF pageBounds,
            final PageTreeNode parent, final PagePaint paint) {
        int childId = getFirstChildId(parent.id);
        for (final int end = Math.min(nodes.length, childId + splitMasks.length); childId < end; childId++) {
            final PageTreeNode child = nodes[childId];
            if (child != null) {
                child.draw(canvas, viewState, pageBounds, paint);
            }
        }
    }

    public boolean isHiddenByChildren(final PageTreeNode parent, final ViewState viewState, final RectF pageBounds) {
        int childId = getFirstChildId(parent.id);
        for (final int end = Math.min(nodes.length, childId + splitMasks.length); childId < end; childId++) {
            final PageTreeNode child = nodes[childId];
            if (child == null) {
                return false;
            }
            if (!child.holder.hasBitmaps() && !child.decodingNow.get()) {
                return false;
            }
        }
        return true;
    }

    public boolean isChildrenRequired(final ViewState viewState, final PageTreeNode node) {
        final PageTreeLevel next = node.level;
        return next.next != null && next.next.zoom < viewState.zoom;
    }

    public boolean hasChildren(final PageTreeNode parent) {
        final int childId = getFirstChildId(parent.id);
        return childId < nodes.length && null != nodes[childId];
    }

    static int getFirstChildId(final long parentId) {
        return (int) (parentId * splitMasks.length + 1);
    }
}
