package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.utils.StringUtils;

import android.graphics.Paint;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FB2ContentHandler extends DefaultHandler {

    private final FB2Document document;

    private final ArrayList<FB2Line> paragraphLines = new ArrayList<FB2Line>();

    private static final int[] starts = new int[10000];

    private static final int[] lengths = new int[10000];

    private boolean documentStarted = false, documentEnded = false;

    private boolean inSection = false;

    private boolean paragraphParsing = false;

    private boolean bold = false;

    private boolean italic = false;

    private boolean cover = false;

    private Paint currentTextPaint = FB2Document.NORMALTEXTPAINT;

    private JustificationMode jm = JustificationMode.Justify;

    public FB2ContentHandler(final FB2Document fb2Document) {
        this.document = fb2Document;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        if ("body".equals(qName)) {
            if (!documentStarted && !documentEnded) {
                documentStarted = true;
            }
        } else if ("section".equals(qName)) {
            inSection = true;
        } else if ("title".equals(qName)) {
            if (!inSection) {
                currentTextPaint = FB2Document.MAINTITLETEXTPAINT;
            } else {
                currentTextPaint = FB2Document.SECTIONTITLETEXTPAINT;
            }
            jm = JustificationMode.Center;
            final FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, (int) currentTextPaint.getTextSize(), true));
            document.appendLine(line);
        } else if ("p".equals(qName)) {
            paragraphParsing = true;
            paragraphLines.clear();
            if (jm != JustificationMode.Center) {
                FB2Line.getLastLine(paragraphLines).append(
                        new FB2LineWhiteSpace((int) currentTextPaint.getTextSize() * 3, (int) currentTextPaint
                                .getTextSize(), false));
            }
        } else if ("a".equals(qName)) {
            if (paragraphParsing) {
                if ("note".equalsIgnoreCase(attributes.getValue("type"))) {
                    final FB2Line line = FB2Line.getLastLine(paragraphLines);
                    final String ref = attributes.getValue("href");
                    line.addNote(document.getNote(ref));
                }
            }
        } else if ("empty-line".equals(qName)) {
            final FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, (int) currentTextPaint.getTextSize(), true));
            document.appendLine(line);
        } else if ("strong".equals(qName)) {
            currentTextPaint.setFakeBoldText(true);
            bold = true;
        } else if ("emphasis".equals(qName)) {
            currentTextPaint.setTypeface(FB2Document.ITALIC_TF);
            italic = true;
        } else if ("epigraph".equals(qName)) {
            jm = JustificationMode.Right;
            currentTextPaint.setTypeface(FB2Document.ITALIC_TF);
            italic = true;
        } else if ("image".equals(qName)) {
            final String ref = attributes.getValue("href");
            if (cover) {
                document.setCover(ref);
            }
            insertImage(ref);
        } else if ("coverpage".equals(qName)) {
            cover = true;
        }
    }

    private void insertImage(final String name) {
        final FB2Image image = document.getImage(name);
        if (image == null) {
            return;
        }
        if (!paragraphParsing) {
            final FB2Line line = new FB2Line();
            line.append(image);
            line.applyJustification(JustificationMode.Center);
            document.appendLine(line);
        } else {
            if (!paragraphLines.isEmpty()) {
                final int space = (int) currentTextPaint.measureText(" ");
                appendLineElement(space, image);
            }
        }

    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if ("body".equals(qName)) {
            if (documentStarted && !documentEnded) {
                documentEnded = true;
                throw new StopParsingException();
            }
        } else if ("section".equals(qName)) {
            document.commitPage();
            inSection = false;
        } else if ("title".equals(qName)) {
            final FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, (int) currentTextPaint.getTextSize(), true));
            document.appendLine(line);
            currentTextPaint = FB2Document.NORMALTEXTPAINT;
            jm = JustificationMode.Justify;
        } else if ("p".equals(qName)) {
            flushParagraphLines();
            paragraphParsing = false;
        } else if ("strong".equals(qName)) {
            currentTextPaint.setFakeBoldText(false);
            bold = false;
        } else if ("emphasis".equals(qName)) {
            currentTextPaint.setTypeface(FB2Document.NORMAL_TF);
            italic = false;
        } else if ("epigraph".equals(qName)) {
            jm = JustificationMode.Justify;
            currentTextPaint.setTypeface(FB2Document.NORMAL_TF);
            italic = false;
        } else if ("coverpage".equals(qName)) {
            cover = false;
        }
    }

    private void flushParagraphLines() {
        if (paragraphLines.isEmpty()) {
            return;
        }
        if (jm == JustificationMode.Justify) {
            final FB2Line l = FB2Line.getLastLine(paragraphLines);
            l.append(new FB2LineWhiteSpace(FB2Page.PAGE_WIDTH - l.getWidth() - 2 * FB2Page.MARGIN_X,
                    (int) currentTextPaint.getTextSize(), false));
        }
        for (final FB2Line l : paragraphLines) {
            l.applyJustification(jm);
            document.appendLine(l);
        }
        paragraphLines.clear();
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (!documentStarted || documentEnded || !paragraphParsing) {
            return;
        }
        final int space = (int) currentTextPaint.measureText(" ");
        final int count = StringUtils.split(ch, start, length, starts, lengths);

        if (count > 0) {
            final char[] dst = new char[length];
            System.arraycopy(ch, start, dst, 0, length);

            for (int i = 0; i < count; i++) {
                final int st = starts[i];
                final int len = lengths[i];
                final FB2TextElement te = new FB2TextElement(dst, st - start, len,
                        (int) currentTextPaint.getTextSize(), (int) currentTextPaint.measureText(ch, st, len), bold,
                        italic);
                appendLineElement(space, te);
            }
        }
    }

    private void appendLineElement(final int space, final FB2LineElement te) {
        FB2Line line = FB2Line.getLastLine(paragraphLines);
        if (line.getWidth() + 2 * FB2Page.MARGIN_X + space + te.getWidth() < FB2Page.PAGE_WIDTH) {
            if (line.hasNonWhiteSpaces()) {
                line.append(new FB2LineWhiteSpace(space, (int) currentTextPaint.getTextSize(), true));
            }
        } else {
            line = new FB2Line();
            paragraphLines.add(line);
        }
        line.append(te);
    }

}
