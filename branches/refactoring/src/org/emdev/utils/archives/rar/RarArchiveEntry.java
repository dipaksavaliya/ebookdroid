package org.emdev.utils.archives.rar;


import java.io.IOException;
import java.io.InputStream;

import org.emdev.utils.archives.ArchiveEntry;

import de.innosystec.unrar.rarfile.FileHeader;

public class RarArchiveEntry implements ArchiveEntry {

    final RarArchive archive;
    final FileHeader fileHeader;

    RarArchiveEntry(final RarArchive archive, final FileHeader fh) {
        this.archive = archive;
        this.fileHeader = fh;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.utils.archives.ArchiveEntry#getName()
     */
    @Override
    public String getName() {
        return fileHeader.getFileNameString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.utils.archives.ArchiveEntry#isDirectory()
     */
    @Override
    public boolean isDirectory() {
        return fileHeader.isDirectory();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.utils.archives.ArchiveEntry#open()
     */
    @Override
    public InputStream open() throws IOException {
        return archive.open(this);
    }
}
