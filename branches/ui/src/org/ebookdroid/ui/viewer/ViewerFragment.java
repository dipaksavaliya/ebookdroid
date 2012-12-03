package org.ebookdroid.ui.viewer;

import org.ebookdroid.R;
import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.common.settings.types.ToastPosition;
import org.ebookdroid.common.touch.TouchManagerView;
import org.ebookdroid.ui.viewer.views.ManualCropView;
import org.ebookdroid.ui.viewer.views.PageViewZoomControls;
import org.ebookdroid.ui.viewer.views.SearchControls;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.emdev.common.android.AndroidVersion;
import org.emdev.ui.AbstractActionFragment;
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
public class ViewerFragment extends AbstractActionFragment<ViewerFragment, ViewerFragmentController> {

    public static final DisplayMetrics DM = new DisplayMetrics();

    IView view;

    private Toast pageNumberToast;

    private Toast zoomToast;

    private PageViewZoomControls zoomControls;

    private SearchControls searchControls;

    private FrameLayout frameLayout;

    private TouchManagerView touchView;

    private ManualCropView cropControls;

    /**
     * Instantiates a new base viewer activity.
     */
    public ViewerFragment() {
        setHasOptionsMenu(true);
    }


    public ViewerFragment(ViewerFragmentController viewerFragmentController) {
        super(viewerFragmentController);
        setHasOptionsMenu(true);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.emdev.ui.AbstractActionActivity#createController()
     */
    @Override
    protected ViewerFragmentController createController() {
        return new ViewerFragmentController(this);
    }

    @Override
    protected View onCreateViewImpl(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(DM);
        LCTX.i("XDPI=" + DM.xdpi + ", YDPI=" + DM.ydpi);

        frameLayout = new FrameLayout(getActivity());

        view = AppSettings.current().viewType.create(getController());
        this.registerForContextMenu(view.getView());

        LayoutUtils.fillInParent(frameLayout, view.getView());

        frameLayout.addView(view.getView());
        frameLayout.addView(getZoomControls());
        frameLayout.addView(getManualCropControls());
        frameLayout.addView(getSearchControls());
        frameLayout.addView(getTouchView());

        return frameLayout;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreateImpl(final Bundle savedInstanceState) {

    }

    @Override
    protected void onResumeImpl() {
        IUIManager.instance.onResume(getActivity());
    }

    @Override
    protected void onPauseImpl() {
        IUIManager.instance.onPause(getActivity());
    }

    protected IView createView() {
        return AppSettings.current().viewType.create(getController());
    }

    public TouchManagerView getTouchView() {
        if (touchView == null) {
            touchView = new TouchManagerView(getController());
        }
        return touchView;
    }

    public void currentPageChanged(final String pageText, final String bookTitle) {
        if (LengthUtils.isEmpty(pageText)) {
            return;
        }

        final AppSettings app = AppSettings.current();
        if (IUIManager.instance.isTitleVisible(getActivity()) && app.pageInTitle) {
            getActivity().getWindow().setTitle("(" + pageText + ") " + bookTitle);
            return;
        }

        if (app.pageNumberToastPosition == ToastPosition.Invisible) {
            return;
        }
        if (pageNumberToast != null) {
            pageNumberToast.setText(pageText);
        } else {
            pageNumberToast = Toast.makeText(getActivity(), pageText, Toast.LENGTH_SHORT);
        }

        pageNumberToast.setGravity(app.pageNumberToastPosition.position, 0, 0);
        pageNumberToast.show();
    }

    public void zoomChanged(final float zoom) {
        if (getZoomControls().isShown()) {
            return;
        }

        final AppSettings app = AppSettings.current();

        if (app.zoomToastPosition == ToastPosition.Invisible) {
            return;
        }

        final String zoomText = String.format("%.2f", zoom) + "x";

        if (zoomToast != null) {
            zoomToast.setText(zoomText);
        } else {
            zoomToast = Toast.makeText(getActivity(), zoomText, Toast.LENGTH_SHORT);
        }

        zoomToast.setGravity(app.zoomToastPosition.position, 0, 0);
        zoomToast.show();
    }

    public PageViewZoomControls getZoomControls() {
        if (zoomControls == null) {
            zoomControls = new PageViewZoomControls(getActivity(), getController().getZoomModel());
            zoomControls.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        }
        return zoomControls;
    }

    public SearchControls getSearchControls() {
        if (searchControls == null) {
            searchControls = new SearchControls(getActivity(), getController());
        }
        return searchControls;
    }

    public ManualCropView getManualCropControls() {
        if (cropControls == null) {
            cropControls = new ManualCropView(getController());
        }
        return cropControls;
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
        menu.clear();
        menu.setHeaderTitle(R.string.app_name);
        menu.setHeaderIcon(R.drawable.application_icon);
        final MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.mainmenu_context, menu);
        getController().updateMenuItems(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        if (hasNormalMenu()) {
            inflater.inflate(R.menu.mainmenu, menu);
        } else {
            inflater.inflate(R.menu.mainmenu_context, menu);
        }
    }

    protected boolean hasNormalMenu() {
        return AndroidVersion.lessThan4x || IUIManager.instance.isTabletUi(getActivity())
                || AppSettings.current().showTitle;
    }

    public void showToastText(final int duration, final int resId, final Object... args) {
        Toast.makeText(getActivity(), getResources().getString(resId, args), duration).show();
    }

}
