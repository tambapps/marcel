package com.tambapps.marcel.android.app.marcel.shell.jar;

import androidx.annotation.NonNull;

import com.tambapps.marcel.compiler.JarWriter;
import com.tambapps.marcel.repl.jar.JarWriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import marcel.lang.android.dex.DexJarWriter;

public class DexJarWriterFactory implements JarWriterFactory {
    @NonNull
    @Override
    public JarWriter newJarWriter(@NonNull OutputStream outputStream) throws IOException {
        return new DexJarWriter(outputStream);
    }

    @NonNull
    @Override
    public JarWriter newJarWriter(@NonNull File file) throws IOException {
        return newJarWriter(Files.newOutputStream(file.toPath()));
    }
}
