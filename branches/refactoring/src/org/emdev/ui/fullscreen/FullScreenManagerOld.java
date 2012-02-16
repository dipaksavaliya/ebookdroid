package org.emdev.ui.fullscreen;

import android.view.Window;
import android.view.WindowManager;

class FullScreenManagerOld implements IFullScreenManager {

    private static final int FLAG_FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;

    protected boolean state;

    /**
     * {@inheritDoc}
     *
     * @see org.emdev.ui.fullscreen.IFullScreenManager#setFullScreenMode(android.view.Window, boolean)
     */
    @Override
    public boolean setFullScreenMode(final Window w, final boolean fullScreen) {
        if (fullScreen) {
            w.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        } else {
            w.clearFlags(FLAG_FULLSCREEN);
        }
        state = fullScreen;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onMenuOpened(android.view.Window)
     */
    @Override
    public boolean onMenuOpened(final Window w) {
        if (state) {
            return setFullScreenMode(w, false);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onMenuClosed(android.view.Window)
     */
    @Override
    public boolean onMenuClosed(final Window w) {
        if (state) {
            return setFullScreenMode(w, true);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onPause()
     */
    @Override
    public boolean onPause() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onResume()
     */
    @Override
    public boolean onResume() {
        return false;
    }
}
