package org.ebookdroid.ui.opds;

import org.ebookdroid.R;
import org.ebookdroid.opds.Book;
import org.ebookdroid.opds.Entry;
import org.ebookdroid.opds.Feed;
import org.ebookdroid.opds.Link;
import org.ebookdroid.ui.opds.adapters.OPDSAdapter;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionDialogBuilder;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.actions.IActionController;
import org.emdev.ui.actions.params.Constant;
import org.emdev.utils.LengthUtils;

public class OPDSActivity extends AbstractActionActivity implements ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnChildClickListener, OPDSAdapter.FeedListener {

    private OPDSAdapter adapter;

    private ExpandableListView list;

    private Menu menu;

    public OPDSActivity() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.opds);
        list = (ExpandableListView) findViewById(R.id.opdslist);

        final Feed flibusta = new Feed("Flibusta", "http://flibusta.net/opds");
        final Feed plough = new Feed("Plough", "http://www.plough.com/ploughCatalog_opds.xml");

        adapter = new OPDSAdapter(this, flibusta, plough);
        adapter.addListener(this);

        list.setAdapter(adapter);
        list.setOnGroupClickListener(this);
        list.setOnChildClickListener(this);

        list.setGroupIndicator(null);
        list.setChildIndicator(null);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        goHome(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.close();
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.opdsmenu, menu);

        this.menu = menu;
        updateNavigation(adapter.getCurrentFeed());
        return true;
    }

    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu) {
        this.menu = menu;
        updateNavigation(adapter.getCurrentFeed());
        return super.onMenuOpened(featureId, menu);
    }

    public void setCurrentFeed(final Feed feed) {
        updateNavigation(feed);

        setTitle(feed != null ? feed.title : "OPDS feeds");
        adapter.setCurrentFeed(feed);
    }

    @ActionMethod(ids = R.id.opdsclose)
    public void close(final ActionEx action) {
        finish();
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
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        if (adapter.getChildrenCount(groupPosition) > 0) {
            return false;
        }
        Entry group = adapter.getGroup(groupPosition);
        if (group instanceof Feed) {
            setCurrentFeed((Feed) group);
            return true;
        } else if (group instanceof Book) {
            downloadBook((Book) group, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Entry group = adapter.getGroup(groupPosition);
        Object child = adapter.getChild(groupPosition, childPosition);
        if (child instanceof Feed) {
            setCurrentFeed((Feed) child);
        } else if (child instanceof Link) {
            downloadBook((Book) group, (Link) child);
        }

        return true;
    }

    protected void downloadBook(final Book book, final Link link) {
        if (LengthUtils.isEmpty(book.downloads)) {
            return;
        }

        if (link != null || book.downloads.size() == 1) {
            final Link target = link != null ? link : book.downloads.get(0);
            final ActionDialogBuilder builder = new ActionDialogBuilder(this, getController());
            builder.setTitle("Downloading book as");
            builder.setMessage(LengthUtils.safeString(target.type, "Raw type"));
            builder.setPositiveButton(R.id.actions_downloadBook, new Constant("book", book), new Constant(
                    IActionController.DIALOG_ITEM_PROPERTY, 0));
            builder.setNegativeButton();
            builder.show();
            return;
        }

        final List<String> itemList = new ArrayList<String>();
        for (final Link l : book.downloads) {
            itemList.add(LengthUtils.safeString(l.type, "Raw type"));
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

        if (menu != null) {
            updateItem(canUp, R.id.opdsupfolder, R.drawable.arrowup_enabled, R.drawable.arrowup_disabled);
            updateItem(canNext, R.id.opdsnextfolder, R.drawable.arrowright_enabled, R.drawable.arrowright_disabled);
            updateItem(canPrev, R.id.opdsprevfolder, R.drawable.arrowleft_enabled, R.drawable.arrowleft_disabled);
        }
    }

    protected void updateItem(final boolean enabled, final int viewId, final int enabledResId, final int disabledResId) {
        final MenuItem v = menu.findItem(viewId);
        if (v != null) {
            v.setIcon(enabled ? enabledResId : disabledResId);
            v.setEnabled(enabled);
        }
    }
}
