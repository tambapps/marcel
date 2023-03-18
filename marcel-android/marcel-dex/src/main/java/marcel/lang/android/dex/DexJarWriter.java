package marcel.lang.android.dex;

import androidx.annotation.NonNull;

import com.android.dx.Version;
import com.tambapps.marcel.compiler.CompiledClass;
import com.tambapps.marcel.compiler.JarWriter;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

// TODO make it extends JarWriter and make a class JarWriterFactory to use on MarcelShell
public class DexJarWriter extends JarWriter implements Closeable {


    private static final String DEX_IN_JAR_NAME = "classes.dex";
    public static final String CLASSES_NAMES_ENTRY = "META-INF/classes.txt";
    private final List<CompiledClass> compiledClasses = new ArrayList<>();


    public DexJarWriter(OutputStream fos) {
        super(fos, makeManifest());
    }

    @Override
    public void writeClass(@NonNull CompiledClass compiledClass) {
        // will be actually written when closing
        compiledClasses.add(compiledClass);
    }

    private static Manifest makeManifest() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue("Dex-Version", Version.VERSION);
        attributes.putValue("Dex-Location", DEX_IN_JAR_NAME);
        return manifest;
    }

    private void flush() throws IOException {
        JarEntry classes = new JarEntry(DEX_IN_JAR_NAME);
        byte[] dalvikBytecode = generateDalvikBytecode();
        classes.setSize(dalvikBytecode.length);
        outputStream.putNextEntry(classes);
        outputStream.write(dalvikBytecode);
        outputStream.closeEntry();

        // write classes names (can be useful if loading dex jar directly
        JarEntry classNamesEntry = new JarEntry(CLASSES_NAMES_ENTRY);
        outputStream.putNextEntry(classNamesEntry);
        outputStream.write(
                compiledClasses.stream()
                        .map(CompiledClass::getClassName)
                        .collect(Collectors.joining("\n"))
                        .getBytes(StandardCharsets.UTF_8)
        );
        outputStream.closeEntry();
        outputStream.finish();
        outputStream.flush();
    }

    private byte[] generateDalvikBytecode() throws IOException {
        DexBytecodeTranslator translator = new DexBytecodeTranslator();
        for (CompiledClass compiledClass : compiledClasses) {
            translator.addClass(compiledClass.getClassName(), compiledClass.getBytes());
        }
        return translator.getDexBytes();
    }

    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }
}
