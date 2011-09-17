package org.ebookdroid.fb2droid.codec;

import org.xml.sax.helpers.DefaultHandler;


public class FB2BinaryHandler extends DefaultHandler {
    private final FB2Document document;


    public FB2BinaryHandler(FB2Document fb2Document) {
        this.document = fb2Document;
    }

}
