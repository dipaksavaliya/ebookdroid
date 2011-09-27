package org.ebookdroid.fb2droid.codec;


public class FB2MarkupTitle implements FB2MarkupElement {

    private final String title;

    public FB2MarkupTitle(String string) {
        this.title = string;
    }

    @Override
    public void publishToDocument(FB2Document doc) {
        doc.addTitle(title);
    }

}
