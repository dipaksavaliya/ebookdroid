package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

import java.io.DataOutputStream;
import java.io.IOException;

public interface FB2LineElement extends FB2MarkupElement {

    int TEXT_ELEMENT_TAG = 0;
    int WHITESPACE_ELEMENT_TAG = 1;
    int IMAGE_ELEMENT_TAG = 2;
    int RULE_ELEMENT_TAG = 3;
    
    int getHeight();

    int getWidth();

    void render(Canvas c, int y, int x);

    void adjustWidth(int w);

    boolean isSizeable();

    void serialize(DataOutputStream out) throws IOException;

}
