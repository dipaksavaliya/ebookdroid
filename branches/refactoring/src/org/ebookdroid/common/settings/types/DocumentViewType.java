package org.ebookdroid.common.settings.types;

import org.ebookdroid.ui.viewer.IActivityController;
import org.ebookdroid.ui.viewer.IView;
import org.ebookdroid.ui.viewer.viewers.BaseView;
import org.ebookdroid.ui.viewer.viewers.SurfaceView;

import java.lang.reflect.Constructor;

public enum DocumentViewType {

    BASE("Base", BaseView.class),

    SURFACE("Surface", SurfaceView.class);

    public static DocumentViewType DEFAULT = SURFACE;

    private static DocumentViewType[] _values = values();

    private final String resValue;

    private final Class<? extends IView> viewClass;

    private DocumentViewType(final String resValue, final Class<? extends IView> viewClass) {
        this.resValue = resValue;
        this.viewClass = viewClass;
    }

    public String getResValue() {
        return resValue;
    }

    public IView create(final IActivityController base) {
        try {
            final Constructor<?> c = viewClass.getConstructor(IActivityController.class);
            return (IView) c.newInstance(base);
        } catch (final Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DocumentViewType getByResValue(final String resValue) {
        for (final DocumentViewType pa : _values) {
            if (pa.resValue.equals(resValue)) {
                return pa;
            }
        }
        return DEFAULT;
    }
}
