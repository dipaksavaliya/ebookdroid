package org.ebookdroid.ui.settings.fragments;

import org.ebookdroid.R;

public class UIFragment extends BasePreferenceFragment {

    public UIFragment() {
        super(R.xml.fragment_ui);
    }

    @Override
    public void decorate() {
        decorator.decorateUISettings();
    }
}
