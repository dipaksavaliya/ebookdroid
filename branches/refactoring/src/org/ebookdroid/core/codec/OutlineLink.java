package org.ebookdroid.core.codec;


public class OutlineLink implements CharSequence {

    public final String title;
    public final String link;
    public final int level;

    public OutlineLink(final String title, final String link, final int level) {
        this.title = title;
        this.level = level;
        this.link = link;
    }

    @Override
    public char charAt(final int index) {
        return title.charAt(index);
    }

    @Override
    public int length() {
        return title.length();
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return title.subSequence(start, end);
    }

    public int getPageIndex() {
        if (link != null && link.startsWith("#")) {
            try {
                return Integer.parseInt(link.substring(1).replace(" ", ""));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public String getUrl() {
        if (link != null && link.startsWith("http:")) {
            return link;
        }
        return null;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getLink() {
        return link;
    }

}
