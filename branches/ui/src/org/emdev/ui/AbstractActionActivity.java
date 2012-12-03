package org.emdev.ui;

import org.ebookdroid.R;
import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.ui.about.AboutActivity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.atomic.AtomicLong;

import org.emdev.common.android.AndroidVersion;
import org.emdev.common.log.LogContext;
import org.emdev.common.log.LogManager;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMenuHelper;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.uimanager.IUIManager;

public abstract class AbstractActionActivity<A extends Activity, C extends AbstractActivityController<A>> extends
        Activity implements ActivityEvents {

    private static final AtomicLong SEQ = new AtomicLong();

    public final LogContext LCTX;

    protected C controller;

    protected int eventMask = ACTIVITY_EVENTS;

    protected AbstractActionActivity() {
        LCTX = LogManager.root().lctx(this.getClass().getSimpleName(), true).lctx("" + SEQ.getAndIncrement());
    }

    @Override
    public final Object onRetainNonConfigurationInstance() {
        return getController();
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private final C restoreController() {
        final Object last = this.getLastNonConfigurationInstance();
        if (last instanceof AbstractActivityController) {
            this.controller = (C) last;
            if ((this.controller.eventMask & ON_RECREATE) == ON_RECREATE) {
                this.controller.onRecreate((A) this);
            }
            return controller;
        } else {
            this.controller = createController();
            if ((this.controller.eventMask & BEFORE_CREATE) == BEFORE_CREATE) {
                this.controller.beforeCreate((A) this);
            }
        }
        return this.controller;
    }

    public final C getController() {
        if (controller == null) {
            controller = createController();
        }
        return controller;
    }

    protected abstract C createController();

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onCreate(): " + savedInstanceState);
        }
        final C c = restoreController();
        super.onCreate(savedInstanceState);
        if ((eventMask & ON_CREATE) == ON_CREATE) {
            onCreateImpl(savedInstanceState);
        }
        if ((c.eventMask & AFTER_CREATE) == AFTER_CREATE) {
            c.afterCreate();
        }
    }

    protected void onCreateImpl(final Bundle savedInstanceState) {
    }

    @Override
    protected final void onRestart() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onRestart()");
        }
        super.onRestart();
        if ((eventMask & ON_RESTART) == ON_RESTART) {
            onRestartImpl();
        }
        if ((controller.eventMask & ON_RESTART) == ON_RESTART) {
            controller.onRestart();
        }
    }

    protected void onRestartImpl() {
    }

    @Override
    protected final void onStart() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStart()");
        }
        super.onStart();
        if ((eventMask & ON_START) == ON_START) {
            onStartImpl();
        }
        if ((controller.eventMask & ON_START) == ON_START) {
            controller.onStart();
        }
    }

    protected void onStartImpl() {
    }

    @Override
    protected final void onPostCreate(final Bundle savedInstanceState) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onCreate(): " + savedInstanceState);
        }
        super.onPostCreate(savedInstanceState);
        if ((eventMask & ON_POST_CREATE) == ON_POST_CREATE) {
            onPostCreateImpl(savedInstanceState);
        }
        if ((controller.eventMask & ON_POST_CREATE) == ON_POST_CREATE) {
            controller.onPostCreate(savedInstanceState);
        }
    }

    protected void onPostCreateImpl(final Bundle savedInstanceState) {
    }

    @Override
    protected final void onResume() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onResume()");
        }
        super.onResume();
        if ((eventMask & ON_RESUME) == ON_RESUME) {
            onResumeImpl();
        }
        if ((controller.eventMask & ON_RESUME) == ON_RESUME) {
            controller.onResume();
        }
    }

    protected void onResumeImpl() {
    }

    @Override
    protected final void onPostResume() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onPostResume()");
        }
        super.onPostResume();
        if ((eventMask & ON_POST_RESUME) == ON_POST_RESUME) {
            onPostResumeImpl();
        }
        if ((controller.eventMask & ON_POST_RESUME) == ON_POST_RESUME) {
            controller.onPostResume();
        }
    }

    protected void onPostResumeImpl() {
    }

    @Override
    protected final void onPause() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onPause()");
        }
        super.onPause();
        if ((eventMask & ON_PAUSE) == ON_PAUSE) {
            onPauseImpl(isFinishing());
        }
        if ((controller.eventMask & ON_PAUSE) == ON_PAUSE) {
            controller.onPause(isFinishing());
        }
    }

    protected void onPauseImpl(final boolean finishing) {
    }

    @Override
    protected final void onStop() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onStop()");
        }
        super.onStop();
        if ((eventMask & ON_STOP) == ON_STOP) {
            onStopImpl(isFinishing());
        }
        if ((controller.eventMask & ON_STOP) == ON_STOP) {
            controller.onStop(isFinishing());
        }
    }

    protected void onStopImpl(final boolean finishing) {
    }

    @Override
    protected final void onDestroy() {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onDestroy()");
        }
        super.onDestroy();
        if ((eventMask & ON_DESTROY) == ON_DESTROY) {
            onDestroyImpl(isFinishing());
        }
        if ((controller.eventMask & ON_DESTROY) == ON_DESTROY) {
            controller.onDestroy(isFinishing());
        }
    }

    protected void onDestroyImpl(final boolean finishing) {
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu != null) {
            updateMenuItems(menu);
        }
        return true;
    }

    protected void updateMenuItems(final Menu menu) {
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        if (onMenuItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        if (onMenuItemSelected(item)) {
            return true;
        }
        return super.onContextItemSelected(item);
    }

    protected boolean onMenuItemSelected(final MenuItem item) {
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (data != null) {
            final int actionId = data.getIntExtra(ActionMenuHelper.ACTIVITY_RESULT_ACTION_ID, 0);
            if (actionId != 0) {
                final ActionEx action = getController().getOrCreateAction(actionId);
                action.putValue(ActionMenuHelper.ACTIVITY_RESULT_CODE, Integer.valueOf(resultCode));
                action.putValue(ActionMenuHelper.ACTIVITY_RESULT_DATA, data);
                action.run();
            }
        }
    }

    public final void setActionForView(final int id) {
        final View view = findViewById(id);
        final ActionEx action = getController().getOrCreateAction(id);
        if (view != null && action != null) {
            view.setOnClickListener(action);
        }
    }

    @ActionMethod(ids = R.id.mainmenu_about)
    public void showAbout(final ActionEx action) {
        final Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    protected boolean hasNormalMenu() {
        return AndroidVersion.lessThan4x || IUIManager.instance.isTabletUi(this) || AppSettings.current().showTitle;
    }

    @SuppressWarnings("unchecked")
    protected <F extends Fragment, FC extends AbstractFragmentController<F>, FF extends AbstractActionFragment<F, FC>> FF showFragment(final int containerId,
            final Class<FF> clazz, final String tag, final Bundle args) {
        final FragmentManager fm = getFragmentManager();
        FF f = (FF) fm.findFragmentByTag(tag);
        try {
            if (f == null) {
                LCTX.d("Create new fragment for: " + tag);
                f = clazz.newInstance();
                f.setArguments(args);
                f.controller = (FC) controller.fragments.get(tag);

                final FragmentTransaction t1 = fm.beginTransaction();
                t1.add(containerId, f, tag);
                t1.commit();
            } else {
                LCTX.d("Use old fragment for: " + tag);
            }
            final FragmentTransaction t = fm.beginTransaction();
            t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            t.addToBackStack(tag);
            t.replace(containerId, f);
            t.commit();
        } catch (final InstantiationException ex) {
            ex.printStackTrace();
        } catch (final IllegalAccessException ex) {
            ex.printStackTrace();
        }

        return f;
    }
}
