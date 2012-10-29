package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Line;

public interface MarkupElement {

    void publishToStream(DataOutputStream out) throws IOException;

    void publishToLines(MarkupStream stream, ArrayList<Line> lines, LineCreationParams params);
}
