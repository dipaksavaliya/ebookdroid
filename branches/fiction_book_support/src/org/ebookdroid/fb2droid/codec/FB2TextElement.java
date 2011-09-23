package org.ebookdroid.fb2droid.codec;

import android.graphics.Canvas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FB2TextElement extends AbstractFB2LineElement {

    private final char[] chars;
    private final int start;
    private final int length;
    private final int width;

    private RenderingStyle renderingState;

    public FB2TextElement(char[] ch, int st, int len, RenderingStyle renderingState) {
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.renderingState = renderingState;
        this.width = (int) renderingState.getTextPaint().measureText(ch, st, len);
    }

    public FB2TextElement(char[] ch, int st, int len, RenderingStyle style, int width) {
        this.chars = ch;
        this.start = st;
        this.length = len;
        this.renderingState = style;
        this.width = width;
    }

    @Override
    public int getHeight() {
        return renderingState.textSize;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void render(final Canvas c, final int y, final int x) {
        c.drawText(chars, start, length, x, y, renderingState.getTextPaint());
    }

    @Override
    public void adjustWidth(final int w) {
    }

    @Override
    public boolean isSizeable() {
        return false;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(TEXT_ELEMENT_TAG);
        out.writeInt(width);
        out.writeInt(length);
        for (int i = start; i < start + length; i++) {
            out.writeChar(chars[i]);
        }
        out.writeInt(renderingState.textSize);
        out.writeBoolean(renderingState.bold);
        out.writeBoolean(renderingState.face == RenderingStyle.ITALIC_TF);
    }

    public static FB2LineElement deserializeImpl(DataInputStream in) throws IOException {
        int width = in.readInt();
        int length = in.readInt();
        char[] ch = new char[length];
        for (int i = 0; i < length; i++) {
            ch[i] = in.readChar();
        }
        int textSize = in.readInt();
        boolean bold = in.readBoolean();
        boolean italic = in.readBoolean();
        
        return new FB2TextElement(ch, 0, length, RenderingStyle.getStyle(textSize, bold, italic), width);
    }
}
