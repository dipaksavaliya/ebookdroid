package org.ebookdroid.droids.fb2;

import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.DecodeServiceBase;
import org.ebookdroid.droids.fb2.codec.FB2Context;
import org.ebookdroid.ui.viewer.BaseViewerActivity;

public class FB2ViewerActivity extends BaseViewerActivity {

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.ui.viewer.BaseViewerActivity#createDecodeService()
     */
    @Override
    protected DecodeService createDecodeService() {
        return new DecodeServiceBase(new FB2Context());
    }
}
