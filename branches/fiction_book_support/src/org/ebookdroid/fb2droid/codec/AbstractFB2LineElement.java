package org.ebookdroid.fb2droid.codec;

import java.io.DataInputStream;
import java.io.IOException;


public abstract class AbstractFB2LineElement implements FB2LineElement {

    @Override
    public void publishToDocument(FB2Document doc) {
        doc.publishElement(this);
    }

    public static FB2LineElement deserialize(DataInputStream in) throws IOException {
        int tag = in.readInt();
        switch (tag) {
            case TEXT_ELEMENT_TAG:
                return FB2TextElement.deserializeImpl(in);
            case WHITESPACE_ELEMENT_TAG:
                return FB2LineWhiteSpace.deserializeImpl(in);
            case IMAGE_ELEMENT_TAG:
                return FB2Image.deserializeImpl(in);
            case RULE_ELEMENT_TAG:
                return FB2HorizontalRule.deserializeImpl(in);
        }
        throw new IOException("Unknown tag");
    }

}
