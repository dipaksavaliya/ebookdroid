package org.emdev.common.textmarkup;

import java.io.DataOutputStream;
import java.io.IOException;

import org.emdev.common.textmarkup.line.LineStream;

public interface MarkupElement {

    void publishToStream(DataOutputStream out) throws IOException;

    void publishToLines(MarkupStream stream, LineStream lines);
}
