package org.ebookdroid.fontpack.ui;

import org.ebookdroid.fontpack.FontpackApp;
import org.ebookdroid.fontpack.R;
import org.emdev.common.fonts.IFontProvider;
import org.emdev.common.fonts.data.FontPack;
import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionController;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.tasks.BaseAsyncTask;

import android.os.Bundle;
import android.view.Menu;
import android.widget.CheckBox;

public class MainActivity extends AbstractActionActivity<MainActivity, ActionController<MainActivity>> {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
