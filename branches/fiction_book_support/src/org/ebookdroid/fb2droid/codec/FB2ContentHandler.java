package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.fb2droid.codec.FB2Document.JustificationMode;
import org.ebookdroid.utils.StringUtils;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FB2ContentHandler extends DefaultHandler {

    private final FB2Document document;

    private boolean documentStarted = false, documentEnded = false;

    private boolean inSection = false;

    private boolean paragraphParsing = false;
    
    private boolean bold = false;
    
    private boolean italic = false;
    
    private boolean cover = false;

    private Paint currentTextPaint = FB2Document.NORMALTEXTPAINT;

    private final ArrayList<FB2Line> paragraphLines = new ArrayList<FB2Line>();

    private JustificationMode jm = JustificationMode.Justify;
    
    public FB2ContentHandler(FB2Document fb2Document) {
        this.document = fb2Document;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("body".equals(qName) && !documentStarted && !documentEnded) {
            documentStarted = true;
            return;
        }
        if ("section".equals(qName)) {
            inSection = true;
            return;
        }
        if ("title".equals(qName)) {
            if (!inSection) {
                currentTextPaint = FB2Document.MAINTITLETEXTPAINT;
            } else {
                currentTextPaint = FB2Document.SECTIONTITLETEXTPAINT;
            }
            jm = JustificationMode.Center;
            FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, (int) currentTextPaint.getTextSize(), true));
            
            document.appendLine(line);
            return;
        }
        if ("p".equals(qName)) {
            paragraphParsing = true;
            paragraphLines.clear();
            if (jm != JustificationMode.Center) {
                FB2Line.getLastLine(paragraphLines).append(
                        new FB2LineWhiteSpace((int) currentTextPaint.getTextSize() * 3, (int) currentTextPaint.getTextSize(), false));
            }
            return;
        }
        if ("a".equals(qName) && paragraphParsing) {
            if ("note".equalsIgnoreCase(attributes.getValue("type"))) {
                FB2Line line = FB2Line.getLastLine(paragraphLines);
                line.addNote(attributes.getValue("href"));
            }
        }
        if ("empty-line".equals(qName)) {
            FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, (int) currentTextPaint.getTextSize(), true));
            
            document.appendLine(line);
            return;
        }
        if ("strong".equals(qName)) {
            currentTextPaint.setFakeBoldText(true);
            bold = true;
            return;
        }
        if ("emphasis".equals(qName)) {
            currentTextPaint.setTypeface(FB2Document.ITALIC_TF);
            italic = true;
            return;
        }
        if ("epigraph".equals(qName)) {
            jm = JustificationMode.Right;
            currentTextPaint.setTypeface(FB2Document.ITALIC_TF);
            italic = true;
            return;
        }
        if ("image".equals(qName)) {
            if (cover) {
                document.setCover(attributes.getValue("href"));
            }
            insertImage(attributes.getValue("href"));
            return;
        }
        if ("coverpage".equals(qName)) {
            cover = true;
            return;
        }
    }

    private void insertImage(String name) {
        FB2Image image = document.getImage(name);
        if (image == null) {
            return;
        }
        if (!paragraphParsing) {
            FB2Line line = new FB2Line();
            line.append(image);
            line.applyJustification(JustificationMode.Center);
            document.appendLine(line);
        } else {
            if (!paragraphLines.isEmpty()) {
                int space = (int)currentTextPaint.measureText(" ");
                appendLineElement(space, image);
            }
        }
        
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("body".equals(qName) && documentStarted && !documentEnded) {
            documentEnded = true;
            throw new StopParsingException();
        }
        if ("section".equals(qName)) {
            document.commitPage();
            inSection = false;
            return;
        }
        if ("title".equals(qName)) {
            FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, (int) currentTextPaint.getTextSize(), true));
            
            document.appendLine(line);
            currentTextPaint = FB2Document.NORMALTEXTPAINT;
            jm = JustificationMode.Justify;
            return;
        }
        if ("p".equals(qName)) {
            flushParagraphLines();
            paragraphParsing = false;
            return;
        }
        if ("strong".equals(qName)) {
            currentTextPaint.setFakeBoldText(false);
            bold = false;
            return;
        }
        if ("emphasis".equals(qName)) {
            currentTextPaint.setTypeface(FB2Document.NORMAL_TF);
            italic = false;
            return;
        }
        if ("epigraph".equals(qName)) {
            jm = JustificationMode.Justify;
            currentTextPaint.setTypeface(FB2Document.NORMAL_TF);
            italic = false;
            return;
        }
        if ("coverpage".equals(qName)) {
            cover = false;
            return;
        }
    }

    private void flushParagraphLines() {
        if (paragraphLines.isEmpty()) {
            return;
        }
        if (jm == JustificationMode.Justify) {
            FB2Line l = FB2Line.getLastLine(paragraphLines);
            l.append(new FB2LineWhiteSpace(FB2Page.PAGE_WIDTH - l.getWidth() - 2 * FB2Page.MARGIN_X, (int) currentTextPaint.getTextSize(), false));
        }
        for (FB2Line l : paragraphLines) {
            l.applyJustification(jm);
            document.appendLine(l);
        }
        paragraphLines.clear();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!documentStarted || documentEnded || !paragraphParsing) {
            return;
        }
        int space = (int)currentTextPaint.measureText(" ");
        for (String word : StringUtils.split(ch, start, length)) {
            FB2TextElement te = new FB2TextElement(word, (int) currentTextPaint.getTextSize(), (int)currentTextPaint.measureText(word), bold, italic);
            appendLineElement(space, te);
        }
        
    }

    private void appendLineElement(int space, FB2LineElement te) {
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
