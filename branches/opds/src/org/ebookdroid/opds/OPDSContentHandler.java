package org.ebookdroid.opds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OPDSContentHandler extends DefaultHandler {

    private final SAXParserFactory spf = SAXParserFactory.newInstance();

    final Feed feed;

    private boolean inEntry;
    private boolean grabContent;

    private final StringBuilder buf = new StringBuilder();
    private final Map<String, String> values = new HashMap<String, String>();

    private Link feedLink;
    private Link bookThumbnail;
    private List<Link> bookLinks;

    public OPDSContentHandler(final Feed feed) {
        this.feed = feed;
    }

    public void parse(InputStreamReader inputStreamReader) throws ParserConfigurationException, SAXException,
            IOException {
        final Reader isr = new BufferedReader(inputStreamReader, 32 * 1024);
        final InputSource is = new InputSource();
        is.setCharacterStream(isr);
        final SAXParser parser = spf.newSAXParser();
        parser.parse(is, this);
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
                        feedLink = new Link(kind, ref, rel, type);
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
                feedLink = null;
                bookThumbnail = null;
                bookLinks = null;
            } else if ("link".equals(qName)) {
                final String ref = a.getValue("href");
                final String rel = a.getValue("rel");
                final String type = a.getValue("type");
                LinkKind kind = LinkKind.valueOf(rel, type);
                if (kind == LinkKind.NEXT_FEED) {
                    feed.next = new Feed(feed.parent, ref, feed.title, feed.content);
                    feed.next.link = new Link(kind, ref, rel, type);
                    feed.next.next = null;
                    feed.next.prev = feed;
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
                if (feedLink != null) {
                    Feed child = new Feed(feed, entryId, entryTitle, content);
                    child.link = feedLink;
                    child.next = null;
                    child.prev = null;
                    feed.children.add(child);
                } else {
                    Book book = new Book(feed, entryId, entryTitle, content);
                    book.author = null;
                    book.downloads = bookLinks;
                    book.thumbnail = bookThumbnail;
                    feed.books.add(book);
                }
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
