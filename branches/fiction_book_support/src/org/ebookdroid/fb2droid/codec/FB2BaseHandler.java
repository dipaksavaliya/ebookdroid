package org.ebookdroid.fb2droid.codec;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FB2BaseHandler extends DefaultHandler {

    protected final FB2Document document;

    protected static final int[] starts = new int[10000];

    protected static final int[] lengths = new int[10000];

    protected RenderingStyle crs = new RenderingStyle(RenderingStyle.TEXT_SIZE);

    private final LinkedList<RenderingStyle> renderingStates = new LinkedList<RenderingStyle>();

    protected final Set<FB2Tag> tags = EnumSet.noneOf(FB2Tag.class);

    public FB2BaseHandler(final FB2Document fb2Document) {
        this.document = fb2Document;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {

        final FB2Tag tag = FB2Tag.getTag(qName);
        if (tag == null) {
            return;
        }
        if (onStartTag(tag, attributes)) {
            tags.add(tag);
        }
    }

    protected boolean onStartTag(final FB2Tag tag, final Attributes attributes) throws SAXException {
        return true;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {

        final FB2Tag tag = FB2Tag.getTag(qName);
        if (tag == null) {
            return;
        }
        if (tags.remove(tag)) {
            onEndTag(tag);
        }
    }

    protected void onEndTag(final FB2Tag tag) throws SAXException {
    }

    protected final RenderingStyle setPrevStyle() {
        crs = renderingStates.removeFirst();
        return crs;
    }

    protected final RenderingStyle setTitleStyle(int textSize) {
        renderingStates.addFirst(crs);
        crs = new RenderingStyle(crs, textSize, JustificationMode.Center);
        return crs;
    }

    protected final RenderingStyle setEpigraphStyle() {
        renderingStates.addFirst(crs);
        crs = new RenderingStyle(crs, JustificationMode.Right, RenderingStyle.ITALIC_TF);
        return crs;
    }

    protected final RenderingStyle setBoldStyle() {
        renderingStates.addFirst(crs);
        crs = new RenderingStyle(crs, true);
        return crs;
    }

    protected final RenderingStyle setEmphasisStyle() {
        renderingStates.addFirst(crs);
        crs = new RenderingStyle(crs, RenderingStyle.ITALIC_TF);
        return crs;
    }

    protected final RenderingStyle setSubtitleStyle() {
        renderingStates.addFirst(crs);
        crs = new RenderingStyle(crs, RenderingStyle.SUBTITLE_SIZE, JustificationMode.Center, true,
                RenderingStyle.NORMAL_TF);
        return crs;
    }

    protected final RenderingStyle setTextAuthorStyle() {
        renderingStates.addFirst(crs);
        crs = new RenderingStyle(crs, RenderingStyle.TEXT_SIZE, JustificationMode.Right, false,
                tags.contains(FB2Tag.CITE) ? RenderingStyle.ITALIC_TF : RenderingStyle.NORMAL_TF);
        return crs;
    }
}
