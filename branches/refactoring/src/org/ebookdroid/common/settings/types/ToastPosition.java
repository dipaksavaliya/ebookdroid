package org.ebookdroid.common.settings.types;

import static android.view.Gravity.*;
import static org.ebookdroid.R.string.*;

import org.ebookdroid.EBookDroidApp;

import org.emdev.utils.enums.ResourceConstant;

public enum ToastPosition implements ResourceConstant {

    /**
     * 
     */
    Invisible(pref_pagenumbertoastposition_invisible, 0),
    /**
     * 
     */
    LeftTop(pref_pagenumbertoastposition_lefttop, LEFT | TOP),
    /**
     * 
     */
    RightTop(pref_pagenumbertoastposition_righttop, RIGHT | TOP),
    /**
     * 
     */
    LeftBottom(pref_pagenumbertoastposition_leftbottom, LEFT | BOTTOM),
    /**
     * 
     */
    Bottom(pref_pagenumbertoastposition_bottom, CENTER | BOTTOM),
    /**
     * 
     */
    RightBottom(pref_pagenumbertoastposition_righbottom, RIGHT | BOTTOM);

    public final int position;
    private final String resValue;

    private ToastPosition(int resId, int position) {
        this.resValue = EBookDroidApp.context.getString(resId);
        this.position = position;
    }

    @Override
    public String getResValue() {
        return resValue;
    }
}
