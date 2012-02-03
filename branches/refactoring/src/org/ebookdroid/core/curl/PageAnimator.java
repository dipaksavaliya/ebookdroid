package org.ebookdroid.core.curl;

import org.ebookdroid.core.EventDraw;
import org.ebookdroid.core.Page;
import org.ebookdroid.core.ViewState;
import org.ebookdroid.core.touch.IGestureDetector;

public interface PageAnimator extends IGestureDetector {

    PageAnimationType getType();

    void init();

    void resetPageIndexes(final int currentIndex);

    void draw(EventDraw event);

    void setViewDrawn(boolean b);

    void flipAnimationStep();

    boolean isPageVisible(final Page page, final ViewState viewState);

    void pageUpdated(int viewIndex);

    void animate(int direction);
}
