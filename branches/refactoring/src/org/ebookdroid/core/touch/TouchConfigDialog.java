package org.ebookdroid.core.touch;

import org.ebookdroid.R;
import org.ebookdroid.core.touch.TouchManager.Region;
import org.ebookdroid.core.touch.TouchManager.TouchProfile;
import org.ebookdroid.ui.viewer.IActivityController;

import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.emdev.ui.actions.ActionEx;
import org.emdev.ui.actions.ActionMethod;
import org.emdev.ui.actions.DialogController;

public class TouchConfigDialog extends Dialog {

    private final DialogController<TouchConfigDialog> actions;

    private final TouchProfile profile;
    private final ArrayAdapter<Region> adapter;
    private Region region;

    public TouchConfigDialog(final IActivityController base, TouchProfile profile, Region region) {
        super(base.getContext());
        this.profile = profile;
        this.region = region;
        this.actions = new DialogController<TouchConfigDialog>(base.getActivity(), this);

        setTitle("Tap configuration");
        setContentView(R.layout.tap_zones_config);

        adapter = new ArrayAdapter<Region>(base.getContext(), android.R.layout.simple_list_item_1, profile.regions);

        Spinner regionsList = (Spinner) this.findViewById(R.id.tapZonesConfigRegions);
        regionsList.setAdapter(adapter);
        regionsList.setSelection(profile.regions.indexOf(region));
        regionsList.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TouchConfigDialog.this.region = TouchConfigDialog.this.profile.regions.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                TouchConfigDialog.this.region = null;
            }
        });
        
        actions.connectViewToAction(R.id.tapZonesConfigClear);
        actions.connectViewToAction(R.id.tapZonesConfigDelete);
        actions.connectViewToAction(R.id.tapZonesConfigReset);
    }

    @ActionMethod(ids = R.id.tapZonesConfigDelete)
    public void deleteRegion(ActionEx action) {
        if (region != null && profile.regions.remove(region)) {
            adapter.notifyDataSetChanged();
            region = null;
        }
    }
}
