package org.ebookdroid.opds;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.log.LogContext;

import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
            //set the download URL, a url that points to a file on the internet
            //this is the file to be downloaded
            URL url = new URL(link.uri);

            //create the new connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //set up some things on the connection
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.50 Safari/534.24");            //and connect!
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.connect();

            final int responseCode = urlConnection.getResponseCode();
            System.out.println("Response code = " + responseCode);

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //set the path where we want to save the file
            //in this case, going to save it on the root directory of the
            //sd card.
            File SDCardRoot = Environment.getExternalStorageDirectory();

            final String contentDisposition = urlConnection.getHeaderField("Content-Disposition");

            final String mimeType = urlConnection.getContentType();

            final String location = urlConnection.getHeaderField("Location");
            System.out.println("Location = " + location);

            final String guessFileName = URLUtil.guessFileName(location != null ? location : link.uri, contentDisposition, mimeType);
            //create a new file, specifying the path, and the filename
            //which we want to save the file as.
            File file = new File(SDCardRoot,guessFileName);

            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(file);


            //this is the total size of the file
            int totalSize = urlConnection.getContentLength();
            //variable to store total downloaded bytes
            int downloadedSize = 0;

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //this is where you would do something to report the prgress, like this maybe
//                    updateProgress(downloadedSize, totalSize);

            }
            //close the output stream when done
            fileOutput.close();

            return file;

    //catch some possible errors...
    } catch (MalformedURLException e) {
            e.printStackTrace();
    } catch (IOException e) {
            e.printStackTrace();
    }
    return null;
    }
}
