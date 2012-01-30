package org.ebookdroid.droids.cbx;

import org.ebookdroid.core.DecodeService;
import org.ebookdroid.core.DecodeServiceBase;
import org.ebookdroid.droids.cbx.codec.CbxArchiveFactory;
import org.ebookdroid.droids.cbx.codec.CbxContext;
import org.ebookdroid.ui.viewer.BaseViewerActivity;

import java.io.File;
import java.io.IOException;

import org.emdev.utils.archives.ArchiveFile;
import org.emdev.utils.archives.zip.ZipArchive;
import org.emdev.utils.archives.zip.ZipArchiveEntry;

public class CbzViewerActivity extends BaseViewerActivity implements CbxArchiveFactory<ZipArchiveEntry> {

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.ui.viewer.BaseViewerActivity#createDecodeService()
     */
    @Override
    protected DecodeService createDecodeService() {
        return new DecodeServiceBase(new CbxContext<ZipArchiveEntry>(this));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.droids.cbx.codec.CbxArchiveFactory#create(java.io.File, java.lang.String)
     */
    @Override
    public ArchiveFile<ZipArchiveEntry> create(final File file, final String password) throws IOException {
        return new ZipArchive(file);
    }

}
