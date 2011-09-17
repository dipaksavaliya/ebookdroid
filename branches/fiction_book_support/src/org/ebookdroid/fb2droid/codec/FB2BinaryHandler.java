package org.ebookdroid.fb2droid.codec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FB2BinaryHandler extends DefaultHandler {

    private final FB2Document document;

    public FB2BinaryHandler(FB2Document fb2Document) {
        this.document = fb2Document;
    }

    String tmpBinaryName = null;
    StringBuffer tmpBinaryContents = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("binary".equalsIgnoreCase(qName)) {
            tmpBinaryName = attributes.getValue("id");
            tmpBinaryContents = new StringBuffer();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("binary".equalsIgnoreCase(qName)) {
            byte[] data = Base64.decode(tmpBinaryContents.toString(), Base64.DEFAULT);
            document.addImage(tmpBinaryName, data);
            tmpBinaryName = null;
            tmpBinaryContents = null;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tmpBinaryContents != null) {
            tmpBinaryContents.append(ch, start, length);
        }
    }

}
