package org.ebookdroid.ui.viewer;

import org.ebookdroid.CodecType;
import org.ebookdroid.R;
import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.common.settings.ISettingsChangeListener;
import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.books.Bookmark;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.EventDraw;
import org.ebookdroid.core.Page;
import org.ebookdroid.core.PageIndex;
import org.ebookdroid.core.ViewState;
import org.ebookdroid.core.codec.OutlineLink;
import org.ebookdroid.core.events.CurrentPageListener;
import org.ebookdroid.core.events.DecodingProgressListener;
import org.ebookdroid.core.models.DecodingProgressModel;
import org.ebookdroid.core.models.DocumentModel;
import org.ebookdroid.core.models.ZoomModel;
import org.ebookdroid.core.touch.TouchManager;
import org.ebookdroid.core.touch.TouchManagerView;
import org.ebookdroid.ui.library.adapters.OutlineAdapter;
import org.ebookdroid.ui.settings.SettingsUI;
import org.ebookdroid.ui.viewer.dialogs.GoToPageDialog;
import org.ebookdroid.ui.viewer.views.PageViewZoomControls;
import org.ebookdroid.ui.viewer.views.ViewEffects;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.emdev.ui.AbstractActionActivity;
import org.emdev.ui.actions.ActionDialogBuilder;
import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.actions.ActionMethodDef;
import org.emdev.ui.actions.ActionTarget;
import org.emdev.ui.actions.IActionController;
import org.emdev.ui.actions.params.Constant;
import org.emdev.ui.actions.params.EditableValue;
import org.emdev.ui.fullscreen.IFullScreenManager;
import org.emdev.utils.LengthUtils;
import org.emdev.utils.StringUtils;
import org.emdev.utils.filesystem.PathFromUri;

@ActionTarget(
// action list
actions = {
        // start
        @ActionMethodDef(id = R.id.actions_addBookmark, method = "addBookmark"),
        @ActionMethodDef(id = R.id.mainmenu_close, method = "closeActivity"),
        @ActionMethodDef(id = R.id.actions_gotoOutlineItem, method = "gotoOutlineItem"),
        @ActionMethodDef(id = R.id.actions_redecodingWithPassord, method = "redecodingWithPassord"),
        @ActionMethodDef(id = R.id.mainmenu_settings, method = "showAppSettings"),
        @ActionMethodDef(id = R.id.mainmenu_bookmark, method = "showBookmarkDialog"),
        @ActionMethodDef(id = R.id.mainmenu_booksettings, method = "showBookSettings"),
        @ActionMethodDef(id = R.id.mainmenu_goto_page, method = "showDialog"),
        @ActionMethodDef(id = R.id.mainmenu_outline, method = "showOutline"),
        @ActionMethodDef(id = R.id.mainmenu_nightmode, method = "toggleNightMode"),
        @ActionMethodDef(id = R.id.mainmenu_zoom, method = "toggleControls"),
        @ActionMethodDef(id = R.id.mainmenu_thumbnail, method = "setCurrentPageAsThumbnail"),
        @ActionMethodDef(id = R.id.mainmenu_about, method = "showAbout"),
        @ActionMethodDef(id = R.id.actions_toggleTouchManagerView, method = "toggleControls"),
        @ActionMethodDef(id = R.id.actions_openOptionsMenu, method = "openOptionsMenu")
// finish
})
public class ViewerActivity extends AbstractActionActivity implements IActivityController, DecodingProgressListener,
        CurrentPageListener, ISettingsChangeListener {

    public static final LogContext LCTX = LogContext.ROOT.lctx("Core");

    private static final String E_MAIL_ATTACHMENT = "[E-mail Attachment]";

    private static final int DIALOG_GOTO = 0;

    public static final DisplayMetrics DM = new DisplayMetrics();

    private IView view;

    private final AtomicReference<IViewController> ctrl = new AtomicReference<IViewController>(new EmptyContoller());

    private Toast pageNumberToast;

    private ZoomModel zoomModel;

    private PageViewZoomControls zoomControls;

    private FrameLayout frameLayout;

    private DecodingProgressModel progressModel;

    private DocumentModel documentModel;

    private String currentFilename;

    private TouchManagerView touchView;

    private boolean menuClosedCalled;

    private boolean temporaryBook;

    private CodecType codecType;

    /**
     * Instantiates a new base viewer activity.
     */
    public ViewerActivity() {
        super();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindowManager().getDefaultDisplay().getMetrics(DM);
        LCTX.i("XDPI=" + DM.xdpi + ", YDPI=" + DM.ydpi);

        frameLayout = createMainContainer();
        view = createView();

        actions.createAction(R.id.mainmenu_goto_page, new Constant("dialogId", DIALOG_GOTO));
        actions.createAction(R.id.mainmenu_zoom).putValue("view", getZoomControls());
        actions.createAction(R.id.actions_toggleTouchManagerView).putValue("view", getTouchView());

        codecType = CodecType.getByUri(getIntent().getData());
        if (codecType == null) {
            throw new RuntimeException("Unknown intent data type: " + getIntent().getData());
        }

        SettingsManager.addListener(this);

        initActivity();
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        IFullScreenManager.instance.onResume(getWindow());
    }

    @Override
    protected void onPause() {
        super.onPause();
        IFullScreenManager.instance.onPause(getWindow());
        SettingsManager.storeBookSettings();
    }

    @Override
    protected void onDestroy() {
        if (documentModel != null) {
            documentModel.recycle();
        }
        if (temporaryBook) {
            CacheManager.clear(E_MAIL_ATTACHMENT);
        }
        SettingsManager.removeListener(this);
        super.onDestroy();
    }

    private void initActivity() {
        final AppSettings oldSettings = null;
        final AppSettings newSettings = SettingsManager.getAppSettings();
        final AppSettings.Diff diff = new AppSettings.Diff(oldSettings, newSettings);
        this.onAppSettingsChanged(oldSettings, newSettings, diff);
    }

    protected IView createView() {
        return SettingsManager.getAppSettings().getDocumentViewType().create(this);
    }

    private void initView() {

        view.getView().setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        frameLayout.addView(view.getView());
        frameLayout.addView(getZoomControls());
        frameLayout.addView(getTouchView());
        setContentView(frameLayout);

        documentModel = new DocumentModel(codecType);
        documentModel.addListener(ViewerActivity.this);
        progressModel = new DecodingProgressModel();
        progressModel.addListener(ViewerActivity.this);

        final Uri uri = getIntent().getData();
        String fileName = "";

        if (getIntent().getScheme().equals("content")) {
            temporaryBook = true;
            fileName = E_MAIL_ATTACHMENT;
            CacheManager.clear(fileName);
        } else {
            fileName = PathFromUri.retrieve(getContentResolver(), uri);
        }
        SettingsManager.init(fileName);
        SettingsManager.applyBookSettingsChanges(null, SettingsManager.getBookSettings(), null);

        startDecoding(fileName, "");
    }

    private TouchManagerView getTouchView() {
        if (touchView == null) {
            touchView = new TouchManagerView(this);
        }
        return touchView;
    }

    private void startDecoding(final String fileName, final String password) {
        view.getView().post(new BookLoadTask(fileName, password));
    }

    private void askPassword(final String fileName) {
        setContentView(R.layout.password);
        final Button ok = (Button) findViewById(R.id.pass_ok);

        ok.setOnClickListener(actions.getOrCreateAction(R.id.actions_redecodingWithPassord).putValue("fileName",
                fileName));

        final Button cancel = (Button) findViewById(R.id.pass_cancel);
        cancel.setOnClickListener(actions.getOrCreateAction(R.id.mainmenu_close));
    }

    @ActionMethod(ids = R.id.actions_redecodingWithPassord)
    public void redecodingWithPassord(final ActionEx action) {
        final EditText te = (EditText) findViewById(R.id.pass_req);
        final String fileName = action.getParameter("fileName");

        startDecoding(fileName, te.getText().toString());
    }

    private void showErrorDlg(final String msg) {
        setContentView(R.layout.error);
        final TextView errortext = (TextView) findViewById(R.id.error_text);
        if (msg != null && msg.length() > 0) {
            errortext.setText(msg);
        } else {
            errortext.setText("Unexpected error occured!");
        }
        final Button cancel = (Button) findViewById(R.id.error_close);
        cancel.setOnClickListener(actions.getOrCreateAction(R.id.mainmenu_close));
    }

    @Override
    public IViewController switchDocumentController() {
        try {
            final BookSettings bs = SettingsManager.getBookSettings();

            final IViewController newDc = bs.viewMode.create(this);
            final IViewController oldDc = ctrl.getAndSet(newDc);

            getZoomModel().removeListener(oldDc);
            getZoomModel().addListener(newDc);

            return newDc;
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void decodingProgressChanged(final int currentlyDecoding) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    setProgressBarIndeterminateVisibility(true);
                    getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                            currentlyDecoding == 0 ? 10000 : currentlyDecoding);
                } catch (final Throwable e) {
                }
            }
        });
    }

    @Override
    public void currentPageChanged(final PageIndex oldIndex, final PageIndex newIndex) {
        final int pageCount = documentModel.getPageCount();
        String prefix = "";

        if (pageCount > 0) {
            final String pageText = (newIndex.viewIndex + 1) + "/" + pageCount;
            if (SettingsManager.getAppSettings().getPageInTitle()) {
                prefix = "(" + pageText + ") ";
            } else {
                if (pageNumberToast != null) {
                    pageNumberToast.setText(pageText);
                } else {
                    pageNumberToast = Toast.makeText(this, pageText, 300);
                }
                pageNumberToast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
                pageNumberToast.show();
            }
        }

        getWindow().setTitle(prefix + currentFilename);
        SettingsManager.currentPageChanged(oldIndex, newIndex);
    }

    private void setWindowTitle() {
        currentFilename = LengthUtils.safeString(getIntent().getData().getLastPathSegment(), E_MAIL_ATTACHMENT);
        currentFilename = StringUtils.cleanupTitle(currentFilename);
        getWindow().setTitle(currentFilename);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setWindowTitle();
    }

    private PageViewZoomControls getZoomControls() {
        if (zoomControls == null) {
            zoomControls = new PageViewZoomControls(this, getZoomModel());
            zoomControls.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        }
        return zoomControls;
    }

    private FrameLayout createMainContainer() {
        return new FrameLayout(this);
    }

    /**
     * Called on creation options menu
     * 
     * @param menu
     *            the main menu
     * @return true, if successful
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu) {
        getView().changeLayoutLock(true);
        IFullScreenManager.instance.onMenuOpened(getWindow());
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onPanelClosed(final int featureId, final Menu menu) {
        menuClosedCalled = false;
        super.onPanelClosed(featureId, menu);
        if (!menuClosedCalled) {
            onOptionsMenuClosed(menu);
        }
    }

    @Override
    public void onOptionsMenuClosed(final Menu menu) {
        menuClosedCalled = true;
        IFullScreenManager.instance.onMenuClosed(getWindow());
        view.changeLayoutLock(false);
    }

    @ActionMethod(ids = R.id.actions_openOptionsMenu)
    public void openOptionsMenu(final ActionEx action) {
        if (!getView().isLayoutLocked()) {
            this.openOptionsMenu();
        }
    }

    @ActionMethod(ids = R.id.actions_gotoOutlineItem)
    public void gotoOutlineItem(final ActionEx action) {
        final Integer item = action.getParameter(IActionController.DIALOG_ITEM_PROPERTY);
        final List<OutlineLink> outline = action.getParameter("outline");

        final String link = outline.get(item).getLink();
        if (link.startsWith("#")) {
            int pageNumber = 0;
            try {
                pageNumber = Integer.parseInt(link.substring(1).replace(" ", ""));
            } catch (final Exception e) {
                pageNumber = 0;
            }
            if (pageNumber < 1 || pageNumber > documentModel.getPageCount()) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(R.string.error_page_out_of_rande,
                                documentModel.getDecodeService().getPageCount()), 2000).show();
                return;
            }
            getDocumentController().goToPage(pageNumber - 1);
        } else if (link.startsWith("http:")) {
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(link));
            startActivity(i);
        }
    }

    @ActionMethod(ids = R.id.mainmenu_outline)
    public void showOutline(final ActionEx action) {
        final List<OutlineLink> outline = documentModel.getDecodeService().getOutline();
        if ((outline != null) && (outline.size() > 0)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.outline_title);
            builder.setAdapter(new OutlineAdapter(this, outline),
                    actions.getOrCreateAction(R.id.actions_gotoOutlineItem).putValue("outline", outline));
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.outline_missed, Toast.LENGTH_SHORT).show();
        }
    }

    @ActionMethod(ids = R.id.mainmenu_goto_page)
    public void showDialog(final ActionEx action) {
        final Integer dialogId = action.getParameter("dialogId");
        showDialog(dialogId);
    }

    @ActionMethod(ids = R.id.mainmenu_booksettings)
    public void showBookSettings(final ActionEx action) {
        SettingsUI.showBookSettings(this, SettingsManager.getBookSettings().fileName);
    }

    @ActionMethod(ids = R.id.mainmenu_settings)
    public void showAppSettings(final ActionEx action) {
        SettingsUI.showAppSettings(this);
    }

    @ActionMethod(ids = R.id.mainmenu_nightmode)
    public void toggleNightMode(final ActionEx action) {
        SettingsManager.getAppSettings().switchNightMode();
        getDocumentController().toggleNightMode(SettingsManager.getAppSettings().getNightMode());
    }

    @ActionMethod(ids = R.id.mainmenu_thumbnail)
    public void setCurrentPageAsThumbnail(final ActionEx action) {
        final Page page = getDocumentModel().getCurrentPageObject();
        if (page != null) {
            getDocumentModel().createBookThumbnail(SettingsManager.getBookSettings(), page, true);
        }
    }

    @ActionMethod(ids = R.id.mainmenu_bookmark)
    public void showBookmarkDialog(final ActionEx action) {
        final int page = getDocumentModel().getCurrentViewPageIndex();

        final String message = getString(R.string.add_bookmark_name);

        final EditText input = new EditText(this);
        input.setText(getString(R.string.text_page) + " " + (page + 1));
        input.selectAll();

        final ActionDialogBuilder builder = new ActionDialogBuilder(actions);
        builder.setTitle(R.string.menu_add_bookmark).setMessage(message).setView(input);
        builder.setPositiveButton(R.id.actions_addBookmark, new EditableValue("input", input));
        builder.setNegativeButton().show();
    }

    @ActionMethod(ids = R.id.actions_addBookmark)
    public void addBookmark(final ActionEx action) {
        final Editable value = action.getParameter("input");
        final String name = value.toString();
        final BookSettings bs = SettingsManager.getBookSettings();
        bs.bookmarks.add(new Bookmark(name, getDocumentModel().getCurrentIndex(), 0, 0));
        SettingsManager.edit(bs).commit();
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_GOTO:
                return new GoToPageDialog(this);
        }
        return null;
    }

    /**
     * Gets the zoom model.
     * 
     * @return the zoom model
     */
    @Override
    public ZoomModel getZoomModel() {
        if (zoomModel == null) {
            zoomModel = new ZoomModel();
        }
        return zoomModel;
    }

    @Override
    public DecodeService getDecodeService() {
        return documentModel != null ? documentModel.getDecodeService() : null;
    }

    /**
     * Gets the decoding progress model.
     * 
     * @return the decoding progress model
     */
    @Override
    public DecodingProgressModel getDecodingProgressModel() {
        return progressModel;
    }

    @Override
    public DocumentModel getDocumentModel() {
        return documentModel;
    }

    @Override
    public IViewController getDocumentController() {
        return ctrl.get();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public IView getView() {
        return view;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public IActionController<?> getActionController() {
        return actions;
    }

    @ActionMethod(ids = { R.id.mainmenu_zoom, R.id.actions_toggleTouchManagerView })
    public void toggleControls(final ActionEx action) {
        final View view = action.getParameter("view");
        ViewEffects.toggleControls(view);
    }

    @Override
    public final boolean dispatchKeyEvent(final KeyEvent event) {
        if (getDocumentController().dispatchKeyEvent(event)) {
            return true;
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (event.getRepeatCount() == 0) {
                        if (touchView.isShown()) {
                            ViewEffects.toggleControls(touchView);
                        } else {
                            closeActivity(null);
                        }
                    }
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        return false;
    }

    @ActionMethod(ids = R.id.mainmenu_close)
    public void closeActivity(final ActionEx action) {
        if (documentModel != null) {
            documentModel.recycle();
        }
        if (temporaryBook) {
            CacheManager.clear(E_MAIL_ATTACHMENT);
            SettingsManager.removeCurrentBookSettings();
        } else {
            SettingsManager.clearCurrentBookSettings();
        }
        finish();
    }

    @Override
    public void onAppSettingsChanged(final AppSettings oldSettings, final AppSettings newSettings,
            final AppSettings.Diff diff) {
        if (diff.isRotationChanged()) {
            setRequestedOrientation(newSettings.getRotation().getOrientation());
        }

        if (diff.isFullScreenChanged()) {
            IFullScreenManager.instance.setFullScreenMode(getWindow(), newSettings.getFullScreen());
        }

        if (diff.isShowTitleChanged() && diff.isFirstTime()) {
            final Window window = getWindow();
            try {
                if (!newSettings.getShowTitle()) {
                    window.requestFeature(Window.FEATURE_NO_TITLE);
                } else {
                    // Android 3.0+ you need both progress!!!
                    window.requestFeature(Window.FEATURE_PROGRESS);
                    window.requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
                    setProgressBarIndeterminate(true);
                }
            } catch (final Throwable th) {
                LCTX.e("Error on requestFeature call: " + th.getMessage());
            }
        }
        if (diff.isKeepScreenOnChanged()) {
            view.getView().setKeepScreenOn(newSettings.isKeepScreenOn());
        }

        if (diff.isNightModeChanged() && !diff.isFirstTime()) {
            getDocumentController().toggleNightMode(newSettings.getNightMode());
        }

        TouchManager.loadFromSettings(newSettings);

        BitmapManager.setPartSize(newSettings.getBitmapSize());
        BitmapManager.setUseEarlyRecycling(newSettings.getUseEarlyRecycling());
    }

    @Override
    public void onBookSettingsChanged(final BookSettings oldSettings, final BookSettings newSettings,
            final BookSettings.Diff diff, final AppSettings.Diff appDiff) {

        boolean redrawn = false;
        if (diff.isViewModeChanged() || diff.isSplitPagesChanged() || diff.isCropPagesChanged()) {
            redrawn = true;
            final IViewController newDc = switchDocumentController();
            if (!diff.isFirstTime()) {
                newDc.init(null);
                newDc.show();
            }
        }

        if (diff.isFirstTime()) {
            getZoomModel().initZoom(newSettings.getZoom());
        }

        final IViewController dc = getDocumentController();
        if (diff.isPageAlignChanged()) {
            dc.setAlign(newSettings.pageAlign);
        }

        if (diff.isAnimationTypeChanged()) {
            dc.updateAnimationType();
        }

        if (!redrawn && appDiff != null) {
            if (appDiff.isPagesInMemoryChanged()) {
                dc.updateMemorySettings();
            }
        }

        final DocumentModel dm = getDocumentModel();
        if (dm != null) {
            currentPageChanged(PageIndex.NULL, dm.getCurrentIndex());
        }
    }

    final class BookLoadTask extends AsyncTask<String, String, Exception> implements IBookLoadTask, Runnable {

        private String m_fileName;
        private String m_password;
        private ProgressDialog progressDialog;

        public BookLoadTask(final String fileName, final String password) {
            m_fileName = fileName;
            m_password = password;
        }

        @Override
        public void run() {
            execute(" ");
        }

        @Override
        protected void onPreExecute() {
            LCTX.d("onPreExecute(): start");
            try {
                final String message = getString(R.string.msg_loading);
                progressDialog = ProgressDialog.show(ViewerActivity.this, "", message, true);
            } catch (final Throwable th) {
                LCTX.e("Unexpected error", th);
            } finally {
                LCTX.d("onPreExecute(): finish");
            }
        }

        @Override
        protected Exception doInBackground(final String... params) {
            LCTX.d("doInBackground(): start");
            try {
                if (getIntent().getScheme().equals("content")) {
                    final File tempFile = CacheManager.createTempFile(getIntent().getData());
                    m_fileName = tempFile.getAbsolutePath();
                }
                getView().waitForInitialization();
                documentModel.open(m_fileName, m_password);
                getDocumentController().init(this);
                return null;
            } catch (final Exception e) {
                LCTX.e(e.getMessage(), e);
                return e;
            } catch (final Throwable th) {
                LCTX.e("Unexpected error", th);
                return new Exception(th.getMessage());
            } finally {
                LCTX.d("doInBackground(): finish");
            }
        }

        @Override
        protected void onPostExecute(final Exception result) {
            LCTX.d("onPostExecute(): start");
            try {
                if (result == null) {
                    getDocumentController().show();

                    final DocumentModel dm = getDocumentModel();
                    currentPageChanged(PageIndex.NULL, dm.getCurrentIndex());

                    setProgressBarIndeterminateVisibility(false);

                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();

                    final String msg = result.getMessage();
                    if ("PDF needs a password!".equals(msg)) {
                        askPassword(m_fileName);
                    } else {
                        showErrorDlg(msg);
                    }
                }
            } catch (final Throwable th) {
                LCTX.e("Unexpected error", th);
            } finally {
                LCTX.d("onPostExecute(): finish");
            }
        }

        @Override
        public void setProgressDialogMessage(final int resourceID, final Object... args) {
            final String message = getString(resourceID, args);
            publishProgress(message);
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            if (values != null && values.length > 0) {
                progressDialog.setMessage(values[0]);
            }
        }

    }

    private class EmptyContoller implements IViewController {

        @Override
        public void zoomChanged(float oldZoom, float newZoom, boolean committed) {
        }

        @Override
        public ViewState goToPage(final int page) {
            return null;
        }

        @Override
        public ViewState goToPage(int page, float offsetX, float offsetY) {
            return null;
        }

        @Override
        public void invalidatePageSizes(final InvalidateSizeReason reason, final Page changedPage) {
        }

        @Override
        public int getFirstVisiblePage() {
            return 0;
        }

        @Override
        public int calculateCurrentPage(final ViewState viewState, final int firstVisible, final int lastVisible) {
            return 0;
        }

        @Override
        public int getLastVisiblePage() {
            return 0;
        }

        @Override
        public void verticalConfigScroll(final int i) {
        }

        @Override
        public void redrawView() {
        }

        @Override
        public void redrawView(final ViewState viewState) {
        }

        @Override
        public void setAlign(final PageAlign byResValue) {
        }

        @Override
        public IActivityController getBase() {
            return ViewerActivity.this;
        }

        @Override
        public IView getView() {
            return view;
        }

        @Override
        public void updateAnimationType() {
        }

        @Override
        public void updateMemorySettings() {
        }

        @Override
        public boolean onLayoutChanged(final boolean layoutChanged, final boolean layoutLocked, final Rect oldLaout,
                final Rect newLayout) {
            return false;
        }

        @Override
        public Rect getScrollLimits() {
            return new Rect(0, 0, 0, 0);
        }

        @Override
        public boolean onTouchEvent(final MotionEvent ev) {
            return false;
        }

        @Override
        public void onScrollChanged(int dX, int dY) {
        }

        @Override
        public boolean dispatchKeyEvent(final KeyEvent event) {
            return false;
        }

        @Override
        public void show() {
        }

        @Override
        public final void init(final IBookLoadTask task) {
        }

        @Override
        public void toggleNightMode(boolean nightMode) {
        }

        @Override
        public void drawView(EventDraw eventDraw) {
        }

        @Override
        public boolean isPageVisible(Page page, ViewState viewState) {
            return false;
        }

        @Override
        public void pageUpdated(ViewState viewState, Page page) {
        }

        @Override
        public void invalidateScroll() {
        }
    }
}
