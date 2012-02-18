package org.emdev.ui.fullscreen;

import android.view.Window;
import android.view.WindowManager;

import java.util.concurrent.atomic.AtomicBoolean;

class FullScreenManagerOld implements IFullScreenManager {

    private static final int FLAG_FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;

    protected final AtomicBoolean fullScreen = new AtomicBoolean();

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.IFullScreenManager#setFullScreenMode(android.view.Window, boolean)
     */
    @Override
    public void setFullScreenMode(final Window w, final boolean fullScreen) {
        if (this.fullScreen.compareAndSet(!fullScreen, fullScreen)) {
            if (fullScreen) {
                w.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
            } else {
                w.clearFlags(FLAG_FULLSCREEN);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onMenuOpened(android.view.Window)
     */
    @Override
    public void onMenuOpened(final Window w) {
        setFullScreenMode(w, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onMenuClosed(android.view.Window)
     */
    @Override
    public void onMenuClosed(final Window w) {
        setFullScreenMode(w, true);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onPause()
     */
    @Override
    public void onPause(final Window w) {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onResume()
     */
    @Override
    public void onResume(final Window w) {
    }
}
