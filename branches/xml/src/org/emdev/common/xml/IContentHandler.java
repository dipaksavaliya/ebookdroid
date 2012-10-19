package org.emdev.common.xml;

import org.emdev.common.xml.tags.XmlTag;


public interface IContentHandler {

    boolean parseAttributes(XmlTag tag);

    void startElement(final XmlTag tag, final String... attributes);

    boolean skipCharacters();

    void characters(final char[] ch, final int start, final int length, boolean persistent);

    void endElement(final XmlTag tag);

}
