package org.ebookdroid.fb2droid.codec;


public class FB2MarkupImageRef implements FB2MarkupElement {

    private final String ref;
    private final boolean inline;

    public FB2MarkupImageRef(String name, boolean inline) {
        this.ref = name;
        this.inline = inline;
    }

    @Override
    public void publishToDocument(FB2Document doc) {
        doc.publishImage(ref, inline);
    }

}
