package org.ebookdroid.core;

public interface IEvent {

    ViewState process();

    ViewState process(ViewState viewState);

    boolean process(ViewState viewState, Page page);

    boolean process(ViewState viewState, PageTree nodes);

    boolean process(ViewState viewState, PageTree nodes, PageTreeLevel level);

    boolean process(ViewState viewState, PageTreeNode node);
}
