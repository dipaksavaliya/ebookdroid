package org.ebookdroid.core.models;

import org.ebookdroid.core.events.ZoomListener;

import org.emdev.utils.listeners.ListenerProxy;

public class ZoomModel extends ListenerProxy {

    private static final float INCREMENT_DELTA = 0.05f;

    private float initialZoom = 1.0f;
    private float currentZoom = 1.0f;

    private boolean isCommited;

    public ZoomModel() {
        super(ZoomListener.class);
    }

    public void initZoom(final float zoom) {
        this.initialZoom = this.currentZoom = Math.max(zoom, 1.0f);
        isCommited = true;
    }

    public void setZoom(float zoom) {
        final float newZoom = Math.max(zoom, 1.0f);
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
        setZoom(getZoom() + INCREMENT_DELTA);
    }

    public void decreaseZoom() {
        setZoom(getZoom() - INCREMENT_DELTA);
    }

    public void commit() {
        if (!isCommited) {
            isCommited = true;
            this.<ZoomListener> getListener().zoomChanged(initialZoom, currentZoom, true);
            initialZoom = currentZoom;
        }
    }
}
