package org.ebookdroid.common.keysbinding;

import org.ebookdroid.R;
import org.ebookdroid.common.keysbinding.AddNewKeyEvent.AddNewKeyEventResult;
import org.ebookdroid.common.keysbinding.KeyBindingsManager.ActionRef;
import org.ebookdroid.ui.viewer.IActivityController;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.emdev.ui.actions.ActionEx;
import org.emdev.utils.LayoutUtils;
import org.emdev.utils.collections.SparseArrayEx;


public class NewKeyBindingsDialog extends Dialog {
    
    final BindingAdapter bindings;

    public NewKeyBindingsDialog(final IActivityController base) {
        super(base.getContext());

        setTitle("Keys binding");

        setContentView(R.layout.keybinding_maindialog);

        final ListView list = (ListView) findViewById(R.id.keybinding_main_eventslist);
        bindings = new BindingAdapter();
        bindings.setActions(KeyBindingsManager.getActions());
        list.setAdapter(bindings);
        
        list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                KeyBindingsManager.ActionRef act = (ActionRef) bindings.getItem(position);
                AddNewKeyEvent dialog = new AddNewKeyEvent(getContext(),act.code, act.name);
                dialog.setDialogResult(new AddNewKeyEventResult() {

                    public void save(Integer key, String action)
                    {
                        final Integer actionId = action != null ? ActionEx.getActionId(action) : null;
                        Log.i("Test", "Action selected " + action + "  " + actionId);
                        if(key != null && action != null)
                        {
                            KeyBindingsManager.addAction(actionId, key);
                            bindings.setActions(KeyBindingsManager.getActions());
                        }
                    }
                    
                    public void del(Integer oldkey, Integer key)
                    {
                        Log.i("Test", "Delete key" + key);
                        if(key != null)
                        {
                            KeyBindingsManager.removeAction(key);
                            bindings.setActions(KeyBindingsManager.getActions());
                        }
                        
                    }
                });
                dialog.show();
            }

          });

        Button button1main = (Button) findViewById(R.id.keybinding_main_add);
        button1main.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AddNewKeyEvent dialog = new AddNewKeyEvent(getContext(), null, null);
                dialog.setDialogResult(new AddNewKeyEventResult() {

                    public void save(Integer key, String action)
                    {
                        final Integer actionId = action != null ? ActionEx.getActionId(action) : null;
                        Log.i("Test", "Action selected " + action + "  " + actionId);
                        if(key != null && action != null)
                        {
                            KeyBindingsManager.addAction(actionId, key);
                            bindings.setActions(KeyBindingsManager.getActions());
                        }
                    }
                    
                    public void del(Integer oldkey, Integer key)
                    {
                        if(key != null)
                        {
                            KeyBindingsManager.removeAction(key);
                            bindings.setActions(KeyBindingsManager.getActions());
                        }
                        
                    }
                });
                dialog.show();
            }
        });

    }

    
 
    @Override
    protected void onStart() {
        LayoutUtils.maximizeWindow(getWindow());
    }

    @Override
    protected void onStop() {
        super.onStop();
        KeyBindingsManager.persist();
    }
    
    private class BindingAdapter extends BaseAdapter {

        private SparseArrayEx<KeyBindingsManager.ActionRef> items;

        public BindingAdapter() {
        }
        
        public void setActions(SparseArrayEx<KeyBindingsManager.ActionRef> items) {
            this.items = items;
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.valueAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.keybinding_actionitem, parent, false);
            }
            KeyBindingsManager.ActionRef action = items.valueAt(position);
            TextView keyText = (TextView) convertView.findViewById(R.id.keybinding_item_key);
            TextView actionText = (TextView) convertView.findViewById(R.id.keybinding_item_action);
            keyText.setText(KeyBindingsManager.keyCodeToString(action.code) + "[" + action.code + "]");                            
            actionText.setText(action.name);
            return convertView;
        }
}

}
