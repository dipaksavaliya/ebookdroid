package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.fb2droid.codec.FB2Document.JustificationMode;

import android.util.Base64;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FB2BinaryHandler extends DefaultHandler {

    private final FB2Document document;

    public FB2BinaryHandler(FB2Document fb2Document) {
        this.document = fb2Document;
    }

    private String tmpBinaryName = null;
    private StringBuffer tmpBinaryContents = null;
    private boolean parsingNotes = false;
    private boolean parsingNotesP = false;
    private boolean parsingBinary = false;
    private boolean inTitle = false;
    private String noteName = null;
    private int noteId = -1;
    private boolean noteFirstWord = true;
    private Pattern notesPattern = Pattern.compile("n([0-9]+)");
    private ArrayList<FB2Line> noteLines = null;

    private boolean bold = false;
    private boolean italic = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("binary".equalsIgnoreCase(qName)) {
            tmpBinaryName = attributes.getValue("id");
            tmpBinaryContents = new StringBuffer();
            parsingBinary = true;
            return;
        }
        if ("body".equalsIgnoreCase(qName)) {
            if ("notes".equals(attributes.getValue("name"))) {
                parsingNotes = true;
            }
        }
        if ("title".equalsIgnoreCase(qName)) {
            inTitle = true;
        }
        if ("p".equalsIgnoreCase(qName) && parsingNotes && !inTitle) {
            parsingNotesP = true;
        }
        if ("strong".equals(qName)) {
            FB2Document.FOOTNOTETEXTPAINT.setFakeBoldText(true);
            bold = true;
            return;
        }
        if ("emphasis".equals(qName)) {
            FB2Document.FOOTNOTETEXTPAINT.setTypeface(FB2Document.ITALIC_TF);
            italic = true;
            return;
        }

        if ("section".equalsIgnoreCase(qName) && parsingNotes) {
            noteName = attributes.getValue("id");
            if (noteName != null) {
                Matcher matcher = notesPattern.matcher(noteName);
                String n = noteName;
                if (matcher.matches()) {
                    noteId = Integer.parseInt(matcher.group(1));
                    n = "" + noteId + ")";
                }
                noteLines = new ArrayList<FB2Line>();
                FB2Line lastLine = FB2Line.getLastLine(noteLines);
                lastLine.append(new FB2TextElement(n, FB2Document.FOOTNOTE_SIZE,
                        (int) FB2Document.FOOTNOTETEXTPAINT.measureText(n), false, false));
                lastLine.append(new FB2LineWhiteSpace((int) FB2Document.FOOTNOTETEXTPAINT.measureText(" "), FB2Document.FOOTNOTE_SIZE, false));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("binary".equalsIgnoreCase(qName)) {
            byte[] data = Base64.decode(tmpBinaryContents.toString(), Base64.DEFAULT);
            document.addImage(tmpBinaryName, data);
            tmpBinaryName = null;
            tmpBinaryContents = null;
            parsingBinary = false;
        }
        if ("body".equalsIgnoreCase(qName)) {
            parsingNotes = false;
        }
        if ("title".equalsIgnoreCase(qName)) {
            inTitle = false;
        }
        if ("section".equalsIgnoreCase(qName) && parsingNotes) {
            document.addNote(noteName, noteLines);
            noteLines = null;
            noteId = -1;
            noteFirstWord = true;
        }
        if ("p".equalsIgnoreCase(qName) && parsingNotesP) {
            parsingNotesP = false;
            FB2Line line = FB2Line.getLastLine(noteLines);
            line.append(new FB2LineWhiteSpace(FB2Page.PAGE_WIDTH - line.getWidth() - 2 * FB2Page.MARGIN_X,
                    (int) FB2Document.FOOTNOTETEXTPAINT.getTextSize(), false));
            for (FB2Line l : noteLines) {
                l.applyJustification(JustificationMode.Justify);
            }
        }
        if ("strong".equals(qName)) {
            FB2Document.FOOTNOTETEXTPAINT.setFakeBoldText(false);
            bold = false;
            return;
        }
        if ("emphasis".equals(qName)) {
            FB2Document.FOOTNOTETEXTPAINT.setTypeface(FB2Document.NORMAL_TF);
            italic = false;
            return;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (parsingBinary && tmpBinaryContents != null) {
            tmpBinaryContents.append(ch, start, length);
        }
        if (parsingNotesP && noteLines != null) {
            String text = new String(ch, start, length);
            int space = (int) FB2Document.FOOTNOTETEXTPAINT.measureText(" ");
            StringTokenizer st = new StringTokenizer(text);
            while (st.hasMoreTokens()) {
                String word = st.nextToken();
                if (noteFirstWord) {
                    noteFirstWord = false;
                    int id = -2;
                    try {
                        id = Integer.parseInt(word);
                    } catch (Exception e) {
                        id = -2;
                    }
                    if (id == noteId) {
                        continue;
                    }
                }
                FB2TextElement te = new FB2TextElement(word, (int) FB2Document.FOOTNOTETEXTPAINT.getTextSize(),
                        (int) FB2Document.FOOTNOTETEXTPAINT.measureText(word), bold, italic);
                FB2Line line = FB2Line.getLastLine(noteLines);
                if (line.getWidth() + 2 * FB2Page.MARGIN_X + space + te.getWidth() < FB2Page.PAGE_WIDTH) {
                    if (line.hasNonWhiteSpaces()) {
                        line.append(new FB2LineWhiteSpace(space, (int) FB2Document.FOOTNOTETEXTPAINT.getTextSize(),
                                true));
                    }
                } else {
                    line = new FB2Line();
                    noteLines.add(line);
                }
                line.append(te);
            }
        }
    }

}
