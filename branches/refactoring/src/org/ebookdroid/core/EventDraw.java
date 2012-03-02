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

import java.util.Queue;

public class EventDraw implements IEvent {

    public final static LogContext LCTX = LogContext.ROOT.lctx("EventDraw", false);

    private final Queue<EventDraw> eventQueue;

    public ViewState viewState;
    public PageTreeLevel level;
    public Canvas canvas;

    RectF pageBounds;
    PointF viewBase;
    final RectF fixedPageBounds = new RectF();

    EventDraw(final Queue<EventDraw> eventQueue) {
        this.eventQueue = eventQueue;
    }

    void init(final ViewState viewState, final Canvas canvas) {
        this.viewState = viewState;
        this.level = PageTreeLevel.getLevel(viewState.zoom);
        this.canvas = canvas;
        this.viewBase = viewState.ctrl.getView().getBase(viewState.viewRect);
    }

    void init(final EventDraw event, final Canvas canvas) {
        this.viewState = event.viewState;
        this.level = event.level;
        this.canvas = canvas;
        this.viewBase = event.viewBase;
    }

    void release() {
        this.canvas = null;
        this.level = null;
        this.pageBounds = null;
        this.viewBase = null;
        this.viewState = null;
        eventQueue.offer(this);
    }

    @Override
    public ViewState process() {
        try {
            canvas.drawRect(canvas.getClipBounds(), viewState.paint.backgroundFillPaint);
            viewState.ctrl.drawView(this);
            return viewState;
        } finally {
            release();
        }
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
        return nodes.process(this, level);
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
            if (node.holder.drawBitmap(canvas, viewState.paint, viewBase, nodeRect, nodeRect)) {
                return true;
            }

            if (node.parent != null) {
                final RectF parentRect = node.parent.getTargetRect(viewState, pageBounds);
                if (node.parent.holder.drawBitmap(canvas, viewState.paint, viewBase, parentRect, nodeRect)) {
                    return true;
                }
            }

            return node.page.nodes.paintChildren(this, node, nodeRect);

        } finally {
            drawBrightnessFilter(canvas, nodeRect);
        }
    }

    public boolean paintChild(final PageTreeNode node, final PageTreeNode child, final RectF nodeRect) {
        final RectF childRect = child.getTargetRect(viewState, pageBounds);
        return child.holder.drawBitmap(canvas, viewState.paint, viewBase, childRect, nodeRect);
    }

    protected void drawPageBackground(final Page page) {
        fixedPageBounds.set(pageBounds);
        fixedPageBounds.offset(-viewBase.x, -viewBase.y);

        canvas.drawRect(fixedPageBounds, viewState.paint.fillPaint);

        final TextPaint textPaint = viewState.paint.textPaint;
        textPaint.setTextSize(24 * viewState.zoom);

        final String text = EBookDroidApp.context.getString(R.string.text_page) + " " + (page.index.viewIndex + 1);
        canvas.drawText(text, fixedPageBounds.centerX(), fixedPageBounds.centerY(), textPaint);
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
