package org.ebookdroid.fontpack.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ebookdroid.fontpack.FontpackApp;
import org.ebookdroid.fontpack.R;
import org.emdev.common.fonts.AssetsFontProvider;
import org.emdev.common.fonts.IFontProvider;
import org.emdev.common.fonts.data.FontFamily;
import org.emdev.common.fonts.data.FontPack;
import org.emdev.common.fonts.data.FontStyle;
import org.emdev.common.fonts.typeface.TypefaceEx;
import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionController;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class FontManagerActivity extends AbstractActionActivity<FontManagerActivity, ActionController<FontManagerActivity>> {

    private IFontProvider selectedProvider;
    private FontPack selectedPack;
    private FontFamily selectedFamily;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fontmanager);

        final Spinner fontProvider = (Spinner) findViewById(R.id.fontProviderSpinner);
        final Spinner fontPack = (Spinner) findViewById(R.id.fontPackSpinner);
        final Spinner fontFamily = (Spinner) findViewById(R.id.fontFamilySpinner);

        final TextView regularText = (TextView) findViewById(R.id.regularText);
        final TextView italicText = (TextView) findViewById(R.id.italicText);
        final TextView boldText = (TextView) findViewById(R.id.boldText);
        final TextView boldItalicText = (TextView) findViewById(R.id.boldItalicText);

        regularText.setTag(FontStyle.REGULAR);
        italicText.setTag(FontStyle.ITALIC);
        boldText.setTag(FontStyle.BOLD);
        boldItalicText.setTag(FontStyle.BOLD_ITALIC);

        fontProvider.setAdapter(createAdapter(FontpackApp.sfm, FontpackApp.afm, FontpackApp.esfm));
        fontProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                selectedProvider = (IFontProvider) parent.getAdapter().getItem(position);
                selectedPack = null;
                selectedFamily = null;

                fontPack.setAdapter(createAdapter(selectedProvider));
                updateControls();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                selectedPack = null;
                selectedFamily = null;
                updateControls();
            }
        });

        fontPack.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                selectedPack = (FontPack) parent.getAdapter().getItem(position);
                selectedFamily = null;

                fontFamily.setAdapter(createAdapter(selectedPack));
                updateControls();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                selectedPack = null;
                selectedFamily = null;
                updateControls();
            }
        });

        fontFamily.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                selectedFamily = (FontFamily) parent.getAdapter().getItem(position);
                updateControls();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                selectedFamily = null;
                updateControls();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
        {
            FontpackApp.uninstall();
            System.exit(0);
        }
    }


    @Override
    protected ActionController<FontManagerActivity> createController() {
        return new ActionController<FontManagerActivity>(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @ActionMethod(ids = R.id.install)
    public void install(final ActionEx action) {
        FontpackApp.esfm.install(selectedPack);
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
                    text = "\u2701\u2702\u2703\u2704\u2705\u2706\u2707\u2708\u2709\u270A\u270B\u270C\u270D\u270E\u270F";
                }
                break;
        }

        view.setText(text);
    }

    protected void updateControls() {

        final TextView regularText = (TextView) findViewById(R.id.regularText);
        final TextView italicText = (TextView) findViewById(R.id.italicText);
        final TextView boldText = (TextView) findViewById(R.id.boldText);
        final TextView boldItalicText = (TextView) findViewById(R.id.boldItalicText);

        updateTextView(regularText);
        updateTextView(italicText);
        updateTextView(boldText);
        updateTextView(boldItalicText);

        this.findViewById(R.id.install).setEnabled(
                selectedProvider instanceof AssetsFontProvider && selectedPack != null);
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
        final ArrayAdapter<T> adapter = new ArrayAdapter<T>(this, R.layout.list_item, R.id.list_item, items);
        adapter.setDropDownViewResource(R.layout.list_dropdown_item);
        return adapter;
    }
}
