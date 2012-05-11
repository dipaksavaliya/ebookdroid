package org.ebookdroid.ui.opds;

import org.ebookdroid.R;
import org.ebookdroid.opds.Entry;
import org.ebookdroid.ui.opds.adapters.OPDSAdapter;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;

public class OPDSActivity extends AbstractActionActivity implements AdapterView.OnItemClickListener {

    private OPDSAdapter adapter;

    private TextView header;
    private ListView list;

    public OPDSActivity() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.opds);

        adapter = new OPDSAdapter(this, "http://www.plough.com/ploughCatalog_opds.xml");
        header = (TextView) findViewById(R.id.opdstext);
        list = (ListView) findViewById(R.id.opdslist);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        goHome(null);
    }

    @ActionMethod(ids = R.id.opdshome)
    public void goHome(final ActionEx action) {
        setCurrentDir(adapter.root);
    }

    @ActionMethod(ids = R.id.opdsupfolder)
    public void goUp(final ActionEx action) {
        final Entry dir = adapter.getCurrentDirectory();
        final Entry parent = dir != null ? dir.parent : null;
        if (parent != null) {
            setCurrentDir(parent);
        }
    }

    public void setCurrentDir(final Entry newDir) {
        final ImageView view = (ImageView) findViewById(R.id.opdsupfolder);
        final boolean hasParent = newDir.parent != null;
        view.setImageResource(hasParent ? R.drawable.arrowup_enabled : R.drawable.arrowup_disabled);

        header.setText(newDir.title);
        adapter.setCurrentDirectory(newDir);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        Entry selected = adapter.getItem(i);
        if (selected.catalog != null) {
            setCurrentDir(selected);
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            final Entry dir = adapter.getCurrentDirectory();
            final Entry parent = dir != null ? dir.parent : null;
            if (parent != null) {
                setCurrentDir(parent);
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
