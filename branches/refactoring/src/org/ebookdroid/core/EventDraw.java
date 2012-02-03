package org.ebookdroid.core;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.R;
import org.ebookdroid.common.settings.SettingsManager;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;

public class EventDraw implements IEvent {

    public final ViewState viewState;
    public final PageTreeLevel level;
    public final Canvas canvas;

    final PagePaint paint;

    RectF nodesBounds;
    RectF pageBounds;

    public EventDraw(ViewState viewState, Canvas canvas) {
        this.viewState = viewState;
        this.level = PageTreeLevel.getLevel(viewState.zoom);
        this.canvas = canvas;
        this.paint = viewState.nightMode ? PagePaint.NIGHT : PagePaint.DAY;
    }

    public EventDraw(EventDraw event, Canvas canvas) {
        this.viewState = event.viewState;
        this.level = event.level;
        this.canvas = canvas;
        this.paint = event.paint;
    }

    @Override
    public ViewState process() {
        viewState.ctrl.drawView(this);
        return viewState;
    }

    @Override
    public boolean process(Page page) {
        nodesBounds = viewState.getBounds(page);
        pageBounds = viewState.view.getAdjustedPageBounds(viewState, nodesBounds);

        if (!page.nodes.nodes[0].holder.hasBitmaps()) {
            canvas.drawRect(pageBounds, paint.fillPaint);

            final TextPaint textPaint = paint.textPaint;
            textPaint.setTextSize(24 * viewState.zoom);
            canvas.drawText(EBookDroidApp.context.getString(R.string.text_page) + " " + (page.index.viewIndex + 1),
                    pageBounds.centerX(), pageBounds.centerY(), textPaint);
        }

        process(page.nodes);
        return true;
    }

    @Override
    public boolean process(PageTree nodes) {
        return process(nodes, level);
    }

    @Override
    public boolean process(PageTree nodes, PageTreeLevel level) {
        return process(nodes.nodes[0]);
    }

    @Override
    public boolean process(PageTreeNode node) {
        final RectF tr = node.getTargetRect(viewState, viewState.viewRect, pageBounds);
        if (!viewState.isNodeVisible(node, pageBounds)) {
            return false;
        }

        if (!node.page.nodes.allChildrenHasBitmap(viewState, node, paint)) {
            node.holder.drawBitmap(viewState, canvas, paint, tr);

            drawBrightnessFilter(canvas, tr);
            return true;
        }

        return drawChildren(viewState, pageBounds, node);
    }

    protected boolean drawChildren(final ViewState viewState, final RectF pageBounds, final PageTreeNode parent) {
        boolean res = false;
        final PageTree nodes = parent.page.nodes;

        int childId = PageTree.getFirstChildId(parent.id);
        for (final int end = Math.min(nodes.nodes.length, childId + PageTree.splitMasks.length); childId < end; childId++) {
            final PageTreeNode child = nodes.nodes[childId];
            if (child != null) {
                res |= process(child);
            }
        }
        return res;
    }

    protected void drawBrightnessFilter(final Canvas canvas, final RectF tr) {
        final int brightness = SettingsManager.getAppSettings().getBrightness();
        if (brightness < 100) {
            final Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setAlpha(255 - brightness * 255 / 100);
            canvas.drawRect(tr, p);
        }
    }

}
