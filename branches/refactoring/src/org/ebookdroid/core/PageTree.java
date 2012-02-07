package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.Bitmaps;

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

    public PageTree(final Page owner) {
        this.owner = owner;
        this.nodes = new PageTreeNode[PageTreeLevel.NODES];
        this.nodes[0] = new PageTreeNode(owner);
    }

    public boolean createChildren(final PageTreeNode parent) {
        int childId = getFirstChildId(parent.id);
        for (int i = 0; i < splitMasks.length; i++, childId++) {
            if (nodes[childId] == null) {
                nodes[childId] = new PageTreeNode(owner, parent, childId, splitMasks[i]);
            }
        }
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
        for (int index = 1; index < nodes.length; index++) {
            if (nodes[index] != null) {
                res |= nodes[index].recycle(bitmapsToRecycle);
                nodes[index] = null;
            }
        }
        return res;
    }

    public boolean recycleParents(final PageTreeNode child, final List<Bitmaps> bitmapsToRecycle) {
        if (child.id == 0) {
            return false;
        }
        int childId = child.id;
        for (PageTreeNode p = getParent(childId, false); p != null; p = getParent(childId, false)) {
            p.recycle(bitmapsToRecycle);
            childId = p.id;
            if (child.id == 0) {
                return true;
            }
        }
        return true;
    }

    public boolean recycleChildren(final PageTreeNode node, final List<Bitmaps> bitmapsToRecycle) {
        boolean res = false;
        int childId = getFirstChildId(node.id);
        for (final int end = Math.min(nodes.length, childId + splitMasks.length); childId < end; childId++) {
            if (nodes[childId] != null) {
                res |= nodes[childId].recycle(bitmapsToRecycle);
                nodes[childId] = null;
            }
        }
        return res;
    }

    public void recycleNodes(final PageTreeLevel level, final List<Bitmaps> bitmapsToRecycle) {
        for (int i = level.start; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].recycle(bitmapsToRecycle);
                nodes[i] = null;
            }
        }
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

    public boolean isHiddenByChildren(final PageTreeNode parent, final ViewState viewState, final RectF pageBounds) {
        int childId = getFirstChildId(parent.id);
        for (final int end = Math.min(nodes.length, childId + splitMasks.length); childId < end; childId++) {
            final PageTreeNode child = nodes[childId];
            if (child == null) {
                return false;
            }
            if (viewState.isNodeKeptInMemory(child, pageBounds) && !child.holder.hasBitmaps()) {
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

    public int getNodesCount() {
        int count = 0;
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                count++;
            }
        }
        return count;
    }
}
