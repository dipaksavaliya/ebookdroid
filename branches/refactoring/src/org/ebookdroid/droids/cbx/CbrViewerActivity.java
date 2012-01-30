package org.ebookdroid.droids.cbx;

import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.DecodeServiceBase;
import org.ebookdroid.droids.cbx.codec.CbxArchiveFactory;
import org.ebookdroid.droids.cbx.codec.CbxContext;
import org.ebookdroid.ui.viewer.BaseViewerActivity;

import java.io.File;
import java.io.IOException;

import org.emdev.utils.archives.ArchiveFile;
import org.emdev.utils.archives.rar.RarArchive;
import org.emdev.utils.archives.rar.RarArchiveEntry;

public class CbrViewerActivity extends BaseViewerActivity implements CbxArchiveFactory<RarArchiveEntry> {

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.ui.viewer.BaseViewerActivity#createDecodeService()
     */
    @Override
    protected DecodeService createDecodeService() {
        return new DecodeServiceBase(new CbxContext<RarArchiveEntry>(this));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.droids.cbx.codec.CbxArchiveFactory#create(java.io.File, java.lang.String)
     */
    @Override
    public ArchiveFile<RarArchiveEntry> create(final File file, final String password) throws IOException {
        return new RarArchive(file);
    }

}
