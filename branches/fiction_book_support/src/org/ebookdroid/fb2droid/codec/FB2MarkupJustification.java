package org.ebookdroid.fb2droid.codec;


public class FB2MarkupJustification implements FB2MarkupElement {

    private final JustificationMode jm;

    public FB2MarkupJustification(JustificationMode jm) {
        this.jm = jm;
    }

    @Override
    public void publishToDocument(FB2Document doc) {
        doc.jm = jm;
    }

}
