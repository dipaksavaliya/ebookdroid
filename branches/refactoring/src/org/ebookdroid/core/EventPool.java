package org.ebookdroid.core;

import org.ebookdroid.ui.viewer.IViewController.InvalidateSizeReason;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EventPool {

    private static final ConcurrentLinkedQueue<EventDraw> drawEvents = new ConcurrentLinkedQueue<EventDraw>();
    private static final ConcurrentLinkedQueue<EventReset> resetEvents = new ConcurrentLinkedQueue<EventReset>();

    private static final ConcurrentLinkedQueue<EventScrollUp> scrollUpEvents = new ConcurrentLinkedQueue<EventScrollUp>();
    private static final ConcurrentLinkedQueue<EventScrollDown> scrollDownEvents = new ConcurrentLinkedQueue<EventScrollDown>();
    private static final ConcurrentLinkedQueue<EventScrollTo> scrollToEvents = new ConcurrentLinkedQueue<EventScrollTo>();
    private static final ConcurrentLinkedQueue<EventChildLoaded> childLoadedEvents = new ConcurrentLinkedQueue<EventChildLoaded>();

    private static final ConcurrentLinkedQueue<EventZoomIn> zoomInEvents = new ConcurrentLinkedQueue<EventZoomIn>();
    private static final ConcurrentLinkedQueue<EventZoomOut> zoomOutEvents = new ConcurrentLinkedQueue<EventZoomOut>();

    public static EventDraw newEventDraw(final ViewState viewState, final Canvas canvas) {
        final EventDraw event = drawEvents.poll();
        return event != null ? event.reuse(viewState, canvas) : new EventDraw(viewState, canvas);
    }

    public static EventDraw newEventDraw(final EventDraw parentEvent, final Canvas canvas) {
        final EventDraw event = drawEvents.poll();
        return event != null ? event.reuse(parentEvent, canvas) : new EventDraw(parentEvent, canvas);
    }

    public static EventReset newEventReset(final AbstractViewController ctrl, final InvalidateSizeReason reason,
            final boolean clearPages) {
        final EventReset event = resetEvents.poll();
        return event != null ? event.reuse(ctrl, reason, clearPages) : new EventReset(ctrl, reason, clearPages);
    }

    public static AbstractEventScroll newEventScroll(final AbstractViewController ctrl, int delta) {
        if (delta > 0) {
            final EventScrollDown event = scrollDownEvents.poll();
            return event != null ? event.reuse(ctrl) : new EventScrollDown(ctrl);
        } else {
            final EventScrollUp event = scrollUpEvents.poll();
            return event != null ? event.reuse(ctrl) : new EventScrollUp(ctrl);
        }
    }

    public static EventScrollTo newEventScrollTo(final AbstractViewController ctrl, final int viewIndex) {
        final EventScrollTo event = scrollToEvents.poll();
        return event != null ? event.reuse(ctrl, viewIndex) : new EventScrollTo(ctrl, viewIndex);
    }

    public static EventChildLoaded newEventChildLoaded(final AbstractViewController ctrl, final PageTreeNode child,
            final Rect bitmapBounds) {
        final EventChildLoaded event = childLoadedEvents.poll();
        return event != null ? event.reuse(ctrl, child, bitmapBounds) : new EventChildLoaded(ctrl, child, bitmapBounds);
    }

    public static AbstractEventZoom newEventZoom(final AbstractViewController ctrl, final float oldZoom,
            final float newZoom, final boolean committed) {
        if (newZoom > oldZoom) {
            final EventZoomIn event = zoomInEvents.poll();
            return event != null ? event.reuse(ctrl, oldZoom, newZoom, committed) : new EventZoomIn(ctrl, oldZoom, newZoom, committed);
        } else {
            final EventZoomOut event = zoomOutEvents.poll();
            return event != null ? event.reuse(ctrl, oldZoom, newZoom, committed) : new EventZoomOut(ctrl, oldZoom, newZoom, committed);
        }
    }

    public static void release(final EventDraw event) {
        event.canvas = null;
        event.level = null;
        event.pageBounds = null;
        event.viewBase = null;
        event.viewState = null;
        drawEvents.offer(event);
    }

    public static void release(final EventReset event) {
        event.ctrl = null;
        event.model = null;
        event.viewState = null;
        event.bitmapsToRecycle.clear();
        event.nodesToDecode.clear();
        event.level = null;
        event.reason = null;
        resetEvents.offer(event);
    }

    public static void release(final EventScrollUp event) {
        event.ctrl = null;
        event.model = null;
        event.viewState = null;
        event.bitmapsToRecycle.clear();
        event.nodesToDecode.clear();
        event.level = null;
        scrollUpEvents.offer(event);
    }

    public static void release(final EventScrollDown event) {
        event.ctrl = null;
        event.model = null;
        event.viewState = null;
        event.bitmapsToRecycle.clear();
        event.nodesToDecode.clear();
        event.level = null;
        scrollDownEvents.offer(event);
    }

    public static void release(final EventScrollTo event) {
        event.ctrl = null;
        event.model = null;
        event.viewState = null;
        event.bitmapsToRecycle.clear();
        event.nodesToDecode.clear();
        event.level = null;
        scrollToEvents.offer(event);
    }

    public static void release(final EventChildLoaded event) {
        event.ctrl = null;
        event.model = null;
        event.viewState = null;
        event.bitmapsToRecycle.clear();
        event.nodesToDecode.clear();
        event.level = null;
        event.child = null;
        event.nodes = null;
        event.page = null;
        childLoadedEvents.offer(event);
    }

    public static void release(final EventZoomIn event) {
        event.ctrl = null;
        event.model = null;
        event.viewState = null;
        event.bitmapsToRecycle.clear();
        event.nodesToDecode.clear();
        event.oldLevel = null;
        event.newLevel = null;
        zoomInEvents.offer(event);
    }

    public static void release(final EventZoomOut event) {
        event.ctrl = null;
        event.model = null;
        event.viewState = null;
        event.bitmapsToRecycle.clear();
        event.nodesToDecode.clear();
        event.oldLevel = null;
        event.newLevel = null;
        zoomOutEvents.offer(event);
    }

}
