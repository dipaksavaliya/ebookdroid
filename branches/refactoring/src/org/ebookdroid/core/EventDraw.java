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

    public final static LogContext LCTX = LogContext.ROOT.lctx("EventDraw", false);

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

        if (!page.nodes.nodes[0].holder.hasBitmaps()) {
            drawPageBackground(page);
        }

        process(page.nodes);
        return true;
    }

    @Override
    public boolean process(final PageTree nodes) {
        return process(nodes, level);
    }

    @Override
    public boolean process(final PageTree nodes, final PageTreeLevel level) {
        return process(nodes.nodes[0]);
    }

    @Override
    public boolean process(final PageTreeNode node) {
        final RectF tr = node.getTargetRect(viewState, viewState.viewRect, pageBounds);

        if (LCTX.isDebugEnabled()) {
            LCTX.d("process(" + node.fullId + "): view=" + viewState.viewRect + ", page=" + pageBounds + ", tr=" + tr);
        }

        if (!viewState.isNodeVisible(node, pageBounds)) {
            return false;
        }

        if (!node.page.nodes.allChildrenHasBitmap(viewState, node, paint)) {
            node.holder.drawBitmap(canvas, paint, viewBase, tr);

            drawBrightnessFilter(canvas, tr);
            return true;
        }

        return drawChildren(viewState, node);
    }

    protected boolean drawChildren(final ViewState viewState, final PageTreeNode parent) {
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
