package org.ebookdroid.droids.djvu;

import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.DecodeServiceBase;
import org.ebookdroid.droids.djvu.codec.DjvuContext;
import org.ebookdroid.ui.viewer.BaseViewerActivity;

public class DjvuViewerActivity extends BaseViewerActivity {

    @Override
    protected DecodeService createDecodeService() {
        return new DecodeServiceBase(new DjvuContext());
    }
}
