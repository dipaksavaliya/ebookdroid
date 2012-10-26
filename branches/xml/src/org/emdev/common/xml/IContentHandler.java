package org.emdev.common.xml;

import org.emdev.common.textmarkup.text.ITextProvider;
import org.emdev.common.xml.tags.XmlTag;


public interface IContentHandler {

    boolean parseAttributes(XmlTag tag);

    void startElement(final XmlTag tag, final String... attributes);

    boolean skipCharacters();

    void characters(final ITextProvider p, final int start, final int length);

    void endElement(final XmlTag tag);

}
