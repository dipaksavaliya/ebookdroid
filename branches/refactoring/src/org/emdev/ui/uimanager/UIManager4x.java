package org.emdev.ui.uimanager;

import android.app.Activity;
import android.view.View;
import android.view.Window;

public class UIManager4x implements IUIManager {

    private boolean hwaEnabled = false;

    private boolean titleVisible;

    @Override
    public void setTitleVisible(final Activity activity, final boolean visible) {
        this.titleVisible = visible;
        try {
            final Window window = activity.getWindow();
            if (!visible) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
                window.requestFeature(Window.FEATURE_ACTION_BAR);
            } else {
                window.requestFeature(Window.FEATURE_ACTION_BAR);
                window.requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
                activity.setProgressBarIndeterminate(true);
                activity.setProgressBarIndeterminateVisibility(true);
                window.setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, 1);
            }
        } catch (final Throwable th) {
            LCTX.e("Error on requestFeature call: " + th.getMessage());
        }
    }

    @Override
    public void setFullScreenMode(final Activity activity, final boolean fullScreen) {
    }

    @Override
    public void setHardwareAccelerationEnabled(final boolean enabled) {
        this.hwaEnabled = enabled;
    }

    @Override
    public void setHardwareAccelerationMode(final View view, final boolean accelerated) {
        if (this.hwaEnabled && view != null) {
            view.setLayerType(accelerated ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    public void openOptionsMenu(final Activity activity, final View view) {
        if (titleVisible) {
            activity.openOptionsMenu();
        } else {
            view.showContextMenu();
        }
    }

    @Override
    public void onMenuOpened(final Activity activity) {
    }

    @Override
    public void onMenuClosed(final Activity activity) {
    }

    @Override
    public void onPause(final Activity activity) {
    }

    @Override
    public void onResume(final Activity activity) {
    }

    @Override
    public void onDestroy(final Activity activity) {
    }
}
