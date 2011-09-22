package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.utils.StringUtils;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FB2ContentHandler2 extends FB2BaseHandler {

    private final ArrayList<FB2Line> paragraphLines = new ArrayList<FB2Line>();

    public FB2ContentHandler2(final FB2Document fb2Document) {
        super(fb2Document);
    }

    @Override
    protected boolean onStartTag(FB2Tag tag, Attributes attributes) {
        switch (tag) {
            case BODY:
                return true;
            case SECTION:
                return true;
            case TITLE:
                setTitleStyle(tags.contains(FB2Tag.SECTION) ? RenderingStyle.SECTION_TITLE_SIZE
                        : RenderingStyle.MAIN_TITLE_SIZE);
                final FB2Line line = new FB2Line();
                line.append(new FB2LineWhiteSpace(0, crs.textSize, true));
                document.appendLine(line);
                return true;
            case PARAGRAPH:
                paragraphLines.clear();
                if (crs.jm != JustificationMode.Center) {
                    FB2Line.getLastLine(paragraphLines).append(
                            new FB2LineWhiteSpace((int) crs.textSize * 3, crs.textSize, false));
                }
                return true;
            case EMPTY_LINE:
                final FB2Line emptyline = new FB2Line();
                emptyline.append(new FB2LineWhiteSpace(0, crs.textSize, true));
                document.appendLine(emptyline);
                return false;
            case AHREF:
                if (tags.contains(FB2Tag.PARAGRAPH) && "note".equalsIgnoreCase(attributes.getValue("type"))) {
                    final FB2Line lastLine = FB2Line.getLastLine(paragraphLines);
                    final String ref = attributes.getValue("href");
                    lastLine.addNote(document.getNote(ref));
                }
                return false;
            case COVERPAGE:
                return true;
            case IMAGE:
                final String ref = attributes.getValue("href");
                if (tags.contains(FB2Tag.COVERPAGE)) {
                    document.setCover(ref);
                }
                if (tags.contains(FB2Tag.BODY)) {
                    insertImage(ref);
                }
                return false;
            case EPIGRAPH:
                setEpigraphStyle();
                return true;
            case STRONG:
                setBoldStyle();
                return true;
            case EMPHASIS:
                setEmphasisStyle();
                return true;
        }
        return false;
    }

    @Override
    protected void onEndTag(FB2Tag tag) throws SAXException {
        switch (tag) {
            case BODY:
                throw new StopParsingException();
            case SECTION:
                document.commitPage();
                return;
            case TITLE:
                final FB2Line line = new FB2Line();
                line.append(new FB2LineWhiteSpace(0, crs.textSize, true));
                document.appendLine(line);
                setPrevStyle();
                return;
            case PARAGRAPH:
                flushParagraphLines();
                return;
            case EPIGRAPH:
            case STRONG:
            case EMPHASIS:
                setPrevStyle();
                return;
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (!tags.contains(FB2Tag.PARAGRAPH) || !tags.contains(FB2Tag.BODY)) {
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

    private void insertImage(final String name) {
        final FB2Image image = document.getImage(name);
        if (image == null) {
            return;
        }
        if (!tags.contains(FB2Tag.PARAGRAPH)) {
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
