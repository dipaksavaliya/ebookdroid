package org.ebookdroid.ui.opds;

import org.ebookdroid.R;
import org.ebookdroid.opds.Book;
import org.ebookdroid.opds.Entry;
import org.ebookdroid.opds.Feed;
import org.ebookdroid.opds.Link;
import org.ebookdroid.ui.opds.adapters.OPDSAdapter;
import org.ebookdroid.ui.opds.adapters.OPDSAdapter.FeedListener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.utils.LengthUtils;

public class OPDSActivity extends AbstractActionActivity implements AdapterView.OnItemClickListener,
        OPDSAdapter.FeedListener {

    private OPDSAdapter adapter;

    private TextView header;
    private ListView list;

    public OPDSActivity() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.opds);
        header = (TextView) findViewById(R.id.opdstext);
        list = (ListView) findViewById(R.id.opdslist);

        final Feed flibusta = new Feed("Flibusta", "http://flibusta.net/opds");
        final Feed plough = new Feed("Plough", "http://www.plough.com/ploughCatalog_opds.xml");

        adapter = new OPDSAdapter(this, flibusta, plough);
        adapter.addListener(this);

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        goHome(null);
    }

    public void setCurrentFeed(final Feed feed) {
        updateNavigation(feed);

        header.setText(feed != null ? feed.title : "OPDS feeds");
        adapter.setCurrentFeed(feed);
    }

    @ActionMethod(ids = R.id.opdshome)
    public void goHome(final ActionEx action) {
        setCurrentFeed(null);
    }

    @ActionMethod(ids = R.id.opdsupfolder)
    public void goUp(final ActionEx action) {
        final Feed dir = adapter.getCurrentFeed();
        final Feed parent = dir != null ? dir.parent : null;
        setCurrentFeed(parent);
    }

    @ActionMethod(ids = R.id.opdsnextfolder)
    public void goNext(final ActionEx action) {
        final Feed dir = adapter.getCurrentFeed();
        final Feed next = dir != null ? dir.next : null;
        if (next != null) {
            setCurrentFeed(next);
        }
    }

    @ActionMethod(ids = R.id.opdsprevfolder)
    public void goPrev(final ActionEx action) {
        final Feed dir = adapter.getCurrentFeed();
        final Feed prev = dir != null ? dir.prev : null;
        if (prev != null) {
            setCurrentFeed(prev);
        }
    }

    @Override
    public void feedLoaded(final Feed feed) {
        updateNavigation(feed);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        final Entry selected = adapter.getItem(i);
        if (selected instanceof Feed) {
            setCurrentFeed((Feed) selected);
        }
        if (selected instanceof Book) {
            downloadBook((Book) selected);
        }
    }

    private void downloadBook(final Book book) {
        if (LengthUtils.length(book.downloads) == 0) {
            return;
        }
        if (book.downloads.size() > 1) {
            List<String> itemList = new ArrayList<String>();
            for (Link link : book.downloads) {
                itemList.add(LengthUtils.safeString(link.type, "Raw type"));
            }

            final String[] items = itemList.toArray(new String[itemList.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick an item");

            builder.setItems(items, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int item) {
                    adapter.downloadBook(book.downloads.get(item));
                }
            });

            AlertDialog alert = builder.create();

            alert.show();
            return;
        }
        adapter.downloadBook(book.downloads.get(0));
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            final Feed current = adapter.getCurrentFeed();
            if (current == null) {
                adapter.close();
                finish();
            } else {
                setCurrentFeed(current.parent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void updateNavigation(final Feed feed) {
        final boolean canUp = feed != null;
        final boolean canNext = feed != null && feed.next != null;
        final boolean canPrev = feed != null && feed.prev != null;

        updateNavigation(canUp, R.id.opdsupfolder, R.drawable.arrowup_enabled, R.drawable.arrowup_disabled);
        updateNavigation(canNext, R.id.opdsnextfolder, R.drawable.arrowright_enabled, R.drawable.arrowright_disabled);
        updateNavigation(canPrev, R.id.opdsprevfolder, R.drawable.arrowleft_enabled, R.drawable.arrowleft_disabled);
    }

    protected void updateNavigation(final boolean enabled, final int viewId, final int enabledResId,
            final int disabledResId) {

        final View v = findViewById(viewId);
        if (v instanceof ImageView) {
            final ImageView view = (ImageView) v;
            view.setImageResource(enabled ? enabledResId : disabledResId);
            view.setEnabled(enabled);
        }
    }
}
