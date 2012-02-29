package org.emdev.ui.fullscreen;

import android.view.Window;

public interface IFullScreenManager {

    IFullScreenManager instance = /* AndroidVersion.lessThan3x ? */ new FullScreenManagerOld() /* : new FullScreenManagerNew() */;

    void setFullScreenMode(Window w, boolean fullScreen);

    void onMenuOpened(Window w);

    void onMenuClosed(Window w);

    void onPause(Window w);

    void onResume(Window w);

}
