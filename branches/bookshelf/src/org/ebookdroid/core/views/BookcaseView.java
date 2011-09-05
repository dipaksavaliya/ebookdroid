package org.ebookdroid.core.views;

import org.ebookdroid.R;
import org.ebookdroid.core.IBrowserActivity;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BookcaseView extends LinearLayout {

    private final TextView shelfCaption;
    private final BookshelfView shelf;
    private final BooksAdapter adapter;

    public BookcaseView(IBrowserActivity base, BooksAdapter adapter) {
        super(base.getContext());
        this.adapter = adapter;
        setOrientation(VERTICAL);

        LinearLayout ll = (LinearLayout) LayoutInflater.from(base.getContext()).inflate(R.layout.bookshelf_caption,
                null);
        addView(ll);

        shelfCaption = (TextView) ll.findViewById(R.id.ShelfCaption);

        ImageButton prevButton = (ImageButton) ll.findViewById(R.id.ShelfLeftButton);
        prevButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BookcaseView.this.adapter.prevList();
            }
        });

        ImageButton nextButton = (ImageButton) ll.findViewById(R.id.ShelfRightButton);
        nextButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BookcaseView.this.adapter.nextList();
            }
        });

        shelf = new BookshelfView(base, adapter);
        addView(shelf);

        adapter.registerDataSetObserver(new DataSetObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                shelfCaption.setText(BookcaseView.this.adapter.getListName());
            }
        });
    }
}
