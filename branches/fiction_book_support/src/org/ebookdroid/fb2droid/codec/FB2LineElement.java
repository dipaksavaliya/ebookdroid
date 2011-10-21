package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

public interface FB2LineElement extends FB2MarkupElement {

    int TEXT_ELEMENT_TAG = 0;
    int WHITESPACE_ELEMENT_TAG = 1;
    int IMAGE_ELEMENT_TAG = 2;
    int RULE_ELEMENT_TAG = 3;
    
    int getHeight();

    float getWidth();

    void render(Canvas c, int y, int x);

    void adjustWidth(float w);

    boolean isSizeable();
}
