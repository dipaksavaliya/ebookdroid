package org.ebookdroid.droids.fb2.codec;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.emdev.common.lang.StrBuilder;
import org.emdev.common.textmarkup.JustificationMode;
import org.emdev.common.textmarkup.MarkupElement;
import org.emdev.common.textmarkup.MarkupEndDocument;
import org.emdev.common.textmarkup.MarkupEndPage;
import org.emdev.common.textmarkup.MarkupImageRef;
import org.emdev.common.textmarkup.MarkupNoSpace;
import org.emdev.common.textmarkup.MarkupNote;
import org.emdev.common.textmarkup.MarkupParagraphEnd;
import org.emdev.common.textmarkup.MarkupTable;
import org.emdev.common.textmarkup.MarkupTable.Cell;
import org.emdev.common.textmarkup.MarkupTitle;
import org.emdev.common.textmarkup.RenderingStyle;
import org.emdev.common.textmarkup.RenderingStyle.Script;
import org.emdev.common.textmarkup.TextStyle;
import org.emdev.common.textmarkup.Words;
import org.emdev.common.textmarkup.line.TextElement;
import org.emdev.utils.StringUtils;

import com.ximpleware.NavException;
import com.ximpleware.VTDGenEx;
import com.ximpleware.VTDNav;
import com.ximpleware.VTDNavEx;

public class FB2ContentHandler4 extends FB2BaseHandler {

    private boolean documentStarted = false, documentEnded = false;

    private boolean inSection = false;

    private boolean paragraphParsing = false;

    private boolean cover = false;

    private String tmpBinaryName = null;
    private boolean parsingNotes = false;
    private boolean parsingBinary = false;
    private boolean inTitle = false;
    private boolean inCite = false;
    private int noteId = -1;
    private boolean noteFirstWord = true;

    private boolean spaceNeeded = true;

    private static final Pattern notesPattern = Pattern.compile("n([0-9]+)|n_([0-9]+)|note_([0-9]+)|.*?([0-9]+)");
    private final StringBuilder tmpBinaryContents = new StringBuilder(64 * 1024);
    private final StringBuilder title = new StringBuilder();

    private final StrBuilder tmpTagContent = new StrBuilder(16 * 1024);

    final SparseArray<Words> words = new SparseArray<Words>();

    int sectionLevel = -1;

    private boolean skipContent = true;

    private MarkupTable currentTable;

    public FB2ContentHandler4(ParsedContent content) {
        super(content);
    }

    public void parse(VTDGenEx inStream) throws NavException {
        VTDNavEx nav = inStream.getNav();

        boolean res = nav.toElement(VTDNav.ROOT);
        if (!res) {
            return;
        }

        FB2Tag tag = FB2Tag.unknownTag;
        int skipUntilSiblingOrParent = -1;

        int first = nav.getCurrentIndex();
        int last = nav.getTokenCount();

        String[] tagAttrs = new String[2];
        FB2Tag[] tags = new FB2Tag[1024];

        StrBuilder buf = new StrBuilder(1024);

        int maxDepth = -1;
        for (int ci = first; ci < last;) {
            final int depth = nav.getTokenDepth(ci);
            final int type = nav.getTokenType(ci);

            if (skipUntilSiblingOrParent != -1) {
                if (type == VTDNav.TOKEN_STARTING_TAG && depth <= skipUntilSiblingOrParent) {
                    skipUntilSiblingOrParent = -1;
                } else {
                    ci++;
                    continue;
                }
            }

            if (type == VTDNav.TOKEN_STARTING_TAG) {
                String name = nav.toRawString(ci);
                tag = FB2Tag.getTagByName(name);

                for (int d = maxDepth; d >= depth; d--) {
                    if (tags[d] != null) {
                        endElement(tags[d]);
                        tags[d] = null;
                    }
                }
                tags[depth] = tag;
                maxDepth = depth;

                if (tag.tag == FB2Tag.UNKNOWN) {
                    skipUntilSiblingOrParent = depth;
                    ci++;
                    continue;
                }

                if (tag.attributes.length > 0) {
                    ci = fillAtributes(nav, ci + 1, last, tag, tagAttrs);
                    startElement(tag, tagAttrs);
                } else {
                    ci = skipAtributes(nav, ci + 1, last);
                    startElement(tag);
                }
                continue;
            }

            for (int d = maxDepth; d > depth; d--) {
                if (tags[d] != null) {
                    endElement(tags[d]);
                    tags[d] = null;
                }
            }
            maxDepth = depth;

            if (type == VTDNav.TOKEN_CHARACTER_DATA || type == VTDNav.TOKEN_CDATA_VAL) {
                if (tag.processText) {
                    buf.setLength(0);
                    nav.toString(ci, buf);
                    char[] charArray = buf.getValue();
                    characters(charArray, 0, buf.length());
                }
                ci++;
                continue;
            }

            ci++;
        }

        for (int d = maxDepth; d >= 0; d--) {
            if (tags[d] != null) {
                endElement(tags[d]);
                tags[d] = null;
            }
        }

        inStream.clear();
    }

    private int skipAtributes(VTDNavEx nav, int first, int last) throws NavException {
        for (int inner = first; inner < last; inner++) {
            int innerType = nav.getTokenType(inner);
            switch (innerType) {
                case VTDNav.TOKEN_ATTR_NAME:
                case VTDNav.TOKEN_ATTR_NS:
                case VTDNav.TOKEN_ATTR_VAL:
                    break;
                default:
                    return inner;
            }
        }
        return last;
    }

    private int fillAtributes(VTDNavEx nav, int first, int last, FB2Tag tag, String[] tagAttrs) throws NavException {
        for (int i = 0; i < tag.attributes.length; i++) {
            tagAttrs[i] = null;
        }

        for (int inner = first; inner < last; inner++) {
            int innerType = nav.getTokenType(inner);
            switch (innerType) {
                case VTDNav.TOKEN_ATTR_NAME:
                    String[] qName = nav.toString(inner).split(":");
                    String attrName = qName[qName.length-1];
                    int attrIndex = Arrays.binarySearch(tag.attributes, attrName);
                    if (attrIndex >= 0) {
                        String attrValue = nav.toString(inner + 1);
                        tagAttrs[attrIndex] = attrValue;
                    }
                    inner++;
                    break;
                case VTDNav.TOKEN_ATTR_NS:
                case VTDNav.TOKEN_ATTR_VAL:
                    break;
                default:
                    return inner;
            }
        }

        return last;
    }

    public void startElement(FB2Tag t, String... attributes) throws NavException {

        spaceNeeded = true;
        final ArrayList<MarkupElement> markupStream = parsedContent.getMarkupStream(currentStream);

        if (tmpTagContent.length() > 0) {
            processTagContent();
        }

        switch (t.tag) {
            case FB2Tag.P:
                paragraphParsing = true;
                if (!parsingNotes) {
                    if (!inTitle) {
                        markupStream.add(crs.paint.pOffset);
                    } else {
                        if (title.length() > 0) {
                            title.append(" ");
                        }
                    }
                }
                break;
            case FB2Tag.V:
                paragraphParsing = true;
                markupStream.add(crs.paint.pOffset);
                markupStream.add(crs.paint.vOffset);
                break;
            case FB2Tag.BINARY:
                tmpBinaryName = attributes[0];
                tmpBinaryContents.setLength(0);
                parsingBinary = true;
                break;
            case FB2Tag.BODY:
                if (!documentStarted && !documentEnded) {
                    documentStarted = true;
                    skipContent = false;
                    currentStream = null;
                }
                if ("notes".equals(attributes[0])) {
                    if (documentStarted) {
                        documentEnded = true;
                        parsedContent.getMarkupStream(null).add(new MarkupEndDocument());
                    }
                    parsingNotes = true;
                    crs = new RenderingStyle(parsedContent, TextStyle.FOOTNOTE);
                }
                break;
            case FB2Tag.SECTION:
                if (parsingNotes) {
                    currentStream = attributes[0];
                    if (currentStream != null) {
                        final String n = getNoteId(currentStream, true);
                        parsedContent.getMarkupStream(currentStream).add(text(n.toCharArray(), 0, n.length(), crs));
                        parsedContent.getMarkupStream(currentStream).add(crs.paint.fixedSpace);
                    }
                } else {
                    inSection = true;
                    sectionLevel++;
                }
                break;
            case FB2Tag.TITLE:
                if (!parsingNotes) {
                    setTitleStyle(!inSection ? TextStyle.MAIN_TITLE : TextStyle.SECTION_TITLE);
                    markupStream.add(crs.jm);
                    markupStream.add(emptyLine(crs.textSize));
                    markupStream.add(MarkupParagraphEnd.E);
                    title.setLength(0);
                } else {
                    skipContent = true;
                }
                inTitle = true;
                break;
            case FB2Tag.CITE:
                inCite = true;
                setEmphasisStyle();
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(MarkupParagraphEnd.E);
                break;
            case FB2Tag.SUBTITLE:
                paragraphParsing = true;
                markupStream.add(setSubtitleStyle().jm);
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(MarkupParagraphEnd.E);
                break;
            case FB2Tag.TEXT_AUTHOR:
                paragraphParsing = true;
                markupStream.add(setTextAuthorStyle(inCite).jm);
                markupStream.add(crs.paint.pOffset);
                break;
            case FB2Tag.DATE:
                if (documentStarted && !documentEnded || parsingNotes) {
                    paragraphParsing = true;
                    markupStream.add(setTextAuthorStyle(inCite).jm);
                    markupStream.add(crs.paint.pOffset);
                }
                break;
            case FB2Tag.A:
                if (paragraphParsing) {
                    if ("note".equalsIgnoreCase(attributes[0])) {
                        String note = attributes[1];
                        markupStream.add(new MarkupNote(note));
                        String prettyNote = " " + getNoteId(note, false);
                        markupStream.add(MarkupNoSpace._instance);
                        markupStream.add(new TextElement(prettyNote.toCharArray(), 0, prettyNote.length(),
                                new RenderingStyle(crs, Script.SUPER)));
                        skipContent = true;
                    }
                }
                break;
            case FB2Tag.EMPTY_LINE:
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(MarkupParagraphEnd.E);
                break;
            case FB2Tag.POEM:
                markupStream.add(MarkupParagraphEnd.E);
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(setPoemStyle().jm);
                break;
            case FB2Tag.STRONG:
                setBoldStyle();
                break;
            case FB2Tag.SUP:
                setSupStyle();
                spaceNeeded = false;
                break;
            case FB2Tag.SUB:
                setSubStyle();
                spaceNeeded = false;
                break;
            case FB2Tag.STRIKETHROUGH:
                setStrikeThrough();
                break;
            case FB2Tag.EMPHASIS:
                setEmphasisStyle();
                break;
            case FB2Tag.EPIGRAPH:
                markupStream.add(MarkupParagraphEnd.E);
                markupStream.add(setEpigraphStyle().jm);
                break;
            case FB2Tag.IMAGE:
                final String ref = attributes[0];
                if (cover) {
                    parsedContent.setCover(ref);
                } else {
                    if (!paragraphParsing) {
                        markupStream.add(emptyLine(crs.textSize));
                        markupStream.add(MarkupParagraphEnd.E);
                    }
                    markupStream.add(new MarkupImageRef(ref, paragraphParsing));
                    if (!paragraphParsing) {
                        markupStream.add(emptyLine(crs.textSize));
                        markupStream.add(MarkupParagraphEnd.E);
                    }
                }
                break;
            case FB2Tag.COVERPAGE:
                cover = true;
                break;
            case FB2Tag.ANNOTATION:
                skipContent = false;
                break;
            case FB2Tag.TABLE:
                currentTable = new MarkupTable();
                markupStream.add(currentTable);
                break;
            case FB2Tag.TR:
                if (currentTable != null) {
                    currentTable.addRow();
                }
                break;
            case FB2Tag.TD:
            case FB2Tag.TH:
                if (currentTable != null) {
                    final int rowCount = currentTable.getRowCount();
                    final Cell c = currentTable.new Cell();
                    currentTable.addCol(c);
                    final String streamId = currentTable.uuid + ":" + rowCount + ":"
                            + currentTable.getColCount(rowCount - 1);
                    c.stream = streamId;
                    c.hasBackground = t.tag == FB2Tag.TH;
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

        }
        tmpTagContent.setLength(0);
    }

    private MarkupElement emptyLine(final int textSize) {
        return crs.paint.emptyLine;
    }

    private TextElement text(final char[] ch, final int st, final int len, final RenderingStyle style) {
        Words w = words.get(style.paint.key);
        if (w == null) {
            w = new Words();
            words.append(style.paint.key, w);
        }
        return w.get(ch, st, len, style);
    }

    public void endElement(FB2Tag t) {
        if (tmpTagContent.length() > 0) {
            processTagContent();
        }
        spaceNeeded = true;
        final ArrayList<MarkupElement> markupStream = parsedContent.getMarkupStream(currentStream);
        switch (t.tag) {
            case FB2Tag.P:
            case FB2Tag.V:
                if (!skipContent) {
                    markupStream.add(MarkupParagraphEnd.E);
                }
                paragraphParsing = false;
                break;
            case FB2Tag.BINARY:
                if (tmpBinaryContents.length() > 0) {
                    parsedContent.addImage(tmpBinaryName, tmpBinaryContents.toString());
                    tmpBinaryName = null;
                    tmpBinaryContents.setLength(0);
                }
                parsingBinary = false;
                break;
            case FB2Tag.BODY:
                parsingNotes = false;
                currentStream = null;
                break;
            case FB2Tag.SECTION:
                if (parsingNotes) {
                    noteId = -1;
                    noteFirstWord = true;
                } else {
                    if (inSection) {
                        markupStream.add(MarkupEndPage.E);
                        sectionLevel--;
                        inSection = false;
                    }
                }
                break;
            case FB2Tag.TITLE:
                inTitle = false;
                skipContent = false;
                if (!parsingNotes) {
                    markupStream.add(new MarkupTitle(title.toString(), sectionLevel));
                    markupStream.add(emptyLine(crs.textSize));
                    markupStream.add(MarkupParagraphEnd.E);
                    markupStream.add(setPrevStyle().jm);
                }
                break;
            case FB2Tag.CITE:
                inCite = false;
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(MarkupParagraphEnd.E);
                markupStream.add(setPrevStyle().jm);
                break;
            case FB2Tag.SUBTITLE:
                markupStream.add(MarkupParagraphEnd.E);
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(MarkupParagraphEnd.E);
                markupStream.add(setPrevStyle().jm);
                paragraphParsing = false;
                break;
            case FB2Tag.TEXT_AUTHOR:
            case FB2Tag.DATE:
                markupStream.add(MarkupParagraphEnd.E);
                markupStream.add(setPrevStyle().jm);
                paragraphParsing = false;
                break;
            case FB2Tag.STANZA:
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(MarkupParagraphEnd.E);
                break;
            case FB2Tag.POEM:
                markupStream.add(emptyLine(crs.textSize));
                markupStream.add(MarkupParagraphEnd.E);
                markupStream.add(setPrevStyle().jm);
                break;
            case FB2Tag.STRONG:
                setPrevStyle();
                spaceNeeded = false;
                break;
            case FB2Tag.STRIKETHROUGH:
                setPrevStyle();
                break;
            case FB2Tag.SUP:
                setPrevStyle();
                if (markupStream.get(markupStream.size() - 1) instanceof MarkupNoSpace) {
                    markupStream.remove(markupStream.size() - 1);
                }
                break;
            case FB2Tag.SUB:
                setPrevStyle();
                if (markupStream.get(markupStream.size() - 1) instanceof MarkupNoSpace) {
                    markupStream.remove(markupStream.size() - 1);
                }
                break;
            case FB2Tag.EMPHASIS:
                setPrevStyle();
                spaceNeeded = false;
                break;
            case FB2Tag.EPIGRAPH:
                markupStream.add(setPrevStyle().jm);
                break;
            case FB2Tag.COVERPAGE:
                cover = false;
                break;
            case FB2Tag.A:
                if (paragraphParsing) {
                    skipContent = false;
                }
                break;
            case FB2Tag.ANNOTATION:
                skipContent = true;
                parsedContent.getMarkupStream(null).add(MarkupEndPage.E);
                break;
            case FB2Tag.TABLE:
                currentTable = null;
                break;
            case FB2Tag.TD:
            case FB2Tag.TH:
                paragraphParsing = false;
                currentStream = oldStream;
                break;
        }
    }

    private void processTagContent() {
        final int length = tmpTagContent.length();
        final int start = 0;
        char[] ch = tmpTagContent.getValue();
        if (inTitle) {
            title.append(ch, start, length);
        }
        final int count = StringUtils.split(ch, start, length, starts, lengths);

        if (count > 0) {
            final ArrayList<MarkupElement> markupStream = parsedContent.getMarkupStream(currentStream);
            if (!spaceNeeded && !Character.isWhitespace(ch[start])) {
                markupStream.add(MarkupNoSpace._instance);
            }
            spaceNeeded = true;

            for (int i = 0; i < count; i++) {
                final int st = starts[i];
                final int len = lengths[i];
                if (parsingNotes) {
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
                }
                markupStream.add(text(ch, st, len, crs));
                if (crs.script != null) {
                    markupStream.add(MarkupNoSpace._instance);
                }
            }
            if (Character.isWhitespace(ch[start + length - 1])) {
                markupStream.add(MarkupNoSpace._instance);
                markupStream.add(crs.paint.space);
            }
            spaceNeeded = false;
        }
        tmpTagContent.setLength(0);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (skipContent
                || (!(documentStarted && !documentEnded) && !paragraphParsing && !parsingBinary && !parsingNotes)) {
            return;
        }
        if (parsingBinary) {
            tmpBinaryContents.append(ch, start, length);
        } else {
            tmpTagContent.append(ch, start, length);
        }
    }

    private String getNoteId(String noteName, boolean bracket) {
        final Matcher matcher = notesPattern.matcher(noteName);
        String n = noteName;
        if (matcher.matches()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    noteId = Integer.parseInt(matcher.group(i));
                    n = "" + noteId + (bracket ? ")" : "");
                    break;
                }
                noteId = -1;
            }
        }
        return n;
    }

}
