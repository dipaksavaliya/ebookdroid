package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FB2ContentHandler extends DefaultHandler {

    private static final FB2MarkupParagraphEnd PARAGRAPH_END = new FB2MarkupParagraphEnd();
    private static final FB2MarkupJustification JUSTIFICATION_CENTER = new FB2MarkupJustification(
            JustificationMode.Center);
    private static final FB2MarkupJustification JUSTIFICATION_RIGHT = new FB2MarkupJustification(
            JustificationMode.Right);
    private static final FB2MarkupJustification JUSTIFICATION_LEFT = new FB2MarkupJustification(JustificationMode.Left);
    private static final FB2MarkupJustification JUSTIFICATION_JUSTIFY = new FB2MarkupJustification(
            JustificationMode.Justify);

    private final FB2Document document;

    private static final int[] starts = new int[10000];

    private static final int[] lengths = new int[10000];

    private boolean documentStarted = false, documentEnded = false;

    private boolean inSection = false;

    private boolean paragraphParsing = false;

    private boolean cover = false;

    private String tmpBinaryName = null;
    private boolean parsingNotes = false;
    private boolean parsingNotesP = false;
    private boolean parsingBinary = false;
    private boolean inTitle = false;
    private String noteName = null;
    private int noteId = -1;
    private boolean noteFirstWord = true;
    private ArrayList<FB2Line> noteLines = null;

    private RenderingStyle crs = new RenderingStyle(RenderingStyle.TEXT_SIZE);

    private final LinkedList<RenderingStyle> renderingStates = new LinkedList<RenderingStyle>();

    private static final Pattern notesPattern = Pattern.compile("n([0-9]+)");
    private final StringBuilder tmpBinaryContents = new StringBuilder(64 * 1024);

    public FB2ContentHandler(final FB2Document fb2Document) {
        this.document = fb2Document;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        if ("binary".equalsIgnoreCase(qName)) {
            tmpBinaryName = attributes.getValue("id");
            tmpBinaryContents.setLength(0);
            parsingBinary = true;
        } else if ("body".equals(qName)) {
            if (!documentStarted && !documentEnded) {
                documentStarted = true;
            }
            if (documentEnded && "notes".equals(attributes.getValue("name"))) {
                parsingNotes = true;
                crs = new RenderingStyle(RenderingStyle.FOOTNOTE_SIZE);
            }
        } else if ("section".equals(qName)) {
            if (parsingNotes) {
                noteName = attributes.getValue("id");
                if (noteName != null) {
                    String n = getNoteId();
                    noteLines = new ArrayList<FB2Line>();
                    final FB2Line lastLine = FB2Line.getLastLine(noteLines);
                    lastLine.append(new FB2TextElement(n.toCharArray(), 0, n.length(), crs));
                    lastLine.append(new FB2LineWhiteSpace((int) crs.getTextPaint().measureText(" "), crs.textSize,
                            false));
                }
            } else {
                inSection = true;
            }
        } else if ("title".equals(qName)) {
            if (!parsingNotes) {
                renderingStates.addFirst(crs);
                crs = new RenderingStyle(crs);
                if (!inSection) {
                    crs.textSize = RenderingStyle.MAIN_TITLE_SIZE;
                } else {
                    crs.textSize = RenderingStyle.SECTION_TITLE_SIZE;
                }
                document.appendMarkupElement(JUSTIFICATION_CENTER);
                document.appendMarkupElement(emptyLine(crs.textSize));
            } else {
                inTitle = true;
            }
        } else if ("p".equals(qName)) {
            if (parsingNotes && !inTitle) {
                parsingNotesP = true;
            } else {
                paragraphParsing = true;
                document.appendMarkupElement(new FB2MarkupNewParagraph(crs.textSize));
            }
        } else if ("a".equals(qName)) {
            if (paragraphParsing) {
                if ("note".equalsIgnoreCase(attributes.getValue("type"))) {
                    document.appendMarkupElement(new FB2MarkupNote(attributes.getValue("href")));
                }
            }
        } else if ("empty-line".equals(qName)) {
            document.appendMarkupElement(emptyLine(crs.textSize));
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
            document.appendMarkupElement(JUSTIFICATION_RIGHT);
            crs.face = RenderingStyle.ITALIC_TF;
        } else if ("image".equals(qName)) {
            final String ref = attributes.getValue("href");
            if (cover) {
                document.setCover(ref);
            } else {
                document.appendMarkupElement(new FB2MarkupImageRef(ref, paragraphParsing));
            }
        } else if ("coverpage".equals(qName)) {
            cover = true;
        }
    }

    private FB2MarkupElement emptyLine(int textSize) {
        return new FB2LineWhiteSpace(FB2Page.PAGE_WIDTH - 2 * FB2Page.MARGIN_X, crs.textSize, true);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if ("binary".equalsIgnoreCase(qName)) {
            document.addImage(tmpBinaryName, tmpBinaryContents.toString());
            tmpBinaryName = null;
            tmpBinaryContents.setLength(0);
            parsingBinary = false;
        } else if ("body".equals(qName)) {
            if (documentStarted && !documentEnded) {
                documentEnded = true;
            }
            parsingNotes = false;
        } else if ("section".equals(qName)) {
            if (parsingNotes) {
                document.addNote(noteName, noteLines);
                noteLines = null;
                noteId = -1;
                noteFirstWord = true;
            } else {
                document.appendMarkupElement(new FB2MarkupEndPage());
                inSection = false;
            }
        } else if ("title".equals(qName)) {
            inTitle = false;
            if (!parsingNotes) {
                document.appendMarkupElement(emptyLine(crs.textSize));
                crs = renderingStates.removeFirst();
                document.appendMarkupElement(getJMElement());
            }
        } else if ("p".equals(qName)) {
            if (parsingNotesP) {
                parsingNotesP = false;
                final FB2Line line = FB2Line.getLastLine(noteLines);
                line.append(new FB2LineWhiteSpace(FB2Page.PAGE_WIDTH - line.getWidth() - 2 * FB2Page.MARGIN_X,
                        (int) crs.textSize, false));
                for (final FB2Line l : noteLines) {
                    l.applyJustification(JustificationMode.Justify);
                }
            } else {
                document.appendMarkupElement(PARAGRAPH_END);
                paragraphParsing = false;
            }
        } else if ("strong".equals(qName)) {
            crs = renderingStates.removeFirst();
        } else if ("emphasis".equals(qName)) {
            crs = renderingStates.removeFirst();
        } else if ("epigraph".equals(qName)) {
            crs = renderingStates.removeFirst();
            document.appendMarkupElement(getJMElement());
        } else if ("coverpage".equals(qName)) {
            cover = false;
        }
    }

    private FB2MarkupElement getJMElement() {
        switch (crs.jm) {
            case Center:
                return JUSTIFICATION_CENTER;
            case Justify:
                return JUSTIFICATION_JUSTIFY;
            case Left:
                return JUSTIFICATION_LEFT;
            case Right:
                return JUSTIFICATION_RIGHT;
        }
        return null;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (!(documentStarted && !documentEnded) && !paragraphParsing && !parsingBinary && !parsingNotes) {
            return;
        }
        if (parsingBinary) {
            tmpBinaryContents.append(ch, start, length);
        } else if (parsingNotesP && noteLines != null) {
            final int space = (int) crs.getTextPaint().measureText(" ");
            final int count = StringUtils.split(ch, start, length, starts, lengths);

            if (count > 0) {
                final char[] dst = new char[length];
                System.arraycopy(ch, start, dst, 0, length);

                for (int i = 0; i < count; i++) {
                    final int st = starts[i];
                    final int len = lengths[i];
                    if (noteFirstWord) {
                        noteFirstWord = false;
                        int id = -2;
                        try {
                            id = Integer.parseInt(new String(ch, st, len));
                        } catch (final Exception e) {
                            id = -2;
                        }
                        if (id == noteId) {
                            continue;
                        }
                    }
                    final FB2TextElement te = new FB2TextElement(dst, st - start, len, crs);
                    FB2Line line = FB2Line.getLastLine(noteLines);
                    if (line.getWidth() + 2 * FB2Page.MARGIN_X + space + te.getWidth() < FB2Page.PAGE_WIDTH) {
                        if (line.hasNonWhiteSpaces()) {
                            line.append(new FB2LineWhiteSpace(space, crs.textSize, true));
                        }
                    } else {
                        line = new FB2Line();
                        noteLines.add(line);
                    }
                    line.append(te);
                }
            }
        } else if (documentStarted && !documentEnded) {
            final int count = StringUtils.split(ch, start, length, starts, lengths);

            if (count > 0) {
                final char[] dst = new char[length];
                System.arraycopy(ch, start, dst, 0, length);

                for (int i = 0; i < count; i++) {
                    final int st = starts[i];
                    final int len = lengths[i];
                    document.appendMarkupElement(new FB2TextElement(dst, st - start, len, crs));
                }
            }
        }
    }

    private String getNoteId() {
        final Matcher matcher = notesPattern.matcher(noteName);
        String n = noteName;
        if (matcher.matches()) {
            noteId = Integer.parseInt(matcher.group(1));
            n = "" + noteId + ")";
        }
        return n;
    }

}
