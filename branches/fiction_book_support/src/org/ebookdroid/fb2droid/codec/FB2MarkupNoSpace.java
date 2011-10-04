package org.ebookdroid.fb2droid.codec;


public class FB2MarkupNoSpace implements FB2MarkupElement {

    @Override
    public void publishToDocument(FB2Document doc) {
        doc.insertSpace = false;
    }

}
