package org.ebookdroid.fontpack.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ebookdroid.fontpack.FontpackApp;
import org.ebookdroid.fontpack.R;
import org.emdev.common.fonts.AbstractFontProvider;
import org.emdev.common.fonts.IFontProvider;
import org.emdev.common.fonts.data.FontFamily;
import org.emdev.common.fonts.data.FontPack;
import org.emdev.common.fonts.data.FontStyle;
import org.emdev.common.fonts.typeface.TypefaceEx;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

class FontViewer implements AdapterView.OnItemSelectedListener {
    final Context context;

    IFontProvider selectedProvider;
    FontPack selectedPack;
    FontFamily selectedFamily;

    final Spinner fontProvider;
    final Spinner fontPack;
    final Spinner fontFamily;

    final TextView regularText;
    final TextView italicText;
    final TextView boldText;
    final TextView boldItalicText;

    ArrayAdapter<AbstractFontProvider> providers;

    FontViewer(Activity activity, int fprsId, int fpsId, int ffsId, int rtId, int itId, int btId, int bitId) {
        this.context = activity;

        fontProvider = (Spinner) activity.findViewById(fprsId);
        fontPack = (Spinner) activity.findViewById(fpsId);
        fontFamily = (Spinner) activity.findViewById(ffsId);

        regularText = (TextView) activity.findViewById(rtId);
        italicText = (TextView) activity.findViewById(itId);
        boldText = (TextView) activity.findViewById(btId);
        boldItalicText = (TextView) activity.findViewById(bitId);

        regularText.setTag(FontStyle.REGULAR);
        italicText.setTag(FontStyle.ITALIC);
        boldText.setTag(FontStyle.BOLD);
        boldItalicText.setTag(FontStyle.BOLD_ITALIC);

        providers = createAdapter(FontpackApp.sfm, FontpackApp.afm, FontpackApp.esfm);

        fontProvider.setAdapter(providers);
        fontProvider.setOnItemSelectedListener(this);
        fontPack.setOnItemSelectedListener(this);
        fontFamily.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == fontProvider) {
            selectedProvider = (IFontProvider) parent.getAdapter().getItem(position);
            selectedPack = null;
            selectedFamily = null;
            fontPack.setAdapter(createAdapter(selectedProvider));
        } else if (parent == fontPack) {
            selectedPack = (FontPack) parent.getAdapter().getItem(position);
            selectedFamily = null;
            fontFamily.setAdapter(createAdapter(selectedPack));
        } else if (parent == fontFamily) {
            selectedFamily = (FontFamily) parent.getAdapter().getItem(position);
        }
        updateControls();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (parent == fontProvider) {
            selectedProvider = null;
            selectedPack = null;
            selectedFamily = null;
        } else if (parent == fontPack) {
            selectedPack = null;
            selectedFamily = null;
        } else if (parent == fontFamily) {
            selectedFamily = null;
        }
        updateControls();
    }

    public void setFontProvider(AbstractFontProvider provider) {
        fontProvider.setSelection(providers.getPosition(provider));
    }

    protected void updateTextView(final TextView view) {
        TypefaceEx tf = null;
        FontStyle style = (FontStyle) view.getTag();
        if (selectedPack != null && selectedFamily != null) {
            tf = selectedPack.getTypeface(selectedFamily.type, style);
        }
        if (tf == null) {
            view.setVisibility(View.INVISIBLE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setTypeface(tf.typeface);

        String text = "";

        switch (selectedFamily.type) {
            case SANS:
            case SERIF:
            case MONO:
                switch (style) {
                    case REGULAR:
                        text = "Regular";
                        break;
                    case ITALIC:
                        text = "Italic";
                        break;
                    case BOLD:
                        text = "Bold";
                        break;
                    case BOLD_ITALIC:
                        text = "Bold italic";
                        break;
                }
                break;
            case SYMBOL:
                if (style == FontStyle.REGULAR) {
                    for(int ch = 0x201; ch <= 0x20F; ch++) {
                        text = text + (char)ch;
                    }
                }
                break;
            case DINGBAT:
                if (style == FontStyle.REGULAR) {
                    for(int ch = 0x2701; ch <= 0x270F; ch++) {
                        text = text + (char)ch;
                    }
                }
                break;
        }

        view.setText(text);
    }

    protected void updateControls() {
        updateTextView(regularText);
        updateTextView(italicText);
        updateTextView(boldText);
        updateTextView(boldItalicText);
    }

    protected <T> ArrayAdapter<T> createAdapter(final T... items) {
        return createAdapter(Arrays.asList(items));
    }

    protected <T> ArrayAdapter<T> createAdapter(final Iterable<T> parent) {
        final List<T> items = new ArrayList<T>();
        for (final T item : parent) {
            items.add(item);
        }
        return createAdapter(items);
    }

    protected <T> ArrayAdapter<T> createAdapter(final List<T> items) {
        final ArrayAdapter<T> adapter = new ArrayAdapter<T>(context, R.layout.list_item, R.id.list_item, items);
        adapter.setDropDownViewResource(R.layout.list_dropdown_item);
        return adapter;
    }

}