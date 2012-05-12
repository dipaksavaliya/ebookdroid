package org.ebookdroid.ui.opds;

import org.ebookdroid.R;
import org.ebookdroid.opds.Book;
import org.ebookdroid.opds.Entry;
import org.ebookdroid.opds.Feed;
import org.ebookdroid.opds.Link;
import org.ebookdroid.ui.opds.adapters.OPDSAdapter;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionDialogBuilder;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.actions.IActionController;
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

    @ActionMethod(ids = R.id.opdsrefreshfolder)
    public void refresh(final ActionEx action) {
        final Feed dir = adapter.getCurrentFeed();
        if (dir != null) {
            setCurrentFeed(dir);
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

    protected void downloadBook(final Book book) {
        if (LengthUtils.isEmpty(book.downloads)) {
            return;
        }

        if (book.downloads.size() == 1) {
            adapter.downloadBook(book, 0);
            return;
        }

        final List<String> itemList = new ArrayList<String>();
        for (final Link link : book.downloads) {
            itemList.add(LengthUtils.safeString(link.type, "Raw type"));
        }
        final String[] items = itemList.toArray(new String[itemList.size()]);

        final ActionDialogBuilder builder = new ActionDialogBuilder(this, getController());
        builder.setTitle("Select type of book to download");
        builder.setItems(items, this.getController().getOrCreateAction(R.id.actions_downloadBook)
                .putValue("book", book));
        builder.show();
    }

    @ActionMethod(ids = R.id.actions_downloadBook)
    public void doDownload(final ActionEx action) {
        final Book book = action.getParameter("book");
        final Integer index = action.getParameter(IActionController.DIALOG_ITEM_PROPERTY);
        if (book != null && index != null) {
            adapter.downloadBook(book, index.intValue());
        }
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
