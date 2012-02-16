package org.emdev.ui.fullscreen;

import android.view.Window;

import org.emdev.utils.android.AndroidVersion;

public interface IFullScreenManager {

    IFullScreenManager instance = AndroidVersion.lessThan3x ? new FullScreenManagerOld() : new FullScreenManagerNew();

    boolean setFullScreenMode(Window w, boolean fullScreen);

    boolean onMenuOpened(Window w);

    boolean onMenuClosed(Window w);

    boolean onPause();

    boolean onResume();

}
