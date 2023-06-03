package com.tambapps.marcel.dalvik.compiler;

import com.android.dx.command.dexer.DxContext;
import com.android.dx.command.dexer.Main;
import com.tambapps.marcel.dumbbell.DumbbellException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import marcel.lang.android.BuildConfig;

public class DexUtils {

    public static boolean isDexJar(File jarFile) {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(jarFile.toPath(), StandardOpenOption.READ))) {
            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                if ("classes.dex".equals(entry.getName())) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static void convertJarToDexFile(File jarFile, File dexOutputFile) throws IOException {
        // class that converts jar to dex

        if (!jarFile.exists()) {
            throw new IllegalArgumentException("The provided file doesn't exists");
        }
        if (!jarFile.isFile()) {
            throw new IllegalArgumentException("The provided file isn't a regular file");
        }
        if (dexOutputFile.isDirectory()) {
            dexOutputFile = new File(dexOutputFile, jarFile.getName());
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        DxContext context = new DxContext(outStream, errStream);
        Main.Arguments arguments = new Main.Arguments(context);
        arguments.parse(String.format(Locale.ENGLISH,
            "--min-sdk-version=%d --output=%s %s", BuildConfig.MIN_SDK_VERSION,
            dexOutputFile.getAbsolutePath(),
            jarFile.getAbsolutePath()).split("\\s"));
        int result = Main.run(arguments);
        if (result != 0) {
            String message = errStream.size() > 0 ? errStream.toString()
                // the no message case can happen when there is a jar with classes in format Java 9+. (e.g. META-INF/versions/9/module-info.class
                : "The provided jar isn't compatible with Android";
            throw new DexException(message);
        }
    }
}
