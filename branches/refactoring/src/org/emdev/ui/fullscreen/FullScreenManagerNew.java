package org.emdev.ui.fullscreen;

import android.view.Window;

import org.emdev.utils.SystemUtils;

class FullScreenManagerNew extends FullScreenManagerOld implements IFullScreenManager {

    private boolean sysState;

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.FullScreenManagerOld#setFullScreenMode(android.view.Window, boolean)
     */
    @Override
    public boolean setFullScreenMode(final Window w, final boolean fullScreen) {
        sysState = SystemUtils.setSystemUIVisible(!fullScreen);
        return super.setFullScreenMode(w, fullScreen);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.FullScreenManagerOld#onPause()
     */
    @Override
    public boolean onPause() {
        if (sysState) {
            SystemUtils.setSystemUIVisible(true);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.FullScreenManagerOld#onResume()
     */
    @Override
    public boolean onResume() {
        if (sysState) {
            SystemUtils.setSystemUIVisible(false);
        }
        return true;
    }
}
