package org.emdev.ui;

import android.app.Activity;
import android.app.Fragment;
import android.view.Menu;

import java.util.concurrent.atomic.AtomicLong;

import org.emdev.common.log.LogContext;
import org.emdev.common.log.LogManager;
import org.emdev.ui.actions.AbstractComponentController;

public abstract class AbstractFragmentController<F extends Fragment> extends AbstractComponentController<F> {

    private static final AtomicLong SEQ = new AtomicLong();

    public final LogContext LCTX;

    protected AbstractFragmentController(final F managedComponent) {
        super(managedComponent);
        LCTX = LogManager.root().lctx(this.getClass().getSimpleName(), true).lctx("" + SEQ.getAndIncrement());
    }

    public abstract F createFragment();

    public F getFragment() {
        return getManagedComponent();
    }

    public Activity getActivity() {
        return getFragment().getActivity();
    }

    public void onRestore(final Activity activity, final F abstractActionFragment) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforeAttach(): " + activity);
        }
        setManagedComponent(abstractActionFragment);
        if (activity instanceof AbstractActionActivity) {
            final AbstractActionActivity<?, ?> a = (AbstractActionActivity<?, ?>) activity;
            this.m_parent = a.getController();
        }
    }

    public void beforeAttach(final Activity activity) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforeAttach(): " + activity);
        }
        if (activity instanceof AbstractActionActivity) {
            final AbstractActionActivity<?, ?> a = (AbstractActionActivity<?, ?>) activity;
            this.m_parent = a.getController();
        }
    }

    public void afterAttach(final Activity activity) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("afterAttach(): " + activity);
        }
    }

    public void beforeCreate() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforeCreate()");
        }
    }

    public void afterCreate() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("afterCreate()");
        }
    }

    public void beforeCreateView() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforeCreateView()");
        }
    }

    public void afterCreateView() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("afterCreateView()");
        }
    }

    public void onActivityCreated() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onActivityCreated()");
        }
    }

    public void onStart() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStart()");
        }
    }

    public void beforeResume() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforeResume()");
        }
    }

    public void afterResume() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("afterResume()");
        }
    }

    public void beforePause() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforePause()");
        }
    }

    public void afterPause() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("afterPause()");
        }
    }

    public void onStop() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStop()");
        }
    }

    public void beforeDestroyView() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("beforeDestroyView()");
        }
    }

    public void afterDestroyView() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("afterDestroyView()");
        }
    }

    public void onDestroy(final boolean finishing) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onDestroy(): " + finishing);
        }
    }

    public void onDetach() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onDetach()");
        }
    }

    public void updateMenuItems(final Menu menu) {
    }

}
