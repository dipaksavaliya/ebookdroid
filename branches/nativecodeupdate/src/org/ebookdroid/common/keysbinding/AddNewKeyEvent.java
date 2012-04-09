package org.ebookdroid.common.keysbinding;

import org.ebookdroid.R;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.emdev.ui.adapters.ActionsAdapter;


public class AddNewKeyEvent extends Dialog {

    AddNewKeyEventResult result;
    final TextView keyText;
    Integer key = null;
    final Integer oldkey;
        
    public AddNewKeyEvent(Context context, Integer keycode, String action) {
        super(context);
        setContentView(R.layout.keybinding_event);
       
        final ActionsAdapter actionsAdapter = new ActionsAdapter(getContext(), R.array.list_actions_ids, R.array.list_actions_labels);

        final Spinner actions = (Spinner) findViewById(R.id.keybinding_addnew_actions);
        actions.setAdapter(actionsAdapter);
        
        keyText = (TextView) findViewById(R.id.keybinding_addnew_key);
        
        final Button saveAction = (Button) findViewById(R.id.keybinding_addnew_save);
        saveAction.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    if( result != null ){

                        final String action = actionsAdapter.getActionId(actions.getSelectedItemPosition());
                        result.save(oldkey, key, action);     
                    }
                    dismiss();
                }
            });
        
        final Button deleteAction = (Button) findViewById(R.id.keybinding_addnew_delete);
        deleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    if( result != null ){
                        result.del(oldkey, key);     
                    }
                    dismiss();
                }
            });
        
        final Button cancelAction = (Button) findViewById(R.id.keybinding_addnew_cancel);
        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        
        if(keycode == null || action == null)
        {
            setTitle("New Key Event");
            
            oldkey = null;
            deleteAction.setVisibility(View.INVISIBLE);
        }
        else
        {
            setTitle("Edit Key Event");
            oldkey = keycode;
            key = keycode;
            final int position = actionsAdapter.getPosition(action);
            actions.setSelection(position);
            keyText.setText(KeyBindingsManager.keyCodeToString(key)+ " [" + key + "]");
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU || oldkey != null) // blacklist menu key
            return super.onKeyUp(keyCode, event);
        key = keyCode;
        keyText.setText(KeyBindingsManager.keyCodeToString(key)+ " [" + key + "]");
        return true;//super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }
    
    public void setDialogResult(AddNewKeyEventResult dialogResult){
        result = dialogResult;
    }

    
    public interface AddNewKeyEventResult{
        public void save(Integer oldkey, Integer key, String action);
        public void del(Integer oldkey, Integer key);
     }
}
