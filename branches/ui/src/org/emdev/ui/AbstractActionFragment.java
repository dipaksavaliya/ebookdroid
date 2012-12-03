package org.emdev.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.atomic.AtomicLong;

import org.emdev.common.log.LogContext;
import org.emdev.common.log.LogManager;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMenuHelper;

public abstract class AbstractActionFragment<F extends Fragment, C extends AbstractFragmentController<F>> extends
        Fragment {

    private static final AtomicLong SEQ = new AtomicLong();

    public final LogContext LCTX;

    protected String tag;

    protected C controller;

    protected AbstractActionFragment() {
        LCTX = LogManager.root().lctx(this.getClass().getSimpleName(), true).lctx("" + SEQ.getAndIncrement());
        setRetainInstance(true);
    }

    protected AbstractActionFragment(final C controller) {
        this();
        this.controller = controller;
    }

    private void restoreController(final Activity activity) {
        if (this.controller != null) {
            this.controller.onRestore(activity, (F) this);
        } else {
            this.controller = createController();
            this.controller.beforeAttach(activity);
            ((AbstractActionActivity<?, ?>)activity).controller.fragments.put(tag, this);
        }
    }


    public final C getController() {
        if (controller == null) {
            controller = createController();
        }
        return controller;
    }

    protected abstract C createController();

    @Override
    public final void onAttach(final Activity activity) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onAttach(): " + activity);
        }
        restoreController(activity);
        super.onAttach(activity);
        onAttachImpl(activity);
        getController().afterAttach(activity);
    }

    protected void onAttachImpl(final Activity activity) {
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onCreate()");
        }
        getController().beforeCreate();
        super.onCreate(savedInstanceState);
        onCreateImpl(savedInstanceState);
        getController().afterCreate();
    }

    protected void onCreateImpl(final Bundle savedInstanceState) {
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onCreateView()");
        }
        getController().beforeCreateView();
        final View view = onCreateViewImpl(inflater, container, savedInstanceState);
        getController().afterCreateView();
        return view;
    }

    protected View onCreateViewImpl(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        return null;
    }

    @Override
    public final void onActivityCreated(final Bundle savedInstanceState) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onActivityCreated()");
        }
        super.onActivityCreated(savedInstanceState);
        onActivityCreatedImpl(savedInstanceState);
        getController().onActivityCreated();
    }

    protected void onActivityCreatedImpl(final Bundle savedInstanceState) {
    }

    @Override
    public final void onStart() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStart()");
        }
        super.onStart();
        onStartImpl();
        getController().onStart();
    }

    protected void onStartImpl() {
    }

    @Override
    public final void onResume() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onResume()");
        }
        getController().beforeResume();
        super.onResume();
        onResumeImpl();
        getController().afterResume();
    }

    protected void onResumeImpl() {
    }

    @Override
    public final void onPause() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onPause()");
        }
        getController().beforePause();
        super.onPause();
        onPauseImpl();
        getController().afterPause();
    }

    protected void onPauseImpl() {
    }

    @Override
    public final void onStop() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStop()");
        }
        super.onStop();
        onStopImpl();
        getController().onStop();
    }

    protected void onStopImpl() {
    }

    @Override
    public final void onDestroyView() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onDestroyView()");
        }
        getController().beforeDestroyView();
        super.onDestroyView();
        onDestroyViewImpl();
        getController().afterDestroyView();
    }

    protected void onDestroyViewImpl() {
    }

    @Override
    public final void onDestroy() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onDestroy()");
        }
        super.onDestroy();
        onDestroyImpl();
        getController().onDestroy(getActivity().isFinishing());
    }

    protected void onDestroyImpl() {
    }

    @Override
    public void onDetach() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onDetach()");
        }
        super.onDetach();
        onDetachImpl();
        getController().onDetach();
    }

    protected void onDetachImpl() {
    }

    @Override
    public final void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu != null) {
            getController().updateMenuItems(menu);
        }
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        if (onMenuItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final boolean onContextItemSelected(final MenuItem item) {
        if (onMenuItemSelected(item)) {
            return true;
        }
        return super.onContextItemSelected(item);
    }

    protected final boolean onMenuItemSelected(final MenuItem item) {
        final int actionId = item.getItemId();
        final ActionEx action = getController().getOrCreateAction(actionId);
        if (action.getMethod().isValid()) {
            ActionMenuHelper.setActionParameters(item, action);
            action.run();
            return true;
        }
        return false;
    }

    public final void onButtonClick(final View view) {
        final int actionId = view.getId();
        final ActionEx action = getController().getOrCreateAction(actionId);
        action.onClick(view);
    }

    public final void setActionForView(final int id) {
        final View view = getActivity().findViewById(id);
        final ActionEx action = getController().getOrCreateAction(id);
        if (view != null && action != null) {
            view.setOnClickListener(action);
        }
    }
}
