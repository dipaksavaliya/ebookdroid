package org.ebookdroid.droids.pdf;

import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.DecodeServiceBase;
import org.ebookdroid.droids.pdf.codec.PdfContext;
import org.ebookdroid.ui.viewer.BaseViewerActivity;

public class PdfViewerActivity extends BaseViewerActivity {

    @Override
    protected DecodeService createDecodeService() {
        return new DecodeServiceBase(new PdfContext());
    }
}
