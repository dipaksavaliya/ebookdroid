package org.ebookdroid.ui;

import org.ebookdroid.R;
import org.ebookdroid.ui.library.RecentsFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;

import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionMethodDef;
import org.emdev.ui.actions.ActionTarget;
import org.emdev.ui.uimanager.IUIManager;

@ActionTarget(
// actions
actions = {
// start
@ActionMethodDef(id = R.id.mainmenu_about, method = "showAbout")
// finish
})
public class EBookDroidActivity extends
        AbstractActionActivity<EBookDroidActivity, EBookDroidActivityController> {

    public static final DisplayMetrics DM = new DisplayMetrics();

    private boolean menuClosedCalled;

    @Override
    public void onCreateImpl(final Bundle savedInstanceState) {
        super.setContentView(R.layout.ebookdroid);

        getWindowManager().getDefaultDisplay().getMetrics(DM);
        LCTX.i("XDPI=" + DM.xdpi + ", YDPI=" + DM.ydpi);

    }

    @Override
    protected void onNewIntent(final Intent intent) {
        if (LCTX.isDebugEnabled()) {
            LCTX.d("onNewIntent(): " + intent);
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onPostCreateImpl(final Bundle savedInstanceState) {
        showFragment(R.id.mainscreen, RecentsFragment.class, "recents", null);
    }

    @Override
    protected void onResumeImpl() {
        IUIManager.instance.invalidateOptionsMenu(this);
    }

    @Override
    protected EBookDroidActivityController createController() {
        return new EBookDroidActivityController(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Activity#onMenuOpened(int, android.view.Menu)
     */
    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu) {
        IUIManager.instance.onMenuOpened(this);
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Activity#onPanelClosed(int, android.view.Menu)
     */
    @Override
    public void onPanelClosed(final int featureId, final Menu menu) {
        menuClosedCalled = false;
        super.onPanelClosed(featureId, menu);
        if (!menuClosedCalled) {
            onOptionsMenuClosed(menu);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Activity#onOptionsMenuClosed(android.view.Menu)
     */
    @Override
    public void onOptionsMenuClosed(final Menu menu) {
        menuClosedCalled = true;
        IUIManager.instance.onMenuClosed(this);
    }
}
