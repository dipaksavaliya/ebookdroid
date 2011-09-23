package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.core.OutlineLink;
import org.ebookdroid.core.PageLink;
import org.ebookdroid.core.codec.CodecDocument;
import org.ebookdroid.core.codec.CodecPage;
import org.ebookdroid.core.codec.CodecPageInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

public class FB2Document implements CodecDocument {

    private static final int MARKUP_VERSION = 1;
    
    private final static boolean USE_SERIALIZE = false;
    private final TreeMap<String, FB2Image> images = new TreeMap<String, FB2Image>();
    private final TreeMap<String, ArrayList<FB2Line>> notes = new TreeMap<String, ArrayList<FB2Line>>();

    JustificationMode jm = JustificationMode.Justify;

    private final ArrayList<FB2Page> pages = new ArrayList<FB2Page>();
    private final ArrayList<FB2Line> paragraphLines = new ArrayList<FB2Line>();

    private String cover;

    public FB2Document(final String fileName) {
        final String encoding = getEncoding(fileName);

        File f = new File(fileName + ".pages");

        if (!f.exists() || !deserialize(f)) {
            final SAXParserFactory spf = SAXParserFactory.newInstance();

            final long t2 = System.currentTimeMillis();
            List<FB2MarkupElement> markup = parseContent(spf, fileName, encoding);
            final long t3 = System.currentTimeMillis();
            System.out.println("SAX parser: " + (t3 - t2) + " ms");
            createDocumentMarkup(markup);
            final long t4 = System.currentTimeMillis();
            System.out.println("Markup: " + (t4 - t3) + " ms");
            serializePages(fileName);
            final long t5 = System.currentTimeMillis();
            System.out.println("Serialize: " + (t5 - t4) + " ms");
        }
    }

    private boolean deserialize(File f) {
        if (!USE_SERIALIZE) {
            return false;
        }
        pages.clear();
        final long t1 = System.currentTimeMillis();
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream((new FileInputStream(f)), 1024 * 1024));
            int version = in.readInt();
            if (version != MARKUP_VERSION) {
                return false;
            }
            int pagesCount = in.readInt();
            for (int i = 0; i < pagesCount; i++) {
                pages.add(FB2Page.deserialize(in));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            System.out.println("Deserialize: "+ (System.currentTimeMillis() - t1) + " ms");
        }
        return true;
    }

    private void serializePages(String fileName) {
        if (!USE_SERIALIZE) {
            return;
        }
        String fname = fileName + ".pages";
        try {

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(((new FileOutputStream(fname))), 1024 * 1024));
            out.writeInt(MARKUP_VERSION);
            out.writeInt(pages.size());
            for (FB2Page page : pages) {
                page.serialize(out);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            new File(fname).delete();
        }
    }

    private void createDocumentMarkup(List<FB2MarkupElement> markup) {
        pages.clear();
        jm = JustificationMode.Justify;
        for (FB2MarkupElement me : markup) {
            me.publishToDocument(this);
        }
        commitPage();
        markup.clear();
    }

    private List<FB2MarkupElement> parseContent(final SAXParserFactory spf, final String fileName, final String encoding) {
        FB2ContentHandler h = new FB2ContentHandler(this);
        try {
            final SAXParser parser = spf.newSAXParser();
            final Reader isr = new InputStreamReader(new FileInputStream(fileName), encoding);
            final InputSource is = new InputSource();
            is.setCharacterStream(isr);
            parser.parse(is, h);
        } catch (final StopParsingException e) {
            // do nothing
        } catch (final Exception e) {
            throw new RuntimeException("FB2 document can not be opened: " + e.getMessage(), e);
        }
        return h.markup;
    }

    private String getEncoding(final String fileName) {
        try {
            final FileInputStream fis = new FileInputStream(fileName);
            final byte[] buffer = new byte[100];
            fis.read(buffer);
            final Pattern p = Pattern.compile("encoding=\"(.*?)\"");
            final Matcher matcher = p.matcher(new String(buffer));
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (final IOException e) {
        }
        return "UTF-8";
    }

    void appendLine(final FB2Line line) {
        FB2Page lastPage = FB2Page.getLastPage(pages);

        if (lastPage.getContentHeight() + 2 * FB2Page.MARGIN_Y + line.getTotalHeight() > FB2Page.PAGE_HEIGHT) {
            commitPage();
            lastPage = new FB2Page();
            pages.add(lastPage);
        }
        lastPage.appendLine(line);
        final List<FB2Line> footnotes = line.getFootNotes();
        if (footnotes != null) {
            for (final FB2Line l : footnotes) {
                lastPage = FB2Page.getLastPage(pages);
                if (lastPage.getContentHeight() + 2 * FB2Page.MARGIN_Y + l.getTotalHeight() > FB2Page.PAGE_HEIGHT) {
                    commitPage();
                    lastPage = new FB2Page();
                    pages.add(lastPage);
                }
                lastPage.appendNoteLine(l);
            }
        }
    }

    @Override
    public List<OutlineLink> getOutline() {
        return null;
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
    public CodecPageInfo getPageInfo(final int pageNuber) {
        final CodecPageInfo codecPageInfo = new CodecPageInfo();
        codecPageInfo.setWidth(FB2Page.PAGE_WIDTH);
        codecPageInfo.setHeight(FB2Page.PAGE_HEIGHT);
        return codecPageInfo;
    }

    @Override
    public List<PageLink> getPageLinks(final int pageNuber) {
        return null;
    }

    @Override
    public boolean isRecycled() {
        return false;
    }

    @Override
    public void recycle() {
    }

    void commitPage() {
        final FB2Page lastPage = FB2Page.getLastPage(pages);
        lastPage.commit();
    }

    public void addImage(final String tmpBinaryName, final String encoded) {
        if (tmpBinaryName != null && encoded != null) {
            final FB2Image img = new FB2Image(encoded);
            images.put(tmpBinaryName, img);
        }
    }

    public FB2Image getImage(final String name) {
        FB2Image img = images.get(name);
        if (img == null && name.startsWith("#")) {
            img = images.get(name.substring(1));
        }
        return img;
    }

    public void addNote(final String noteName, final ArrayList<FB2Line> noteLines) {
        if (noteName != null && noteLines != null) {
            notes.put(noteName, noteLines);
        }
    }

    public List<FB2Line> getNote(final String noteName) {
        List<FB2Line> note = notes.get(noteName);
        if (note == null && noteName.startsWith("#")) {
            note = notes.get(noteName.substring(1));
        }
        return note;
    }

    public void setCover(final String value) {
        this.cover = value;
    }

    @Override
    public Bitmap getEmbeddedThumbnail() {
        final FB2Image image = getImage(cover);
        if (image != null) {
            final byte[] data = image.getData();
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    public void publishElement(FB2LineElement le) {
        FB2Line line = FB2Line.getLastLine(paragraphLines);
        int space = (int) RenderingStyle.getTextPaint(line.getHeight()).measureText(" ");
        if (line.getWidth() + 2 * FB2Page.MARGIN_X + space + le.getWidth() < FB2Page.PAGE_WIDTH) {
            if (line.hasNonWhiteSpaces()) {
                line.append(new FB2LineWhiteSpace(space, line.getHeight(), true));
            }
        } else {
            line = new FB2Line();
            paragraphLines.add(line);
        }
        line.append(le);
    }

    public void commitParagraph() {
        if (paragraphLines.isEmpty()) {
            return;
        }
        if (jm == JustificationMode.Justify) {
            final FB2Line l = FB2Line.getLastLine(paragraphLines);
            l.append(new FB2LineWhiteSpace(FB2Page.PAGE_WIDTH - l.getWidth() - 2 * FB2Page.MARGIN_X, l.getHeight(),
                    false));
        }
        for (final FB2Line l : paragraphLines) {
            l.applyJustification(jm);
            appendLine(l);
        }
        paragraphLines.clear();
    }

    public void publishImage(String ref, boolean inline) {
        FB2Image image = getImage(ref);
        if (image != null) {
            if (!inline) {
                final FB2Line line = new FB2Line();
                line.append(image);
                line.applyJustification(JustificationMode.Center);
                appendLine(line);
            } else {
                publishElement(image);
            }
        }
    }

    public void publishNote(String ref) {
        List<FB2Line> note = getNote(ref);
        if (note != null && !paragraphLines.isEmpty()) {
            FB2Line line = FB2Line.getLastLine(paragraphLines);
            line.addNote(note);
        }
    }

}
