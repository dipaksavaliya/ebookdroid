package org.ebookdroid.ui.settings.fragments;

import org.ebookdroid.R;

public class RenderFragment extends BasePreferenceFragment {

    public RenderFragment() {
        super(R.xml.fragment_render);
    }

    @Override
    public void decorate() {
        decorator.decorateRenderSettings();
    }
}
