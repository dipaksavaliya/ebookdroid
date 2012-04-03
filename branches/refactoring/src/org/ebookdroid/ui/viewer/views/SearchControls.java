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
    private ImageButton m_prevButton;
    private ImageButton m_nextButton;
    private LinearLayout m_line;

    public SearchControls(final ViewerActivity parent) {
        super(parent);
        setVisibility(View.GONE);
        setOrientation(LinearLayout.VERTICAL);

        m_line = new LinearLayout(parent);
        m_line.setOrientation(LinearLayout.HORIZONTAL);
        m_line.setLayoutParams(new LinearLayout.LayoutParams(LayoutUtils.FILL_PARENT, LayoutUtils.WRAP_CONTENT));
        m_line.setGravity(Gravity.TOP);
        m_line.setBackgroundColor(Color.BLACK);

        m_prevButton = new ImageButton(parent);
        m_nextButton = new ImageButton(parent);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(40, 40, 1.0f);
        btnParams.leftMargin = 4;
        btnParams.rightMargin = 4;
        btnParams.bottomMargin = 4;

        m_prevButton.setLayoutParams(btnParams);
        m_nextButton.setLayoutParams(btnParams);
        m_nextButton.setBackgroundColor(Color.BLACK);
        
        m_edit = new EditText(parent);
        m_edit.setSingleLine();
        m_edit.setLayoutParams(new LinearLayout.LayoutParams(LayoutUtils.FILL_PARENT, LayoutUtils.FILL_PARENT, 100.0f));
        m_edit.setGravity(Gravity.CENTER_VERTICAL);

        ActionEx forwardSearch = parent.getController().getOrCreateAction(R.id.actions_doSearch);
        ActionEx backwardSearch = parent.getController().getOrCreateAction(R.id.actions_doSearchBack);

        forwardSearch.addParameter(new EditableValue("input", m_edit)).addParameter(new Constant("forward", "true"));
        backwardSearch.addParameter(new EditableValue("input", m_edit)).addParameter(new Constant("forward", "false"));

        m_prevButton.setImageResource(R.drawable.arrowleft);
        m_prevButton.setOnClickListener(backwardSearch);

        m_nextButton.setImageResource(R.drawable.arrowright);
        m_nextButton.setOnClickListener(forwardSearch);
        m_edit.setOnEditorActionListener(forwardSearch);

        m_line.addView(m_prevButton);
        m_line.addView(m_edit);
        m_line.addView(m_nextButton);

        addView(m_line);
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
    
    public int getActualHeight() {
        return m_line.getHeight();
    }
}
