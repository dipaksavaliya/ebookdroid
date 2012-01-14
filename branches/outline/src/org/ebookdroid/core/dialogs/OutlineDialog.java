package org.ebookdroid.core.dialogs;

import org.ebookdroid.R;
import org.ebookdroid.core.IViewerActivity;
import org.ebookdroid.core.OutlineLink;
import org.ebookdroid.core.actions.ActionController;
import org.ebookdroid.core.presentation.OutlineAdapter;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

public class OutlineDialog extends Dialog {

    final IViewerActivity base;
    final OutlineAdapter adapter;
    final ActionController<OutlineDialog> actions;

    public OutlineDialog(final IViewerActivity base, final OutlineAdapter adapter) {
        super(base.getContext());
        this.base = base;
        this.adapter = adapter;
        this.actions = new ActionController<OutlineDialog>(base.getActivity(), this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Outline");
        setContentView(R.layout.outline);

        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        final ExpandableListView tree = (ExpandableListView) findViewById(R.id.outline_tree);
        tree.setAdapter(new OutlineTreeAdapter(adapter));
        tree.setChoiceMode(tree.CHOICE_MODE_NONE);
        tree.setChildIndicatorBounds(0, 1);
    }

    public static class OutlineTreeAdapter extends BaseExpandableListAdapter {

        private final OutlineAdapter adapter;
        private final OutlineAdapter[] children;

        private OutlineTreeAdapter(final OutlineAdapter adapter) {
            this.adapter = adapter;
            this.children = new OutlineAdapter[adapter.getCount()];
        }

        @Override
        public boolean isChildSelectable(final int groupPosition, final int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView,
                final ViewGroup parent) {
            return adapter.getView(groupPosition, convertView, parent);
        }

        @Override
        public long getGroupId(final int groupPosition) {
            return groupPosition;
        }

        @Override
        public int getGroupCount() {
            return adapter.getCount();
        }

        @Override
        public OutlineLink getGroup(final int groupPosition) {
            return adapter.getItem(groupPosition);
        }

        @Override
        public int getChildrenCount(final int groupPosition) {
            return getGroup(groupPosition).children.isEmpty() ? 0 : 1;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild,
                final View convertView, final ViewGroup parent) {
            View container = null;
            if (convertView == null) {
                container = LayoutInflater.from(adapter.getContext()).inflate(R.layout.outline_inner, parent, false);
            } else {
                container = convertView;
            }

            final ExpandableListView tree = (ExpandableListView) container.findViewById(R.id.outline_inner);
            tree.setAdapter(new OutlineTreeAdapter(getChild(groupPosition, childPosition)));
            tree.setChoiceMode(tree.CHOICE_MODE_NONE);
            tree.setChildIndicatorBounds(0, 1);
            return container;
        }

        @Override
        public long getChildId(final int groupPosition, final int childPosition) {
            return childPosition;
        }

        @Override
        public OutlineAdapter getChild(final int groupPosition, final int childPosition) {
            if (children[groupPosition] == null) {
                children[groupPosition] = new OutlineAdapter(adapter.getContext(), getGroup(groupPosition).children);
            }
            return children[groupPosition];
        }
    }
}
