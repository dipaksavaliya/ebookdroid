package org.ebookdroid.fb2droid.codec;



public abstract class AbstractFB2LineElement implements FB2LineElement {

    @Override
    public void publishToDocument(FB2Document doc) {
        doc.publishElement(this);
    }
}
