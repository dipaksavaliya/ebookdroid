package org.emdev.common.textmarkup.text;

import java.util.concurrent.atomic.AtomicLong;


public interface ITextProvider {

    AtomicLong SEQ = new AtomicLong();

    long id();

    char[] text();

    int size();
}
