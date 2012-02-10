package org.emdev.ui.hwa;


import android.view.View;

import org.emdev.utils.android.AndroidVersion;

public interface IHardwareAcceleration {

    public void setMode(View view, boolean enabled, boolean accelerated);

    public class Factory {

        private static final IHardwareAcceleration instance = AndroidVersion.lessThan3x ? new OldHardwareAcceleration()
                : new NewHardwareAcceleration();

        public static IHardwareAcceleration getInstance() {
            return instance;
        }
    }
}
