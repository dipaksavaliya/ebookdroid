package org.ebookdroid.droids.cbx.codec;

import org.ebookdroid.EBookDroidLibraryLoader;
import org.ebookdroid.core.codec.AbstractCodecContext;
import org.ebookdroid.core.codec.CodecDocument;

import java.io.File;
import java.io.IOException;

import org.emdev.utils.archives.ArchiveEntry;
import org.emdev.utils.archives.ArchiveFile;

public class CbxContext<ArchiveEntryType extends ArchiveEntry> extends AbstractCodecContext {

    static {
        EBookDroidLibraryLoader.load();
    }
    
    private final CbxArchiveFactory<ArchiveEntryType> factory;

    public CbxContext(final CbxArchiveFactory<ArchiveEntryType> factory) {
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.codec.CodecContext#openDocument(java.lang.String, java.lang.String)
     */
    @Override
    public CodecDocument openDocument(final String fileName, final String password) {
        try {
            final ArchiveFile<ArchiveEntryType> archive = factory.create(new File(fileName), password);
            return new CbxDocument<ArchiveEntryType>(this, archive);
        } catch (final IOException e) {
            if (CbxDocument.LCTX.isDebugEnabled()) {
                CbxDocument.LCTX.d("IO error: " + e.getMessage());
            }
            return new CbxDocument<ArchiveEntryType>(this, null);
        }
    }
}
