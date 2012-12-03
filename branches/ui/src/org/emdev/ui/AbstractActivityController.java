package org.emdev.ui;

import android.app.Activity;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.emdev.common.log.LogContext;
import org.emdev.common.log.LogManager;
import org.emdev.ui.actions.AbstractComponentController;

public class AbstractActivityController<A extends Activity> extends AbstractComponentController<A> implements ActivityEvents {

    private static final AtomicLong SEQ = new AtomicLong();

    public final LogContext LCTX;

    protected int eventMask = CONTROLLER_EVENTS;

    protected Map<String, Object> fragments = new HashMap<String, Object>();

    protected AbstractActivityController(final A activity) {
        super(activity);
        LCTX = LogManager.root().lctx(this.getClass().getSimpleName(), true).lctx("" + SEQ.getAndIncrement());
    }

    public A getActivity() {
        return getManagedComponent();
    }

    public void beforeCreate(final A activity) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforeCreate(): " + activity);
        }
    }

    public void onRecreate(final A activity) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onRecreate(): " + activity);
        }
        setManagedComponent(activity);
    }

    public void afterCreate() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("afterCreate()");
        }
    }

    public void onRestart() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onRestart()");
        }
    }

    public void onPostCreate(Bundle savedInstanceState) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onPostCreate(): " + savedInstanceState);
        }
    }

    public void onStart() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStart()");
        }
    }

    public void onResume() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onResume()");
        }
    }

    public void onPostResume() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onPostResume()");
        }
    }

    public void onPause(final boolean finishing) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onPause(): " + finishing);
        }
    }

    public void onStop(final boolean finishing) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStop(): " + finishing);
        }
    }

    public void onDestroy(final boolean finishing) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onDestroy(): " + finishing);
        }
    }

}
