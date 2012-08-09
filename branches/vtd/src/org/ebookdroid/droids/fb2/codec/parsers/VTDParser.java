package org.ebookdroid.droids.fb2.codec.parsers;

import org.ebookdroid.droids.fb2.codec.FB2Tag;
import org.ebookdroid.droids.fb2.codec.handlers.IContentHandler;

import java.util.Arrays;

import org.emdev.common.lang.StrBuilder;

import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class VTDParser {

    public void parse(VTDGen inStream, IContentHandler handler) throws NavException {
        VTDNav nav = inStream.getNav();

        boolean res = nav.toElement(VTDNav.ROOT);
        if (!res) {
            return;
        }

        FB2Tag tag = FB2Tag.UNKNOWN;
        int skipUntilSiblingOrParent = -1;

        int first = nav.getCurrentIndex();
        int last = nav.getTokenCount();

        String[] tagAttrs = new String[2];
        FB2Tag[] tags = new FB2Tag[1024];

        StrBuilder buf = new StrBuilder(1024);

        int maxDepth = -1;
        for (int ci = first; ci < last;) {
            final int depth = nav.getTokenDepth(ci);
            final int type = nav.getTokenType(ci);

            if (skipUntilSiblingOrParent != -1) {
                if (type == VTDNav.TOKEN_STARTING_TAG && depth <= skipUntilSiblingOrParent) {
                    skipUntilSiblingOrParent = -1;
                } else {
                    ci++;
                    continue;
                }
            }

            if (type == VTDNav.TOKEN_STARTING_TAG) {
                String name = nav.toRawString(ci);
                tag = FB2Tag.getTagByName(name);

                for (int d = maxDepth; d >= depth; d--) {
                    if (tags[d] != null) {
                        handler.endElement(tags[d]);
                        tags[d] = null;
                    }
                }
                tags[depth] = tag;
                maxDepth = depth;

                if (tag == FB2Tag.UNKNOWN) {
                    skipUntilSiblingOrParent = depth;
                    ci++;
                    continue;
                }

                if (tag.attributes.length > 0) {
                    ci = fillAtributes(nav, ci + 1, last, tag, tagAttrs);
                    handler.startElement(tag, tagAttrs);
                } else {
                    ci = skipAtributes(nav, ci + 1, last);
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

            if (type == VTDNav.TOKEN_CHARACTER_DATA || type == VTDNav.TOKEN_CDATA_VAL) {
                if (tag.processText && !handler.skipCharacters()) {
                    buf.setLength(0);
                    nav.toString(ci, buf);
                    char[] charArray = buf.getValue();
                    handler.characters(charArray, 0, buf.length(), false);
                }
                ci++;
                continue;
            }

            ci++;
        }

        for (int d = maxDepth; d >= 0; d--) {
            if (tags[d] != null) {
                handler.endElement(tags[d]);
                tags[d] = null;
            }
        }

        inStream.clear();
    }

    private int skipAtributes(VTDNav nav, int first, int last) throws NavException {
        for (int inner = first; inner < last; inner++) {
            int innerType = nav.getTokenType(inner);
            switch (innerType) {
                case VTDNav.TOKEN_ATTR_NAME:
                case VTDNav.TOKEN_ATTR_NS:
                case VTDNav.TOKEN_ATTR_VAL:
                    break;
                default:
                    return inner;
            }
        }
        return last;
    }

    private int fillAtributes(VTDNav nav, int first, int last, FB2Tag tag, String[] tagAttrs) throws NavException {
        for (int i = 0; i < tag.attributes.length; i++) {
            tagAttrs[i] = null;
        }

        for (int inner = first; inner < last; inner++) {
            int innerType = nav.getTokenType(inner);
            switch (innerType) {
                case VTDNav.TOKEN_ATTR_NAME:
                    String[] qName = nav.toString(inner).split(":");
                    String attrName = qName[qName.length-1];
                    int attrIndex = Arrays.binarySearch(tag.attributes, attrName);
                    if (attrIndex >= 0) {
                        String attrValue = nav.toString(inner + 1);
                        tagAttrs[attrIndex] = attrValue;
                    }
                    inner++;
                    break;
                case VTDNav.TOKEN_ATTR_NS:
                case VTDNav.TOKEN_ATTR_VAL:
                    break;
                default:
                    return inner;
            }
        }

        return last;
    }

}
