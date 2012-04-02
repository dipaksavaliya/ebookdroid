package org.ebookdroid.ui.viewer.views;

import org.ebookdroid.R;
import org.ebookdroid.ui.viewer.ViewerActivity;

import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.params.Constant;
import org.emdev.ui.actions.params.EditableValue;
import org.emdev.utils.LayoutUtils;

public class SearchControls extends LinearLayout {

    private EditText m_edit;

    public SearchControls(final ViewerActivity parent) {
        super(parent);
        setVisibility(View.GONE);
        setOrientation(LinearLayout.VERTICAL);

        LinearLayout line = new LinearLayout(parent);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setLayoutParams(new LinearLayout.LayoutParams(LayoutUtils.FILL_PARENT, LayoutUtils.WRAP_CONTENT));
        line.setGravity(Gravity.TOP);
        line.setBackgroundColor(Color.BLACK);

        final ImageButton prevButton = new ImageButton(parent);
        final ImageButton nextButton = new ImageButton(parent);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(40, 40, 1.0f);
        btnParams.leftMargin = 4;
        btnParams.rightMargin = 4;
        btnParams.bottomMargin = 4;

        prevButton.setLayoutParams(btnParams);
        nextButton.setLayoutParams(btnParams);
        nextButton.setBackgroundColor(Color.BLACK);
        
        m_edit = new EditText(parent);
        m_edit.setSingleLine();
        m_edit.setLayoutParams(new LinearLayout.LayoutParams(LayoutUtils.FILL_PARENT, LayoutUtils.FILL_PARENT, 100.0f));
        m_edit.setGravity(Gravity.CENTER_VERTICAL);

        ActionEx forwardSearch = parent.getController().getOrCreateAction(R.id.actions_doSearch);
        ActionEx backwardSearch = parent.getController().getOrCreateAction(R.id.actions_doSearchBack);

        forwardSearch.addParameter(new EditableValue("input", m_edit)).addParameter(new Constant("direction", 1));
        backwardSearch.addParameter(new EditableValue("input", m_edit)).addParameter(new Constant("direction", -1));

        prevButton.setImageResource(R.drawable.arrowleft);
        prevButton.setOnClickListener(backwardSearch);

        nextButton.setImageResource(R.drawable.arrowright);
        nextButton.setOnClickListener(forwardSearch);
        m_edit.setOnEditorActionListener(forwardSearch);

        // line.addView(prevButton);
        line.addView(m_edit);
        line.addView(nextButton);

        addView(line);
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            m_edit.requestFocus();
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return false;
    }
}
