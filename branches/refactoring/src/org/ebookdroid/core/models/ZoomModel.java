package org.ebookdroid.core.models;

import org.ebookdroid.core.events.ZoomListener;

import org.emdev.utils.listeners.ListenerProxy;

public class ZoomModel extends ListenerProxy {

    public static final float MIN_ZOOM = 1.0f;
    public static final float MAX_ZOOM = 16.0f;

    private static final float INCREMENT_DELTA = 0.05f;

    private float initialZoom = MIN_ZOOM;
    private float currentZoom = MIN_ZOOM;

    private boolean isCommited;

    public ZoomModel() {
        super(ZoomListener.class);
    }

    public void initZoom(final float zoom) {
        this.initialZoom = this.currentZoom = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, zoom));
        isCommited = true;
    }

    public void setZoom(float zoom) {
        final float newZoom = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, zoom));
        final float oldZoom = this.currentZoom;
        if (newZoom != oldZoom) {
            isCommited = false;
            this.currentZoom = newZoom;
            this.<ZoomListener> getListener().zoomChanged(oldZoom, newZoom, false);
        }
    }

    public float getZoom() {
        return currentZoom;
    }

    public void increaseZoom() {
        setZoom(currentZoom + INCREMENT_DELTA);
    }

    public void decreaseZoom() {
        setZoom(currentZoom - INCREMENT_DELTA);
    }

    public void commit() {
        if (!isCommited) {
            isCommited = true;
            this.<ZoomListener> getListener().zoomChanged(initialZoom, currentZoom, true);
            initialZoom = currentZoom;
        }
    }
}
