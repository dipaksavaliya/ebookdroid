package org.ebookdroid.fontpack.ui;

import org.ebookdroid.fontpack.FontpackApp;
import org.ebookdroid.fontpack.R;
import org.emdev.common.fonts.IFontProvider;
import org.emdev.common.fonts.data.FontPack;
import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionController;
import org.emdev.ui.actions.ActionDialogBuilder;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.tasks.BaseAsyncTask;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CheckBox;

public class MainActivity extends AbstractActionActivity<MainActivity, ActionController<MainActivity>> {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FontpackApp.EBOOKDROID_VERSION == 0) {
            showErrorDlg(R.string.msg_no_ebookdroid);
            return;
        }

        if (FontpackApp.EBOOKDROID_VERSION < 1499) {
            showErrorDlg(R.string.msg_old_ebookdroid);
            return;
        }

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            System.exit(0);
        }
    }

    @Override
    protected ActionController<MainActivity> createController() {
        return new ActionController<MainActivity>(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @ActionMethod(ids = R.id.install)
    public void install(final ActionEx action) {
        new FontInstaller().execute(FontpackApp.afm);
    }

    @ActionMethod(ids=R.id.menu_viewfonts)
    public void viewFonts(final ActionEx action) {
        startActivity(new Intent(this, FontViewerActivity.class));
    }

    @ActionMethod(ids = R.id.menu_close)
    public void close(final ActionEx action) {
        finish();
    }

    public void showErrorDlg(final int msgId, final Object... args) {
        final ActionDialogBuilder builder = new ActionDialogBuilder(this, getController());

        builder.setTitle(R.string.app_name);
        builder.setMessage(msgId, args);

        builder.setPositiveButton(R.string.menu_close, R.id.menu_close);
        builder.show();
    }

    public class FontInstaller extends BaseAsyncTask<IFontProvider, Boolean> {

        public FontInstaller() {
            super(MainActivity.this, R.string.msg_installing, false);
        }

        @Override
        protected Boolean doInBackground(IFontProvider... params) {
            boolean res = true;
            for (IFontProvider ifp : params) {
                for (FontPack fp : ifp) {
                    publishProgress(getString(R.string.msg_installing_pack, fp.name));
                    res &= FontpackApp.esfm.install(fp);
                }
            }
            return res;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            CheckBox removeView = (CheckBox) findViewById(R.id.remove);
            if (result && removeView.isChecked()) {
                FontpackApp.uninstall();
                finish();
            }
        }
    }
}
