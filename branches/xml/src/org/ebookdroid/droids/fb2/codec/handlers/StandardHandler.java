package org.ebookdroid.droids.fb2.codec.handlers;

import org.ebookdroid.droids.fb2.codec.FB2Page;
import org.ebookdroid.droids.fb2.codec.ParsedContent;
import org.ebookdroid.droids.fb2.codec.tags.FB2Tag;

import org.emdev.common.textmarkup.JustificationMode;
import org.emdev.common.textmarkup.MarkupStream;
import org.emdev.common.textmarkup.MarkupTable;
import org.emdev.common.textmarkup.MarkupTable.Cell;
import org.emdev.common.textmarkup.MarkupTag;
import org.emdev.common.textmarkup.RenderingStyle;
import org.emdev.common.textmarkup.RenderingStyle.Script;
import org.emdev.common.textmarkup.TextStyle;
import org.emdev.common.textmarkup.text.ITextProvider;
import org.emdev.common.textmarkup.text.TextProvider;
import org.emdev.common.xml.tags.XmlTag;
import org.emdev.utils.StringUtils;

public class StandardHandler extends BaseHandler {

    protected boolean documentStarted = false, documentEnded = false;

    protected boolean paragraphParsing = false;

    protected int ulLevel = 0;

    protected boolean cover = false;

    protected String tmpBinaryName = null;
    protected boolean parsingNotes = false;
    protected boolean parsingBinary = false;
    protected boolean inTitle = false;
    protected boolean inCite = false;
    protected boolean noteFirstWord = true;
    protected boolean parseNotesInParagraphs = false;

    protected boolean spaceNeeded = true;

    protected final StringBuilder tmpBinaryContents = new StringBuilder(64 * 1024);
    protected final StringBuilder title = new StringBuilder();

    protected int sectionLevel = -1;

    protected boolean skipContent = true;

    protected MarkupTable currentTable;

    private static final ITextProvider BULLET = new TextProvider("\u2022 ");

    public StandardHandler(final ParsedContent content) {
        this(content, true);
    }

    public StandardHandler(final ParsedContent content, final boolean useUniqueTextElements) {
        super(content);
    }

    @Override
    public boolean parseAttributes(final XmlTag tag) {
        if (tag == FB2Tag.P.tag) {
            return parseNotesInParagraphs && tag.attributes.length > 0;
        }
        return tag.attributes.length > 0;
    }

    @Override
    public void startElement(final XmlTag tag, final String... attributes) {
        spaceNeeded = true;
        final MarkupStream markupStream = parsedContent.getMarkupStream(currentStream);

        switch (tag.tag) {
            case P:
                paragraphParsing = true;
                if (!parsingNotes) {
                    if (!inTitle) {
                        markupStream.fixedWhiteSpace(crs.paint.pOffset);
                    } else {
                        if (title.length() > 0) {
                            title.append(" ");
                        }
                    }
                } else if (parseNotesInParagraphs) {
                    currentStream = attributes[0];
                    if (currentStream != null) {
                        final String n = getNoteId(currentStream, true);
                        parsedContent.getMarkupStream(currentStream).text(parsedContent, new TextProvider(n), crs);
                        parsedContent.getMarkupStream(currentStream).fixedWhiteSpace(crs.paint.fixedSpace);
                        noteFirstWord = true;
                    }
                }
                break;
            case UL:
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                ulLevel++;
                break;
            case LI:
                paragraphParsing = true;
                markupStream.extraSpace(((int) (crs.paint.pOffset.width * ulLevel)));
                markupStream.text(parsedContent, BULLET, crs);
                markupStream.noSpace();
                break;
            case V:
                paragraphParsing = true;
                markupStream.fixedWhiteSpace(crs.paint.pOffset);
                markupStream.fixedWhiteSpace(crs.paint.vOffset);
                break;
            case BINARY:
                tmpBinaryName = attributes[0];
                tmpBinaryContents.setLength(0);
                parsingBinary = true;
                break;
            case BODY:
                if (!documentStarted && !documentEnded) {
                    documentStarted = true;
                    skipContent = false;
                    currentStream = null;
                }
                if ("notes".equals(attributes[0])) {
                    if (documentStarted) {
                        documentEnded = true;
                        parsedContent.getMarkupStream(null).endDocument();
                    }
                    parsingNotes = true;
                    crs = RenderingStyle.get(parsedContent, TextStyle.FOOTNOTE);
                }
                if ("footnotes".equals(attributes[0])) {
                    if (documentStarted) {
                        documentEnded = true;
                        parsedContent.getMarkupStream(null).endDocument();
                    }
                    parsingNotes = true;
                    parseNotesInParagraphs = true;
                    crs = RenderingStyle.get(parsedContent, TextStyle.FOOTNOTE);
                }
                break;
            case SECTION:
                if (parsingNotes) {
                    if (!parseNotesInParagraphs) {
                        currentStream = attributes[0];
                        if (currentStream != null) {
                            final String n = getNoteId(currentStream, true);
                            parsedContent.getMarkupStream(currentStream).text(parsedContent, new TextProvider(n), crs);
                            parsedContent.getMarkupStream(currentStream).fixedWhiteSpace(crs.paint.fixedSpace);
                            noteFirstWord = true;
                        }
                    }
                } else {
                    sectionLevel++;
                }
                break;
            case TITLE:
                if (!parsingNotes) {
                    setTitleStyle(!isInSection() ? TextStyle.MAIN_TITLE : TextStyle.SECTION_TITLE);
                    markupStream.justify(crs.jm);
                    markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                    markupStream.endParagraph();
                    title.setLength(0);
                } else {
                    skipContent = true;
                }
                inTitle = true;
                break;
            case CITE:
                inCite = true;
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                markupStream.extraSpace(FB2Page.PAGE_WIDTH / 6);
                break;
            case SUBTITLE:
                paragraphParsing = true;
                markupStream.justify(setSubtitleStyle().jm);
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                break;
            case TEXT_AUTHOR:
                paragraphParsing = true;
                markupStream.justify(setTextAuthorStyle(inCite).jm);
                markupStream.fixedWhiteSpace(crs.paint.pOffset);
                break;
            case DATE:
                if (documentStarted && !documentEnded || parsingNotes) {
                    paragraphParsing = true;
                    markupStream.justify(setTextAuthorStyle(inCite).jm);
                    markupStream.fixedWhiteSpace(crs.paint.pOffset);
                }
                break;
            case A:
                if (paragraphParsing) {
                    if ("note".equalsIgnoreCase(attributes[1])) {
                        final String note = attributes[0];
                        final String prettyNote = " " + getNoteId(note, false);
                        final int ref = parsedContent.getNoteRef(note);
                        markupStream.note(ref);
                        markupStream.noSpace();
                        markupStream.text(parsedContent, new TextProvider(prettyNote), RenderingStyle.get(crs, Script.SUPER));
                        skipContent = true;
                    }
                }
                break;
            case EMPTY_LINE:
            case BR:
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                break;
            case POEM:
                markupStream.endParagraph();
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.justify(setPoemStyle().jm);
                break;
            case STRONG:
                setBoldStyle();
                break;
            case SUP:
                setSupStyle();
                spaceNeeded = false;
                break;
            case SUB:
                setSubStyle();
                spaceNeeded = false;
                break;
            case STRIKETHROUGH:
                setStrikeThrough();
                break;
            case EMPHASIS:
                setEmphasisStyle();
                break;
            case EPIGRAPH:
                markupStream.endParagraph();
                markupStream.justify(setEpigraphStyle().jm);
                break;
            case IMAGE:
                final String name = attributes[0];
                if (cover) {
                    parsedContent.setCover(name);
                } else {
                    if (!paragraphParsing) {
                        markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                        markupStream.endParagraph();
                    }
                    int ref = parsedContent.getImageRef(name);
                    markupStream.imageRef(ref, paragraphParsing);
                    if (!paragraphParsing) {
                        markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                        markupStream.endParagraph();
                    }
                }
                break;
            case COVERPAGE:
                cover = true;
                break;
            case ANNOTATION:
                skipContent = false;
                break;
            case TABLE:
                currentTable = new MarkupTable();
                // markupStream.add(currentTable);
                break;
            case TR:
                if (currentTable != null) {
                    currentTable.addRow();
                }
                break;
            case TD:
            case TH:
                if (currentTable != null) {
                    final int rowCount = currentTable.getRowCount();
                    final Cell c = currentTable.new Cell();
                    currentTable.addCol(c);
                    final String streamId = currentTable.uuid + ":" + rowCount + ":"
                            + currentTable.getColCount(rowCount - 1);
                    c.stream = streamId;
                    c.hasBackground = tag == FB2Tag.TH.tag;
                    final String align = attributes[0];
                    if ("right".equals(align)) {
                        c.align = JustificationMode.Right;
                    }
                    if ("center".equals(align)) {
                        c.align = JustificationMode.Center;
                    }
                    paragraphParsing = true;
                    oldStream = currentStream;
                    currentStream = streamId;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void endElement(final XmlTag tag) {
        spaceNeeded = true;
        final MarkupStream markupStream = parsedContent.getMarkupStream(currentStream);
        switch (tag.tag) {
            case LI:
                markupStream.extraSpace(-(int) (crs.paint.pOffset.width * ulLevel));
            case P:
            case V:
                if (!skipContent) {
                    markupStream.endParagraph();
                }
                paragraphParsing = false;
                break;
            case UL:
                ulLevel--;
                markupStream.fixedWhiteSpace(crs.paint.emptyLine);
                markupStream.endParagraph();
                break;
            case BINARY:
                if (tmpBinaryContents.length() > 0) {
                    parsedContent.addImage(tmpBinaryName, tmpBinaryContents.toString());
                    tmpBinaryName = null;
                    tmpBinaryContents.setLength(0);
                }
                parsingBinary = false;
                break;
            case BODY:
                parsingNotes = false;
                currentStream = null;
                parseNotesInParagraphs = false;
                break;
            case SECTION:
                if (parsingNotes) {
                    noteId = -1;
                } else {
                    if (isInSection()) {
                        markupStream.endPage();
                        sectionLevel--;
                    }
                }
                break;
            case TITLE:
                inTitle = false;
                skipContent = false;
                if (!parsingNotes) {
                    markupStream.title(title.toString(), sectionLevel);
                    markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                    markupStream.endParagraph();
                    markupStream.justify(setPrevStyle().jm);
                }
                break;
            case CITE:
                inCite = false;
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                markupStream.extraSpace(-FB2Page.PAGE_WIDTH / 6);
                break;
            case SUBTITLE:
                markupStream.endParagraph();
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                markupStream.justify(setPrevStyle().jm);
                paragraphParsing = false;
                break;
            case TEXT_AUTHOR:
            case DATE:
                markupStream.endParagraph();
                markupStream.justify(setPrevStyle().jm);
                paragraphParsing = false;
                break;
            case STANZA:
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                break;
            case POEM:
                markupStream.fixedWhiteSpace(emptyLine(crs.textSize));
                markupStream.endParagraph();
                markupStream.justify(setPrevStyle().jm);
                break;
            case STRONG:
                setPrevStyle();
                spaceNeeded = false;
                break;
            case STRIKETHROUGH:
                setPrevStyle();
                break;
            case SUP:
            case SUB:
                setPrevStyle();
                if (markupStream.lastTag() == MarkupTag.MarkupNoSpace) {
                    markupStream.noSpaceOff();
                }
                break;
            case EMPHASIS:
                setPrevStyle();
                spaceNeeded = false;
                break;
            case EPIGRAPH:
                markupStream.justify(setPrevStyle().jm);
                break;
            case COVERPAGE:
                cover = false;
                break;
            case A:
                if (paragraphParsing) {
                    skipContent = false;
                }
                break;
            case ANNOTATION:
                skipContent = true;
                parsedContent.getMarkupStream(null).endPage();
                break;
            case TABLE:
                currentTable = null;
                break;
            case TD:
            case TH:
                paragraphParsing = false;
                currentStream = oldStream;
                break;
            default:
                break;
        }
    }

    protected boolean isInSection() {
        return sectionLevel >= 0;
    }

    @Override
    public boolean skipCharacters() {
        return skipContent
                || (!(documentStarted && !documentEnded) && !paragraphParsing && !parsingBinary && !parsingNotes);
    }

    @Override
    public void characters(final ITextProvider p, final int start, final int length) {
        if (parsingBinary) {
            tmpBinaryContents.append(p.text(), start, length);
        } else {
            processText(p, start, length);
        }
    }

    protected void processText(final ITextProvider ch, final int start, final int length) {
        char[] text = ch.text();
        if (inTitle) {
            title.append(text, start, length);
        }
        final int count = StringUtils.split(text, start, length, starts, lengths);

        if (count > 0) {
            final MarkupStream markupStream = parsedContent.getMarkupStream(currentStream);
            char c = text[start];
            if (!spaceNeeded && !Character.isWhitespace(c)) {
                markupStream.noSpace();
            }
            spaceNeeded = true;

            for (int i = 0; i < count; i++) {
                final int st = starts[i];
                final int len = lengths[i];
                if (parsingNotes) {
                    if (noteFirstWord) {
                        noteFirstWord = false;
                        int id = getNoteId(text, st, len);
                        if (id == noteId) {
                            continue;
                        }
                    }
                }
                markupStream.text(parsedContent, ch, st, len, crs);
                if (crs.script != Script.NONE) {
                    markupStream.noSpace();
                }
            }
            if (Character.isWhitespace(text[start + length - 1])) {
                markupStream.noSpace();
                markupStream.whiteSpace(crs.paint.space);
            }
            spaceNeeded = false;
        }
    }
}
