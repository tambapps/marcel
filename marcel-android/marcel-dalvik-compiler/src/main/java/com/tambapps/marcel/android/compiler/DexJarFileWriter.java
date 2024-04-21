package com.tambapps.marcel.android.compiler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * JarWriter that makes the input file read-only at the end. This is needed by Android, otherwise we
 * get the exception
 * SecurityException: Writable dex file is not allowed.
 */
public class DexJarFileWriter extends DexJarWriter {

    private final File file;
    public DexJarFileWriter(File file, OutputStream fos) {
        super(fos);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (!file.setReadOnly()) {
            throw new IOException("Couldn't make the file read only");
        }
    }
}
