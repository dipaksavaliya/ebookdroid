package org.ebookdroid.ui.settings.fragments;

import org.ebookdroid.R;

public class ScrollFragment extends BasePreferenceFragment {

    public ScrollFragment() {
        super(R.xml.fragment_scroll);
    }

    @Override
    public void decorate() {
        decorator.decorateScrollSettings();
    }

}
