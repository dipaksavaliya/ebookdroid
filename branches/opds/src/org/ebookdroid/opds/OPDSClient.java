package org.ebookdroid.opds;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.log.LogContext;

import android.net.http.AndroidHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;

public class OPDSClient {

    private static final LogContext LCTX = LogContext.ROOT.lctx("OPDS");

    private final AndroidHttpClient client;

    private final SAXParserFactory spf = SAXParserFactory.newInstance();

    public OPDSClient() {
        client = AndroidHttpClient.newInstance(EBookDroidApp.APP_PACKAGE + " " + EBookDroidApp.APP_VERSION);
    }

    public List<Entry> load(final Entry parent) {
        final List<Entry> entries = new ArrayList<Entry>();
        if (parent.catalog != null) {
            final String uri = parent.catalog.uri;
            try {
                HttpGet req = new HttpGet(uri);
                URI reqUri = req.getURI();
                if (reqUri.getHost() == null) {
                    for(Entry p = parent; p != null; p = p.parent) {
                        URI parentURI = new URI(p.catalog.uri);
                        if (parentURI.getHost() != null) {
                            reqUri = new URI(parentURI.getScheme(), parentURI.getHost(), reqUri.getPath(), reqUri.getFragment());
                            req = new HttpGet(reqUri);
                            break;
                        }
                    }
                }

                final HttpResponse resp = client.execute(req);
                final HttpEntity entity = resp.getEntity();

                final OPDSContentHandler h = new OPDSContentHandler(parent);
                final Reader isr = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"), 32 * 1024);
                final InputSource is = new InputSource();
                is.setCharacterStream(isr);
                final SAXParser parser = spf.newSAXParser();
                parser.parse(is, h);

                if (h.next != null) {
                    final Entry nextEntry = new Entry(parent, h.next.uri, "Next records...", null, null, h.next, null,
                            null);
                    h.entries.add(nextEntry);
                }
                return h.entries;
            } catch (final Throwable th) {
                LCTX.e("Error on OPDS catalog access: ", th);
            }
        }
        return entries;
    }

    public File loadFile(final Link link) {
        try {
            final HttpGet req = new HttpGet(link.uri);
            final HttpResponse resp = client.execute(req);
            final HttpEntity entity = resp.getEntity();
            return CacheManager.createTempFile(entity.getContent(), ".opds.thmb");
        } catch (final Throwable th) {
            LCTX.e("Error on OPDS catalog access: ", th);
        }
        return null;
    }
}
