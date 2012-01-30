package org.ebookdroid.droids.xps;

import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.DecodeServiceBase;
import org.ebookdroid.droids.xps.codec.XpsContext;
import org.ebookdroid.ui.viewer.BaseViewerActivity;

public class XpsViewerActivity extends BaseViewerActivity {

    @Override
    protected DecodeService createDecodeService() {
        return new DecodeServiceBase(new XpsContext());
    }
}
