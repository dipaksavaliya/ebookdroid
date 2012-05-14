package org.ebookdroid.opds;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.R;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.log.LogContext;

import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.emdev.ui.progress.IProgressIndicator;
import org.emdev.ui.progress.UIFileCopying;
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

    public File download(final Link link, final IProgressIndicator progress) {
        try {
            final AtomicReference<String> uri = new AtomicReference<String>(link.uri);
            final HttpResponse resp = connect(uri);
            final StatusLine statusLine = resp.getStatusLine();
            final int statusCode = statusLine.getStatusCode();

            if (statusCode != 200) {
                LCTX.e("Content cannot be retrived: " + statusLine);
                return null;
            }

            final HttpEntity entity = resp.getEntity();
            final String contentDisposition = getHeaderValue(resp, "Content-Disposition");
            final String mimeType = getHeaderValue(entity.getContentType());
            final long contentLength = entity.getContentLength();

            LCTX.d("Content-Disposition: " + contentDisposition);
            LCTX.d("Content-Type: " + mimeType);
            LCTX.d("Content-Length: " + contentLength);

            final String guessFileName = URLUtil.guessFileName(uri.get(), contentDisposition, mimeType);

            LCTX.d("File name: " + guessFileName);

            // create a new file, specifying the path, and the filename which we want to save the file as.
            final File SDCardRoot = Environment.getExternalStorageDirectory();
            final File file = new File(SDCardRoot, guessFileName);

            // this will be used to write the downloaded data into the file we created
            final FileOutputStream fileOutput = new FileOutputStream(file);
            final UIFileCopying worker = new UIFileCopying(R.string.opds_loading, 64 * 1024, progress);
            try {
                worker.copy(contentLength, entity.getContent(), fileOutput);
            } catch (final ClosedByInterruptException ex) {
                try {
                    file.delete();
                } catch (final Exception ex1) {
                }
            }

            return file;
        } catch (final Throwable th) {
            LCTX.e("Error on downloading book: ", th);
        }
        return null;
    }

    protected HttpResponse connect(final AtomicReference<String> uri) throws IOException {
        LCTX.d("URL to download: " + uri);
        HttpGet req = new HttpGet(uri.get());
        HttpResponse resp = client.execute(req);
        StatusLine statusLine = resp.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        LCTX.d("Status: " + statusLine);
        int redirectCount = 5;
        while (redirectCount > 0 && (statusCode == 301 || statusCode == 302)) {
            redirectCount--;

            final String location = getHeaderValue(resp, "Location");
            uri.set(location);
            LCTX.d("Location: " + uri);

            if (LengthUtils.isEmpty(location)) {
                break;
            }
            req = new HttpGet(location);
            resp = client.execute(req);
            statusLine = resp.getStatusLine();
            statusCode = statusLine.getStatusCode();
            LCTX.d("Status: " + statusLine);
        }

        return resp;
    }

    protected static String getHeaderValue(final HttpResponse resp, final String name) {
        final Header header = resp.getFirstHeader(name);
        return header != null ? header.getValue() : null;
    }

    protected static String getHeaderValue(final Header header) {
        return header != null ? header.getValue() : null;
    }

}
