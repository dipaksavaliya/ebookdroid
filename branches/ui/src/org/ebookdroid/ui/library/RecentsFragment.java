package org.ebookdroid.ui.library;

import org.ebookdroid.R;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.books.Bookmark;
import org.ebookdroid.core.PageIndex;
import org.ebookdroid.ui.library.adapters.BookNode;
import org.ebookdroid.ui.library.adapters.BookShelfAdapter;
import org.ebookdroid.ui.library.adapters.BooksAdapter;
import org.ebookdroid.ui.library.adapters.LibraryAdapter;
import org.ebookdroid.ui.library.adapters.RecentAdapter;
import org.ebookdroid.ui.library.views.BookcaseView;
import org.ebookdroid.ui.library.views.LibraryView;
import org.ebookdroid.ui.library.views.RecentBooksView;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.emdev.ui.AbstractActionFragment;
import org.emdev.ui.actions.ActionMenuHelper;
import org.emdev.ui.uimanager.IUIManager;
import org.emdev.utils.LengthUtils;

public class RecentsFragment extends AbstractActionFragment<RecentsFragment, RecentsFragmentController> {

    public static final int VIEW_RECENT = 0;
    public static final int VIEW_LIBRARY = 1;

    private ViewFlipper viewflipper;

    private BookcaseView bookcaseView;
    private RecentBooksView recentBooksView;
    private LibraryView libraryView;

    public RecentsFragment() {
        setHasOptionsMenu(true);
    }

    public RecentsFragment(RecentsFragmentController recentsFragmentController) {
        super(recentsFragmentController);
        setHasOptionsMenu(true);
    }

    @Override
    protected RecentsFragmentController createController() {
        return new RecentsFragmentController(this);
    }

    @Override
    protected View onCreateViewImpl(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.recent, container, false);
        viewflipper = (ViewFlipper) root.findViewById(R.id.recentflip);
        bookcaseView = null;
        recentBooksView = null;
        libraryView = null;
        return root;
    }

    @Override
    protected void onResumeImpl() {
        IUIManager.instance.invalidateOptionsMenu(this.getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recentmenu, menu);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        final Object source = getContextMenuSource(v, menuInfo);

        if (source instanceof BookNode) {
            onCreateBookMenu(menu, (BookNode) source);
        } else if (source instanceof BookShelfAdapter) {
            onCreateShelfMenu(menu, (BookShelfAdapter) source);
        }

        ActionMenuHelper.setMenuSource(controller, menu, source);
    }

    protected Object getContextMenuSource(final View v, final ContextMenuInfo menuInfo) {
        Object source = null;
        if (menuInfo instanceof AdapterContextMenuInfo) {
            final AbsListView list = (AbsListView) v;
            final AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
            source = list.getAdapter().getItem(mi.position);
        } else if (menuInfo instanceof ExpandableListContextMenuInfo) {
            final ExpandableListView list = (ExpandableListView) v;
            final ExpandableListAdapter adapter = list.getExpandableListAdapter();
            final ExpandableListContextMenuInfo mi = (ExpandableListContextMenuInfo) menuInfo;
            final long pp = mi.packedPosition;
            final int group = ExpandableListView.getPackedPositionGroup(pp);
            final int child = ExpandableListView.getPackedPositionChild(pp);
            if (child >= 0) {
                source = adapter.getChild(group, child);
            } else {
                source = adapter.getGroup(group);
            }
        }
        return source;
    }

    protected void onCreateBookMenu(final ContextMenu menu, final BookNode node) {
        final BookSettings bs = node.settings;
        final MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.book_menu, menu);

        menu.setHeaderTitle(node.path);
        menu.findItem(R.id.bookmenu_recentgroup).setVisible(bs != null);

        final BookShelfAdapter bookShelf = controller.getBookShelf(node);
        final BookShelfAdapter current = bookcaseView != null ? controller.getBookShelf(bookcaseView.getCurrentList())
                : null;
        menu.findItem(R.id.bookmenu_openbookshelf).setVisible(
                bookShelf != null && current != null && bookShelf != current);

        final MenuItem om = menu.findItem(R.id.bookmenu_open);
        final SubMenu osm = om != null ? om.getSubMenu() : null;
        if (osm == null) {
            return;
        }
        osm.clear();

        final List<Bookmark> list = new ArrayList<Bookmark>();
        list.add(new Bookmark(true, getString(R.string.bookmark_start), PageIndex.FIRST, 0, 0));
        list.add(new Bookmark(true, getString(R.string.bookmark_end), PageIndex.LAST, 0, 1));
        if (bs != null) {
            if (LengthUtils.isNotEmpty(bs.bookmarks)) {
                list.addAll(bs.bookmarks);
            }
            list.add(new Bookmark(true, getString(R.string.bookmark_current), bs.currentPage, bs.offsetX, bs.offsetY));
        }

        Collections.sort(list);
        for (final Bookmark b : list) {
            addBookmarkMenuItem(osm, b);
        }
    }

    protected void addBookmarkMenuItem(final Menu menu, final Bookmark b) {
        final MenuItem bmi = menu.add(R.id.actions_goToBookmarkGroup, R.id.actions_goToBookmark, Menu.NONE, b.name);
        bmi.setIcon(R.drawable.viewer_menu_bookmark);
        ActionMenuHelper.setMenuItemExtra(bmi, "bookmark", b);
    }

    protected void onCreateShelfMenu(final ContextMenu menu, final BookShelfAdapter a) {
        final MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.library_menu, menu);
        menu.setHeaderTitle(a.name);
    }

    void changeLibraryView(final int view) {
        final ViewFlipper vf = viewflipper;
        if (view == VIEW_LIBRARY) {
            vf.setDisplayedChild(VIEW_LIBRARY);
        } else {
            vf.setDisplayedChild(VIEW_RECENT);
        }
    }

    public int getCurrentList() {
        return bookcaseView.getCurrentList();
    }

    int getViewMode() {
        final ViewFlipper vf = viewflipper;
        return vf != null ? vf.getDisplayedChild() : VIEW_RECENT;
    }

    void showBookshelf(final int shelfIndex) {
        if (bookcaseView != null) {
            bookcaseView.setCurrentList(shelfIndex);
        }
    }

    void showNextBookshelf() {
        if (bookcaseView != null) {
            bookcaseView.nextList();
        }
    }

    void showPrevBookshelf() {
        if (bookcaseView != null) {
            bookcaseView.prevList();
        }
    }

    void showBookcase(final BooksAdapter bookshelfAdapter, final RecentAdapter recentAdapter) {
        viewflipper.removeAllViews();
        if (bookcaseView == null) {
            bookcaseView = (BookcaseView) LayoutInflater.from(getActivity()).inflate(R.layout.bookcase_view,
                    viewflipper, false);
            bookcaseView.init(bookshelfAdapter);
        }
        viewflipper.addView(bookcaseView, 0);
    }

    void showLibrary(final LibraryAdapter libraryAdapter, final RecentAdapter recentAdapter) {
        if (recentBooksView == null) {
            recentBooksView = new RecentBooksView(getController(), recentAdapter);
            registerForContextMenu(recentBooksView);
        }
        if (libraryView == null) {
            libraryView = new LibraryView(getController(), libraryAdapter);
            registerForContextMenu(libraryView);
        }

        viewflipper.removeAllViews();
        viewflipper.addView(recentBooksView, VIEW_RECENT);
        viewflipper.addView(libraryView, VIEW_LIBRARY);
    }
}
