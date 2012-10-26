package org.emdev.common.textmarkup.text;

public class TextProvider implements ITextProvider {

    private final long id = SEQ.incrementAndGet();

    private final char[] text;

    public TextProvider(final char... text) {
        this.text = text;
    }

    public TextProvider(final String text) {
        this.text = text.toCharArray();
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public char[] text() {
        return text;
    }

    @Override
    public int size() {
        return text.length;
    }
}
