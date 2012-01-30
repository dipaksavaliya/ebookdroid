package org.ebookdroid.ui.settings.fragments;

import org.ebookdroid.R;

public class BookFragment extends BasePreferenceFragment {

    public BookFragment() {
        super(R.xml.fragment_book);
    }

    @Override
    public void decorate() {
        decorator.decorateBooksSettings();
    }
}
