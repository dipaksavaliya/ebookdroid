package org.ebookdroid.opds;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.log.LogContext;

import android.net.http.AndroidHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.emdev.utils.LengthUtils;

public class OPDSClient {

    private static final LogContext LCTX = LogContext.ROOT.lctx("OPDS");

    private final AndroidHttpClient client;

    public OPDSClient() {
        client = AndroidHttpClient.newInstance(EBookDroidApp.APP_PACKAGE + " " + EBookDroidApp.APP_VERSION);
    }

    @Override
    protected void finalize() {
        close();
    }

    public void close() {
        client.close();
    }

    public Feed load(final Feed feed) {
        if (feed.link == null) {
            return feed;
        }
        try {
            final HttpGet req = createRequest(feed);

            final HttpResponse resp = client.execute(req);
            final HttpEntity entity = resp.getEntity();

            final OPDSContentHandler h = new OPDSContentHandler(feed);
            final Header enc = entity.getContentEncoding();
            final String encoding = enc != null ? enc.getValue() : "";

            h.parse(new InputStreamReader(entity.getContent(), LengthUtils.safeString(encoding, "UTF-8")));
        } catch (final Throwable th) {
            LCTX.e("Error on OPDS catalog access: ", th);
        }

        feed.loadedAt = System.currentTimeMillis();
        return feed;
    }

    private HttpGet createRequest(final Feed feed) throws URISyntaxException {
        HttpGet req = new HttpGet(feed.link.uri);
        URI reqUri = req.getURI();
        if (reqUri.getHost() == null) {
            for (Feed p = feed.parent; p != null; p = p.parent) {
                final URI parentURI = new URI(p.link.uri);
                if (parentURI.getHost() != null) {
                    reqUri = new URI(parentURI.getScheme(), parentURI.getHost(), reqUri.getPath(), reqUri.getFragment());
                    req = new HttpGet(reqUri);
                    break;
                }
            }
        }
        return req;
    }

    public File loadFile(final Link link) {
        try {
            final HttpGet req = new HttpGet(link.uri);
            final HttpResponse resp = client.execute(req);
            final HttpEntity entity = resp.getEntity();
            return CacheManager.createTempFile(entity.getContent(), ".opds");
        } catch (final Throwable th) {
            LCTX.e("Error on OPDS catalog access: ", th);
        }
        return null;
    }

    public File download(final Link link) {
        try {
            final HttpGet req = new HttpGet(link.uri);
            final HttpResponse resp = client.execute(req);
            final HttpEntity entity = resp.getEntity();

            final String name = new File("" + req.getURI().toString()).getName();
            final File file = new File("/sdcard", name);

            entity.writeTo(new FileOutputStream(file));

            return file;
        } catch (final Throwable th) {
            LCTX.e("Error on downloading book: ", th);
        }
        return null;
    }
}
