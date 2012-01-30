package org.emdev.ui;

import org.ebookdroid.R;
import org.ebookdroid.ui.about.AboutActivity;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import org.emdev.ui.actions.ActionController;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;

public abstract class AbstractActionActivity extends Activity {

    protected final ActionController<AbstractActionActivity> actions;

    protected AbstractActionActivity() {
        actions = new ActionController<AbstractActionActivity>(this, this);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        final int actionId = item.getItemId();
        final ActionEx action = actions.getOrCreateAction(actionId);
        if (action.getMethod().isValid()) {
            action.run();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public final void onButtonClick(final View view) {
        final int actionId = view.getId();
        final ActionEx action = actions.getOrCreateAction(actionId);
        action.onClick(view);
    }

    public final void setActionForView(int id) {
        View view = findViewById(id);
        ActionEx action = actions.getOrCreateAction(id);
        if (view != null && action != null) {
            view.setOnClickListener(action);
        }
    }
    
    @ActionMethod(ids = R.id.mainmenu_about)
    public void showAbout(final ActionEx action) {
        final Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }
}
