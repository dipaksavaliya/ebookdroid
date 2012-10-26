package org.emdev.common.textmarkup;

import org.ebookdroid.droids.fb2.codec.LineCreationParams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.emdev.common.textmarkup.line.Line;

public enum JustificationMode implements MarkupElement {

    /** Centered line */
    Center,

    /** Left justified */
    Left,

    /** Right justified */
    Right,

    /** Width justified */
    Justify;

    @Override
    public void publishToLines(ArrayList<Line> lines, LineCreationParams params) {
        params.jm = this;
    }

    @Override
    public void publishToStream(DataOutputStream out) throws IOException {
        write(out, this);
    }

    public static void write(DataOutputStream out, JustificationMode jm) throws IOException {
        out.writeByte(MarkupTag.JustificationMode.ordinal());
        out.writeByte(jm.ordinal());
    }

    public static void publishToLines(DataInputStream in, ArrayList<Line> lines, LineCreationParams params)
            throws IOException {
        JustificationMode jm = JustificationMode.values()[in.readByte()];
        jm.publishToLines(lines, params);
    }
}
