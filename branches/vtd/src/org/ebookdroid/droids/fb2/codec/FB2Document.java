package org.ebookdroid.droids.fb2.codec;

import static org.ebookdroid.droids.fb2.codec.FB2Page.MARGIN_X;
import static org.ebookdroid.droids.fb2.codec.FB2Page.MARGIN_Y;
import static org.ebookdroid.droids.fb2.codec.FB2Page.PAGE_HEIGHT;
import static org.ebookdroid.droids.fb2.codec.FB2Page.PAGE_WIDTH;

import org.ebookdroid.core.codec.CodecDocument;
import org.ebookdroid.core.codec.CodecPage;
import org.ebookdroid.core.codec.CodecPageInfo;
import org.ebookdroid.core.codec.OutlineLink;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.emdev.common.archives.zip.ZipArchive;
import org.emdev.common.archives.zip.ZipArchiveEntry;
import org.emdev.common.textmarkup.JustificationMode;
import org.emdev.common.textmarkup.MarkupTitle;
import org.emdev.common.textmarkup.TextStyle;
import org.emdev.common.textmarkup.Words;
import org.emdev.common.textmarkup.line.HorizontalRule;
import org.emdev.common.textmarkup.line.Line;
import org.emdev.utils.LengthUtils;
import org.xml.sax.InputSource;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDGenEx;

public class FB2Document implements CodecDocument {

    private static boolean USE_DUCKBILL_PARSER = true;

    private static boolean USE_VTD_EX_PARSER = true;

    private static boolean USE_PULL_PARSER = true;

    private static boolean USE_VTD_PARSER = true;

    private final ArrayList<FB2Page> pages = new ArrayList<FB2Page>();

    private final List<OutlineLink> outline = new ArrayList<OutlineLink>();

    private final ParsedContent content = new ParsedContent();

    private final SAXParserFactory spf = SAXParserFactory.newInstance();

    public FB2Document(final String fileName) {
        Words.clear();
        final long t1 = System.currentTimeMillis();
        content.loadFonts();
        final long t2 = System.currentTimeMillis();
        System.out.println("Fonts preloading: " + (t2 - t1) + " ms");

        if (USE_DUCKBILL_PARSER) {
            parseContent5(fileName);
        } else if (USE_VTD_EX_PARSER) {
            parseContent4(fileName);
        } else if (USE_PULL_PARSER) {
            parseContent3(fileName);
        } else if (USE_VTD_PARSER) {
            parseContent2(fileName);
        } else {
            parseContent(fileName);
        }

        System.out.println("Words=" + Words.words + ", uniques=" + Words.uniques);

        final long t3 = System.currentTimeMillis();

        final List<Line> documentLines = content.createLines(content.getMarkupStream(null), PAGE_WIDTH - 2 * MARGIN_X,
                JustificationMode.Justify);
        createPages(documentLines);

        content.clear();

        System.gc();
        final long t4 = System.currentTimeMillis();
        System.out.println("Markup: " + (t4 - t3) + " ms");
    }

    private void createPages(final List<Line> documentLines) {
        pages.clear();
        if (LengthUtils.isEmpty(documentLines)) {
            return;
        }

        for (final Line line : documentLines) {
            FB2Page lastPage = getLastPage();

            if (lastPage.contentHeight + 2 * MARGIN_Y + line.getTotalHeight() > PAGE_HEIGHT) {
                commitPage();
                lastPage = new FB2Page();
                pages.add(lastPage);
            }

            lastPage.appendLine(line);
            final MarkupTitle title = line.getTitle();
            if (title != null) {
                addTitle(title);
            }

            final List<Line> footnotes = line.getFootNotes();
            if (footnotes != null) {
                final Iterator<Line> iterator = footnotes.iterator();
                if (lastPage.noteLines.size() > 0 && iterator.hasNext()) {
                    // Skip rule for non first note on page
                    iterator.next();
                }
                while (iterator.hasNext()) {
                    final Line l = iterator.next();
                    lastPage = getLastPage();
                    if (lastPage.contentHeight + 2 * MARGIN_Y + l.getTotalHeight() > PAGE_HEIGHT) {
                        commitPage();
                        lastPage = new FB2Page();
                        pages.add(lastPage);
                        final Line ruleLine = new Line(PAGE_WIDTH / 4, JustificationMode.Left);
                        ruleLine.append(new HorizontalRule(PAGE_WIDTH / 4, TextStyle.FOOTNOTE.getFontSize()));
                        ruleLine.applyJustification(JustificationMode.Left);
                        lastPage.appendNoteLine(ruleLine);
                    }
                    lastPage.appendNoteLine(l);
                }
            }
        }
    }

    private void parseContent(final String fileName) {
        final FB2ContentHandler h = new FB2ContentHandler(content);
        final List<Closeable> resources = new ArrayList<Closeable>();

        final long t1 = System.currentTimeMillis();
        try {
            final AtomicLong size = new AtomicLong();
            final InputStream inStream = getInputStream(fileName, size, resources);

            if (inStream != null) {

                final String encoding = getEncoding(inStream);

                final Reader isr = new BufferedReader(new InputStreamReader(inStream, encoding), 32 * 1024);
                resources.add(isr);
                final InputSource is = new InputSource();
                is.setCharacterStream(isr);
                final SAXParser parser = spf.newSAXParser();
                parser.parse(is, h);
            }
        } catch (final StopParsingException e) {
            // do nothing
        } catch (final Exception e) {
            throw new RuntimeException("FB2 document can not be opened: " + e.getMessage(), e);
        } finally {
            for (final Closeable r : resources) {
                try {
                    if (r != null) {
                        r.close();
                    }
                } catch (final IOException e) {
                }
            }
            resources.clear();
            final long t2 = System.currentTimeMillis();
            System.out.println("SAX parser: " + (t2 - t1) + " ms");
        }
    }

    private void parseContent2(final String fileName) {
        final FB2ContentHandler2 h = new FB2ContentHandler2(content);

        try {
            final long t1 = System.currentTimeMillis();
            final VTDGen inStream = parse(fileName);
            final long t2 = System.currentTimeMillis();
            System.out.println("VTD parse: " + (t2 - t1) + " ms");
            h.parse(inStream);
            final long t3 = System.currentTimeMillis();
            System.out.println("VTD scan: " + (t3 - t2) + " ms");
        } catch (final Exception e) {
            throw new RuntimeException("FB2 document can not be opened: " + e.getMessage(), e);
        }
    }

    private void parseContent3(final String fileName) {
        final FB2ContentHandler3 h = new FB2ContentHandler3(content);
        final List<Closeable> resources = new ArrayList<Closeable>();

        final long t1 = System.currentTimeMillis();
        try {
            final AtomicLong size = new AtomicLong();
            final InputStream inStream = getInputStream(fileName, size, resources);

            if (inStream != null) {
                final char[] chars = loadContent(inStream, size, resources);

                final long t2 = System.currentTimeMillis();
                System.out.println("PULL  load: " + (t2 - t1) + " ms");

                final CharArrayReader rr = new CharArrayReader(chars, 0, (int) size.get());
                resources.add(rr);
                h.parse(rr);

                final long t3 = System.currentTimeMillis();
                System.out.println("PULL parse: " + (t3 - t2) + " ms");
            }
        } catch (final Exception e) {
            throw new RuntimeException("FB2 document can not be opened: " + e.getMessage(), e);
        } finally {
            for (final Closeable r : resources) {
                try {
                    if (r != null) {
                        r.close();
                    }
                } catch (final IOException e) {
                }
            }
            resources.clear();
        }
    }

    private void parseContent4(final String fileName) {
        final FB2ContentHandler4 h = new FB2ContentHandler4(content);
        final List<Closeable> resources = new ArrayList<Closeable>();

        final long t1 = System.currentTimeMillis();
        try {
            final AtomicLong size = new AtomicLong();
            final InputStream inStream = getInputStream(fileName, size, resources);

            if (inStream != null) {
                final char[] chars = loadContent(inStream, size, resources);

                final long t2 = System.currentTimeMillis();
                System.out.println("VTDEx  load: " + (t2 - t1) + " ms");

                final VTDGenEx gen = new VTDGenEx();
                gen.setDoc(chars, 0, (int) size.get());
                gen.parse(false);

                final long t3 = System.currentTimeMillis();
                System.out.println("VTDEx parse: " + (t3 - t2) + " ms");

                h.parse(gen);

                final long t4 = System.currentTimeMillis();
                System.out.println("VTDEx  scan: " + (t4 - t3) + " ms");
            }
        } catch (final Exception e) {
            throw new RuntimeException("FB2 document can not be opened: " + e.getMessage(), e);
        } finally {
            for (final Closeable r : resources) {
                try {
                    if (r != null) {
                        r.close();
                    }
                } catch (final IOException e) {
                }
            }
            resources.clear();
        }
    }

    private void parseContent5(final String fileName) {
        final FB2ContentHandler5 h = new FB2ContentHandler5(content);
        final List<Closeable> resources = new ArrayList<Closeable>();

        final long t1 = System.currentTimeMillis();
        try {
            final AtomicLong size = new AtomicLong();
            final InputStream inStream = getInputStream(fileName, size, resources);

            if (inStream != null) {
                final char[] chars = loadContent(inStream, size, resources);

                final long t2 = System.currentTimeMillis();
                System.out.println("DUCK  load: " + (t2 - t1) + " ms");

                h.parse(chars, (int) size.get());

                final long t4 = System.currentTimeMillis();
                System.out.println("DUCK  parse: " + (t4 - t2) + " ms");
            }
        } catch (final Exception e) {
            throw new RuntimeException("FB2 document can not be opened: " + e.getMessage(), e);
        } finally {
            for (final Closeable r : resources) {
                try {
                    if (r != null) {
                        r.close();
                    }
                } catch (final IOException e) {
                }
            }
            resources.clear();
        }
    }

    private char[] loadContent(final InputStream inStream, final AtomicLong size, final List<Closeable> resources)
            throws IOException, UnsupportedEncodingException {
        final String encoding = getEncoding(inStream);
        final Reader isr = new InputStreamReader(inStream, encoding);
        resources.add(isr);

        final char[] chars = new char[(int) size.get()];
        int offset = 0;
        for (int len = chars.length; offset < len;) {
            final int n = isr.read(chars, offset, len);
            if (n == -1) {
                break;
            }
            offset += n;
            len -= n;
        }
        size.set(offset);
        return chars;
    }

    private String getEncoding(final InputStream inStream) throws IOException {
        String encoding = "utf-8";
        final char[] buffer = new char[256];
        boolean found = false;
        int len = 0;
        while (len < 256) {
            final int val = inStream.read();
            if (len == 0 && (val == 0xEF || val == 0xBB || val == 0xBF)) {
                continue;
            }
            buffer[len++] = (char) val;
            if (val == '>') {
                found = true;
                break;
            }
        }
        if (found) {
            final String xmlheader = new String(buffer, 0, len).trim();
            if (xmlheader.startsWith("<?xml") && xmlheader.endsWith("?>")) {
                final int index = xmlheader.indexOf("encoding");
                if (index > 0) {
                    final int startIndex = xmlheader.indexOf('"', index);
                    if (startIndex > 0) {
                        final int endIndex = xmlheader.indexOf('"', startIndex + 1);
                        if (endIndex > 0) {
                            encoding = xmlheader.substring(startIndex + 1, endIndex);
                            System.out.println("XML encoding:" + encoding);
                        }
                    }
                }
            } else {
                throw new RuntimeException("FB2 document can not be opened: " + "Invalid header");
            }
        } else {
            throw new RuntimeException("FB2 document can not be opened: " + "Header not found");
        }
        return encoding;
    }

    private InputStream getInputStream(final String fileName, final AtomicLong size, final List<Closeable> resources)
            throws IOException, FileNotFoundException {
        InputStream inStream = null;

        if (fileName.endsWith("zip")) {
            final ZipArchive zipArchive = new ZipArchive(new File(fileName));

            final Enumeration<ZipArchiveEntry> entries = zipArchive.entries();
            while (entries.hasMoreElements()) {
                final ZipArchiveEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith("fb2")) {
                    size.set(entry.getSize());
                    inStream = entry.open();
                    resources.add(inStream);
                    break;
                }
            }
            resources.add(zipArchive);
        } else {
            final File f = new File(fileName);
            size.set(f.length());
            inStream = new FileInputStream(f);
            resources.add(inStream);
        }
        return inStream;
    }

    private VTDGen parse(final String fileName) throws IOException {
        if (fileName.endsWith("zip")) {
            final ZipArchive zipArchive = new ZipArchive(new File(fileName));
            try {
                final Enumeration<ZipArchiveEntry> entries = zipArchive.entries();
                while (entries.hasMoreElements()) {
                    final ZipArchiveEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith("fb2")) {
                        final VTDGen parser = new VTDGen();
                        if (parser.parseZIPFile(fileName, entry.getName(), false)) {
                            return parser;
                        }
                        throw new IOException("Parsing failed");
                    }
                }
            } finally {
                zipArchive.close();
            }
        } else {
            final VTDGen parser = new VTDGen();
            if (parser.parseFile(fileName, false)) {
                return parser;
            }
            throw new IOException("Parsing failed");
        }
        return null;
    }

    @Override
    public List<OutlineLink> getOutline() {
        return outline;
    }

    @Override
    public CodecPage getPage(final int pageNuber) {
        if (0 <= pageNuber && pageNuber < pages.size()) {
            return pages.get(pageNuber);
        } else {
            return null;
        }
    }

    @Override
    public int getPageCount() {
        return pages.size();
    }

    @Override
    public CodecPageInfo getUnifiedPageInfo() {
        return FB2Page.CPI;
    }

    @Override
    public CodecPageInfo getPageInfo(final int pageNuber) {
        return FB2Page.CPI;
    }

    @Override
    public boolean isRecycled() {
        return false;
    }

    @Override
    public void recycle() {
    }

    void commitPage() {
        getLastPage().commit();
    }

    @Override
    public Bitmap getEmbeddedThumbnail() {
        return content.getCoverImage();
    }

    public void addTitle(final MarkupTitle title) {
        outline.add(new OutlineLink(title.title, "#" + pages.size(), title.level));
    }

    @Override
    public List<? extends RectF> searchText(final int pageNuber, final String pattern) throws DocSearchNotSupported {
        if (LengthUtils.isEmpty(pattern)) {
            return null;
        }

        final FB2Page page = (FB2Page) getPage(pageNuber);
        return (page == null) ? null : page.searchText(pattern);

    }

    public FB2Page getLastPage() {
        if (pages.size() == 0) {
            pages.add(new FB2Page());
        }
        FB2Page fb2Page = pages.get(pages.size() - 1);
        if (fb2Page.committed) {
            fb2Page = new FB2Page();
            pages.add(fb2Page);
        }
        return fb2Page;
    }
}
