package org.ebookdroid.core;

import org.ebookdroid.common.bitmaps.Bitmaps;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.core.models.ZoomModel;

import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.LinkedList;
import java.util.List;

public class PageTree {

    private static final LogContext LCTX = Page.LCTX;

    static RectF[] splitMasks = {
            // Left Top
            new RectF(0, 0, 0.5f, 0.5f),
            // Right top
            new RectF(0.5f, 0, 1.0f, 0.5f),
            // Left Bottom
            new RectF(0, 0.5f, 0.5f, 1.0f),
            // Right Bottom
            new RectF(0.5f, 0.5f, 1.0f, 1.0f), };

    static final int ZOOM_THRESHOLD = 2;

    public static int LEVELS = 1;
    public static int NODES = 1;
    public static float[] levels;

    final Page owner;

    final PageTreeNode[] nodes;

    static {
        LEVELS = 1 + (int) Math.ceil(Math.log(ZoomModel.MAX_ZOOM) / Math.log(2));
        levels = new float[LEVELS];
        levels[0] = 1.0f;
        for (int i = 1; i < LEVELS; i++) {
            levels[i] = levels[i - 1] * 2;
            NODES = NODES + (int) Math.pow(splitMasks.length, i);
        }
    }

    public PageTree(final Page owner) {
        this.owner = owner;
        this.nodes = new PageTreeNode[NODES];
        this.nodes[0] = new PageTreeNode(owner, ZOOM_THRESHOLD);
    }

    public boolean recycleAll(final List<Bitmaps> bitmapsToRecycle, final boolean includeRoot) {
        boolean res = false;
        final int oldCount = bitmapsToRecycle.size();
        for (int index = 0; index < nodes.length; index++) {
            if (nodes[index] != null) {
                if (includeRoot || index != 0) {
                    res |= nodes[index].recycle(bitmapsToRecycle);
                }
                if (index != 0) {
                    nodes[index] = null;
                }
            }
        }
        final int newCount = bitmapsToRecycle.size();
        if (LCTX.isDebugEnabled()) {
            if (newCount != oldCount) {
                LCTX.d("Recycle children for: " + owner.index + " : " + (newCount - oldCount));
            }
        }
        return res;
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

    public boolean recycleChildren(final PageTreeNode parent, final List<Bitmaps> bitmapsToRecycle) {
        if (parent.id == 0) {
            return recycleAll(bitmapsToRecycle, false);
        } else {
            return recycleChildrenImpl(parent, bitmapsToRecycle);
        }
    }

    private boolean recycleChildrenImpl(final PageTreeNode parent, final List<Bitmaps> bitmapsToRecycle) {
        final int oldCount = bitmapsToRecycle.size();

        final LinkedList<PageTreeNode> nodesToRemove = new LinkedList<PageTreeNode>();

        int childId = getFirstChildId(parent.id);
        for (final int end = Math.min(nodes.length, childId + splitMasks.length); childId < end; childId++) {
            if (nodes[childId] != null) {
                nodesToRemove.add(nodes[childId]);
                nodes[childId] = null;
            }
        }

        while (!nodesToRemove.isEmpty()) {
            PageTreeNode child = nodesToRemove.removeFirst();
            child.recycle(bitmapsToRecycle);

            childId = getFirstChildId(child.id);
            for (final int end = Math.min(nodes.length, childId + splitMasks.length); childId < end; childId++) {
                child = nodes[childId];
                if (child != null) {
                    nodesToRemove.add(child);
                    nodes[childId] = null;
                }
            }
        }

        final int newCount = bitmapsToRecycle.size();
        if (LCTX.isDebugEnabled()) {
            if (newCount != oldCount) {
                LCTX.d("Recycle children for: " + parent.fullId + " : " + (newCount - oldCount));
            }
        }

        return true;
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
        final int childLevel = node.level + 1;
        if (childLevel >= PageTree.LEVELS) {
            return false;
        }
        return viewState.zoom >= PageTree.levels[childLevel];
    }

    public boolean hasChildren(final PageTreeNode parent) {
        final int childId = getFirstChildId(parent.id);
        return childId < nodes.length && null != nodes[childId];
    }

    static int getFirstChildId(final long parentId) {
        return (int) (parentId * splitMasks.length + 1);
    }
}
