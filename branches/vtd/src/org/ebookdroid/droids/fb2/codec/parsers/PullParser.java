package org.ebookdroid.droids.fb2.codec.parsers;

import org.ebookdroid.droids.fb2.codec.FB2Tag;
import org.ebookdroid.droids.fb2.codec.handlers.IContentHandler;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class PullParser {

    public void parse(final Reader r, final IContentHandler handler) throws XmlPullParserException, IOException {

        final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
        final XmlPullParser p = f.newPullParser();
        p.setInput(r);

        FB2Tag tag = FB2Tag.unknownTag;
        int skipUntilSiblingOrParent = -1;

        final String[] tagAttrs = new String[2];
        final FB2Tag[] tags = new FB2Tag[1024];
        final int[] textRegion = new int[2];

        int maxDepth = -1;
        for (int type = p.next(); type != XmlPullParser.END_DOCUMENT; type = p.nextToken()) {
            final int depth = p.getDepth() - 1;

            if (skipUntilSiblingOrParent != -1) {
                if (type == XmlPullParser.START_TAG && depth <= skipUntilSiblingOrParent) {
                    skipUntilSiblingOrParent = -1;
                } else {
                    continue;
                }
            }

            if (type == XmlPullParser.START_TAG) {
                final String name = p.getName();
                tag = FB2Tag.getTagByName(name);

                for (int d = maxDepth; d >= depth; d--) {
                    if (tags[d] != null) {
                        handler.endElement(tags[d]);
                        tags[d] = null;
                    }
                }

                tags[depth] = tag;
                maxDepth = depth;

                if (tag.tag == FB2Tag.UNKNOWN) {
                    skipUntilSiblingOrParent = depth;
                    continue;
                }

                if (tag.attributes.length > 0) {
                    fillAtributes(p, tag, tagAttrs);
                    handler.startElement(tag, tagAttrs);
                } else {
                    handler.startElement(tag);
                }
                continue;
            }

            for (int d = maxDepth; d > depth; d--) {
                if (tags[d] != null) {
                    handler.endElement(tags[d]);
                    tags[d] = null;
                }
            }
            maxDepth = depth;

            if (type == XmlPullParser.TEXT || type == XmlPullParser.CDSECT || type == XmlPullParser.ENTITY_REF) {
                if (tag.processText && !handler.skipCharacters()) {
                    final char[] text = p.getTextCharacters(textRegion);
                    handler.characters(text, textRegion[0], textRegion[1], false);
                }
                continue;
            }
        }

        for (int d = maxDepth; d >= 0; d--) {
            if (tags[d] != null) {
                handler.endElement(tags[d]);
                tags[d] = null;
            }
        }
    }

    private void fillAtributes(final XmlPullParser p, final FB2Tag tag, final String[] tagAttrs) {
        for (int i = 0; i < tag.attributes.length; i++) {
            tagAttrs[i] = null;
        }

        for (int i = 0, n = p.getAttributeCount(); i < n; i++) {
            final String[] qName = p.getAttributeName(i).split(":");
            final String attrName = qName[qName.length - 1];
            final int attrIndex = Arrays.binarySearch(tag.attributes, attrName);
            if (attrIndex >= 0) {
                final String attrValue = p.getAttributeValue(i);
                tagAttrs[attrIndex] = attrValue;
            }
        }
    }

}
