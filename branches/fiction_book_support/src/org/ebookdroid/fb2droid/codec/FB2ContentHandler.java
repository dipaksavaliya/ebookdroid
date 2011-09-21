package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;

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

    private boolean cover = false;

    private RenderingStyle crs = new RenderingStyle(RenderingStyle.TEXT_SIZE);

    private final LinkedList<RenderingStyle> renderingStates = new LinkedList<RenderingStyle>();

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
            renderingStates.addFirst(crs);
            crs = new RenderingStyle(crs);
            if (!inSection) {
                crs.textSize = RenderingStyle.MAIN_TITLE_SIZE;
            } else {
                crs.textSize = RenderingStyle.SECTION_TITLE_SIZE;
            }
            crs.jm = JustificationMode.Center;
            final FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, crs.textSize, true));
            document.appendLine(line);
        } else if ("p".equals(qName)) {
            paragraphParsing = true;
            paragraphLines.clear();
            if (crs.jm != JustificationMode.Center) {
                FB2Line.getLastLine(paragraphLines).append(
                        new FB2LineWhiteSpace((int) crs.textSize * 3, crs.textSize, false));
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
            line.append(new FB2LineWhiteSpace(0, crs.textSize, true));
            document.appendLine(line);
        } else if ("strong".equals(qName)) {
            renderingStates.addFirst(crs);
            crs = new RenderingStyle(crs);
            crs.bold = true;
        } else if ("emphasis".equals(qName)) {
            renderingStates.addFirst(crs);
            crs = new RenderingStyle(crs);
            crs.face = RenderingStyle.ITALIC_TF;
        } else if ("epigraph".equals(qName)) {
            renderingStates.addFirst(crs);
            crs = new RenderingStyle(crs);
            crs.jm = JustificationMode.Right;
            crs.face = RenderingStyle.ITALIC_TF;
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
                final int space = (int) crs.getTextPaint().measureText(" ");
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
            line.append(new FB2LineWhiteSpace(0, crs.textSize, true));
            document.appendLine(line);
            crs = renderingStates.removeFirst();
        } else if ("p".equals(qName)) {
            flushParagraphLines();
            paragraphParsing = false;
        } else if ("strong".equals(qName)) {
            crs = renderingStates.removeFirst();
        } else if ("emphasis".equals(qName)) {
            crs = renderingStates.removeFirst();
        } else if ("epigraph".equals(qName)) {
            crs = renderingStates.removeFirst();
        } else if ("coverpage".equals(qName)) {
            cover = false;
        }
    }

    private void flushParagraphLines() {
        if (paragraphLines.isEmpty()) {
            return;
        }
        if (crs.jm == JustificationMode.Justify) {
            final FB2Line l = FB2Line.getLastLine(paragraphLines);
            l.append(new FB2LineWhiteSpace(FB2Page.PAGE_WIDTH - l.getWidth() - 2 * FB2Page.MARGIN_X, crs.textSize,
                    false));
        }
        for (final FB2Line l : paragraphLines) {
            l.applyJustification(crs.jm);
            document.appendLine(l);
        }
        paragraphLines.clear();
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (!documentStarted || documentEnded || !paragraphParsing) {
            return;
        }
        final int space = (int) crs.getTextPaint().measureText(" ");
        final int count = StringUtils.split(ch, start, length, starts, lengths);

        if (count > 0) {
            final char[] dst = new char[length];
            System.arraycopy(ch, start, dst, 0, length);

            for (int i = 0; i < count; i++) {
                final int st = starts[i];
                final int len = lengths[i];
                final FB2TextElement te = new FB2TextElement(dst, st - start, len, crs);
                appendLineElement(space, te);
            }
        }
    }

    private void appendLineElement(final int space, final FB2LineElement te) {
        FB2Line line = FB2Line.getLastLine(paragraphLines);
        if (line.getWidth() + 2 * FB2Page.MARGIN_X + space + te.getWidth() < FB2Page.PAGE_WIDTH) {
            if (line.hasNonWhiteSpaces()) {
                line.append(new FB2LineWhiteSpace(space, crs.textSize, true));
            }
        } else {
            line = new FB2Line();
            paragraphLines.add(line);
        }
        line.append(te);
    }

}
