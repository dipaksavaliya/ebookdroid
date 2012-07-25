package org.ebookdroid.fontpack.ui;


import org.ebookdroid.fontpack.FontpackApp;
import org.ebookdroid.fontpack.R;
import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionController;

import android.os.Bundle;
import android.view.Menu;

public class FontViewerActivity extends AbstractActionActivity<FontViewerActivity, ActionController<FontViewerActivity>> {

    FontViewer viewer1;
    FontViewer viewer2;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fontviewer);

        viewer1 = new FontViewer(this, R.id.fontProviderSpinner1, R.id.fontPackSpinner1, R.id.fontFamilySpinner1, R.id.regularText1, R.id.italicText1, R.id.boldText1, R.id.boldItalicText1);
        viewer2 = new FontViewer(this, R.id.fontProviderSpinner2, R.id.fontPackSpinner2, R.id.fontFamilySpinner2, R.id.regularText2, R.id.italicText2, R.id.boldText2, R.id.boldItalicText2);

        viewer1.setFontProvider(FontpackApp.afm);
        viewer2.setFontProvider(FontpackApp.esfm);
    }

    @Override
    protected ActionController<FontViewerActivity> createController() {
        return new ActionController<FontViewerActivity>(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
