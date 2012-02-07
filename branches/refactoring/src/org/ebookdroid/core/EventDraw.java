package org.ebookdroid.core;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.R;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.SettingsManager;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;

public class EventDraw implements IEvent {

    public final static LogContext LCTX = LogContext.ROOT.lctx("EventDraw", true);

    public final ViewState viewState;
    public final PageTreeLevel level;
    public final Canvas canvas;

    final PagePaint paint;

    RectF pageBounds;
    PointF viewBase;

    public EventDraw(final ViewState viewState, final Canvas canvas) {
        this.viewState = viewState;
        this.level = PageTreeLevel.getLevel(viewState.zoom);
        this.canvas = canvas;
        this.paint = viewState.nightMode ? PagePaint.NIGHT : PagePaint.DAY;
        this.viewBase = viewState.view.getBase(viewState.viewRect);
    }

    public EventDraw(final EventDraw event, final Canvas canvas) {
        this.viewState = event.viewState;
        this.level = event.level;
        this.canvas = canvas;
        this.paint = event.paint;
        this.viewBase = event.viewBase;
    }

    @Override
    public ViewState process() {
        viewState.ctrl.drawView(this);
        return viewState;
    }

    @Override
    public boolean process(final Page page) {
        pageBounds = viewState.getBounds(page);

        if (LCTX.isDebugEnabled()) {
            LCTX.d("process(" + page.index + "): view=" + viewState.viewRect + ", page=" + pageBounds);
        }

        drawPageBackground(page);

        return process(page.nodes);
    }

    @Override
    public boolean process(final PageTree nodes) {
        return process(nodes, level);
    }

    @Override
    public boolean process(final PageTree nodes, final PageTreeLevel level) {
        boolean res = false;
        for (int nodeIndex = level.start; nodeIndex < level.end; nodeIndex++) {
            final PageTreeNode node = nodes.nodes[nodeIndex];
            if (node != null) {
                res |= process(node);
            }
        }
        return res;
    }

    @Override
    public boolean process(final PageTreeNode node) {
        final RectF nodeRect = node.getTargetRect(viewState, pageBounds);
        if (LCTX.isDebugEnabled()) {
            LCTX.d("process(" + node.fullId + "): view=" + viewState.viewRect + ", page=" + pageBounds + ", node="
                    + nodeRect);
        }

        if (!viewState.isNodeVisible(nodeRect)) {
            return false;
        }

        try {
            if (node.holder.drawBitmap(canvas, paint, viewBase, nodeRect, nodeRect)) {
                return true;
            }

            if (node.parent != null) {
                final RectF parentRect = node.parent.getTargetRect(viewState, pageBounds);
                if (node.parent.holder.drawBitmap(canvas, paint, viewBase, parentRect, nodeRect)) {
                    return true;
                }
            }

            final PageTree nodes = node.page.nodes;

            boolean res = true;
            int childId = PageTree.getFirstChildId(node.id);
            for (final int end = Math.min(nodes.nodes.length, childId + PageTree.splitMasks.length); childId < end; childId++) {
                final PageTreeNode child = nodes.nodes[childId];
                if (child != null) {
                    final RectF childRect = child.getTargetRect(viewState, pageBounds);
                    res &= child.holder.drawBitmap(canvas, paint, viewBase, childRect, nodeRect);
                }
            }

            return res;
        } finally {
            drawBrightnessFilter(canvas, nodeRect);
        }
    }

    protected void drawPageBackground(final Page page) {
        final RectF rect = new RectF(pageBounds);
        rect.offset(-viewBase.x, -viewBase.y);

        canvas.drawRect(rect, paint.fillPaint);

        final TextPaint textPaint = paint.textPaint;
        textPaint.setTextSize(24 * viewState.zoom);

        final String text = EBookDroidApp.context.getString(R.string.text_page) + " " + (page.index.viewIndex + 1);
        canvas.drawText(text, rect.centerX(), rect.centerY(), textPaint);
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
