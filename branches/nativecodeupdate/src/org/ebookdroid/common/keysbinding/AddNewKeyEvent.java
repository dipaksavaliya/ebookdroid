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
    
    public AddNewKeyEvent(Context context) {
        super(context);
        setTitle("Press Any Key.");
        final ActionsAdapter actionsAdapter = new ActionsAdapter(getContext(), R.array.list_actions_ids, R.array.list_actions_labels);
        setContentView(R.layout.keybinding_addaction);
        final Spinner actions = (Spinner) findViewById(R.id.keybinding_addnew_actions);
        actions.setAdapter(actionsAdapter);
        
        keyText = (TextView) findViewById(R.id.keybinding_addnew_key);
        
        Button addAction = (Button) findViewById(R.id.keybinding_addnew_add);
        addAction.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    if( result != null ){

                        final String action = actionsAdapter.getActionId(actions.getSelectedItemPosition());
                        result.finish(key, action);     
                    }
                    dismiss();
                }
            });
        
        Button cancelAction = (Button) findViewById(R.id.keybinding_addnew_cancel);
        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU) // blacklist menu key
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
        void finish(Integer key, String action);
     }
}
