package org.ebookdroid.ui.library;

import org.ebookdroid.R;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.books.Bookmark;
import org.ebookdroid.core.PageIndex;
import org.ebookdroid.ui.library.views.FileBrowserView;

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
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.emdev.ui.AbstractActionFragment;
import org.emdev.ui.actions.ActionMenuHelper;
import org.emdev.ui.actions.ActionMethodDef;
import org.emdev.ui.actions.ActionTarget;
import org.emdev.ui.uimanager.IUIManager;
import org.emdev.utils.LayoutUtils;
import org.emdev.utils.LengthUtils;

@ActionTarget(
// action list
actions = {
// start
@ActionMethodDef(id = R.id.mainmenu_about, method = "showAbout")
// finish
})
public class FileBrowserFragment extends AbstractActionFragment<FileBrowserFragment, FileBrowserFragmentController> {

    private static final String CURRENT_DIRECTORY = "currentDirectory";

    ViewFlipper viewflipper;
    TextView header;

    public FileBrowserFragment() {
        super();
        setHasOptionsMenu(true);
    }

    public FileBrowserFragment(FileBrowserFragmentController fileBrowserFragmentController) {
        super(fileBrowserFragmentController);
        setHasOptionsMenu(true);
    }

    @Override
    protected FileBrowserFragmentController createController() {
        return new FileBrowserFragmentController(this);
    }

    @Override
    protected View onCreateViewImpl(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.browser, container, false);
        header = (TextView) root.findViewById(R.id.browsertext);
        viewflipper = (ViewFlipper) root.findViewById(R.id.browserflip);
        viewflipper.addView(LayoutUtils.fillInParent(viewflipper, new FileBrowserView(controller, controller.adapter)));
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.browsermenu, menu);
    }

    void setTitle(final File dir) {
        String path = dir.getAbsolutePath();
        getActivity().setTitle(path);
        IUIManager.instance.invalidateOptionsMenu(getActivity());
    }

    public void showProgress(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    getActivity().setProgressBarIndeterminateVisibility(show);
                    getActivity().getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, !show ? 10000 : 1);
                } catch (final Throwable e) {
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        final Object source = getContextMenuSource(v, menuInfo);

        if (source instanceof File) {
            final File node = (File) source;
            final String path = node.getAbsolutePath();

            if (node.isDirectory()) {
                createFolderMenu(menu, path);
            } else {
                createFileMenu(menu, path);
            }
        }

        ActionMenuHelper.setMenuSource(getController(), menu, source);
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

    protected void createFileMenu(final ContextMenu menu, final String path) {
        final BookSettings bs = SettingsManager.getBookSettings(path);
        final MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.book_menu, menu);
        menu.setHeaderTitle(path);
        menu.findItem(R.id.bookmenu_recentgroup).setVisible(bs != null);
        menu.findItem(R.id.bookmenu_openbookshelf).setVisible(false);
        menu.findItem(R.id.bookmenu_openbookfolder).setVisible(false);

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

    protected void createFolderMenu(final ContextMenu menu, final String path) {
        final MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.library_menu, menu);
        menu.setHeaderTitle(path);
    }
}
