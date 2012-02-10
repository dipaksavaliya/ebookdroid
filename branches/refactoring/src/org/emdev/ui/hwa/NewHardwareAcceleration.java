package org.emdev.ui.hwa;

import android.view.View;

class NewHardwareAcceleration implements IHardwareAcceleration {

    @Override
    public void setMode(final View view, final boolean enabled, final boolean accelerated) {
        if (enabled) {
            view.setLayerType(accelerated ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);
        }
    }
}
