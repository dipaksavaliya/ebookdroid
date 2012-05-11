package org.ebookdroid.opds;

import java.util.List;

public class Entry {

    public final Entry parent;
    public final String id;
    public final String title;
    public final Content content;
    public final Author author;

    public final Link thumbnail;
    public final Link catalog;
    public final List<Link> downloads;

    public Entry(Entry parent, String id, String title, Content content, Author author, Link catalog, Link thumbnail, List<Link> downloads) {
        this.parent = parent;
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.catalog = catalog;
        this.downloads = downloads;
        this.thumbnail = thumbnail;
    }

    public Entry(Link link) {
        parent = null;
        id = link.uri;
        title = link.uri;
        content = null;
        author = null;
        catalog = link;
        downloads = null;
        thumbnail = null;
    }
}
