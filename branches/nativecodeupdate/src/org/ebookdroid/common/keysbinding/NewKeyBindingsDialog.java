package org.ebookdroid.common.keysbinding;

import org.ebookdroid.R;
import org.ebookdroid.common.keysbinding.AddNewKeyEvent.AddNewKeyEventResult;
import org.ebookdroid.common.keysbinding.KeyBindingsManager.ActionRef;
import org.ebookdroid.ui.viewer.IActivityController;

import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.DialogInterface;


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


    public NewKeyBindingsDialog(final IActivityController base) {
        super(base.getContext());

        setTitle("Keys binding");

        setContentView(R.layout.keybinding_maindialog);

        final ListView list = (ListView) findViewById(R.id.keybinding_main_eventslist);
        final BindingAdapter bindings = new BindingAdapter();
        bindings.setActions(KeyBindingsManager.getActions());
        list.setAdapter(bindings);
        
        list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want remove action?")
                       .setCancelable(false)
                       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                             KeyBindingsManager.ActionRef act = (ActionRef) bindings.getItem(position);
                             if(act != null)
                             {
                                 KeyBindingsManager.removeAction(act.code);
                                 bindings.setActions(KeyBindingsManager.getActions());
                             }
                           }
                       })
                       .setNegativeButton("No", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                           }
                       });
                AlertDialog alert = builder.create();
                alert.show();

            }

          });

        Button button1main = (Button) findViewById(R.id.keybinding_main_add);
        button1main.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AddNewKeyEvent dialog = new AddNewKeyEvent(getContext());
                dialog.setDialogResult(new AddNewKeyEventResult() {

                    public void finish(Integer key, String action) {
                        final Integer actionId = action != null ? ActionEx.getActionId(action) : null;
                        Log.i("Test", "Action selected " + action + "  " + actionId);
                        if(key != null && action != null)
                        {
                            KeyBindingsManager.addAction(actionId, key);
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
