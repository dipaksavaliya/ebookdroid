package org.ebookdroid.opds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OPDSContentHandler extends DefaultHandler {

    final Entry parent;

    final List<Entry> entries = new ArrayList<Entry>();

    Link next;

    private boolean inEntry;
    private boolean grabContent;

    private final StringBuilder buf = new StringBuilder();

    private final Map<String, String> values = new HashMap<String, String>();

    private Link entryFeed;
    private Link bookThumbnail;
    private List<Link> bookLinks;

    public OPDSContentHandler(final Entry parent) {
        this.parent = parent;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes a)
            throws SAXException {

        buf.setLength(0);
        if (inEntry) {
            if ("content".equals(qName)) {
                values.put("content@type", a.getValue("type"));
                grabContent = true;
            } else if ("link".equals(qName)) {
                final String ref = a.getValue("href");
                final String rel = a.getValue("rel");
                final String type = a.getValue("type");

                LinkKind kind = LinkKind.valueOf(rel, type);
                switch (kind) {
                    case FEED:
                        entryFeed = new Link(kind, ref, rel, type);
                        break;
                    case BOOK_DOWNLOAD:
                        if (bookLinks == null) {
                            bookLinks = new LinkedList<Link>();
                        }
                        bookLinks.add(new Link(kind, ref, rel, type));
                        break;
                    case BOOK_THUMBNAIL:
                        bookThumbnail = new Link(kind, ref, rel, type);
                        break;
                     default:
                         break;
                }
            } else {
                grabContent = "id".equals(qName) || "title".equals(qName);
            }
        } else {
            if ("entry".equals(qName)) {
                inEntry = true;
                values.clear();
                entryFeed = null;
                bookThumbnail = null;
                bookLinks = null;
            } else if ("link".equals(qName)) {
                final String ref = a.getValue("href");
                final String rel = a.getValue("rel");
                final String type = a.getValue("type");
                LinkKind kind = LinkKind.valueOf(rel, type);
                if (kind == LinkKind.NEXT_FEED) {
                    next = new Link(kind, ref, rel, type);
                }
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (inEntry) {
            if (grabContent) {
                values.put(qName, buf.toString());
            } else if ("entry".equals(qName)) {
                inEntry = false;
                final String contentString = values.get("content");
                final String contentType = values.get("content@type");
                final Content content = contentString != null ? new Content(contentType, contentString) : null;
                final String entryId = values.get("id");
                final String entryTitle = values.get("title");
                final Entry entry = new Entry(parent, entryId, entryTitle, content, null, entryFeed, bookThumbnail, bookLinks);
                entries.add(entry);
                values.clear();
            }
        }
        grabContent = false;
        buf.setLength(0);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (grabContent) {
            buf.append(ch, start, length);
        }
    }
}
