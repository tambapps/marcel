package marcel.lang.android.dex;

import com.android.dx.Version;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class DexJarWriter implements Closeable {


    private static final String DEX_IN_JAR_NAME = "classes.dex";
    public static final String CLASSES_NAMES_ENTRY = "META-INF/classes.txt";
    private static final Attributes.Name CREATED_BY = new Attributes.Name("Created-By");

    private final JarOutputStream jar;

    public DexJarWriter(OutputStream fos) throws IOException {
        jar = new JarOutputStream(fos, makeManifest());
    }

    public void write(Collection<String> classNames, byte[] dalvikBytecode) throws IOException {
        JarEntry classes = new JarEntry(DEX_IN_JAR_NAME);
        classes.setSize(dalvikBytecode.length);
        jar.putNextEntry(classes);
        jar.write(dalvikBytecode);
        jar.closeEntry();

        // write classes names (can be useful if loading dex jar directly
        JarEntry classNamesEntry = new JarEntry(CLASSES_NAMES_ENTRY);
        jar.putNextEntry(classNamesEntry);
        jar.write(String.join("\n", classNames).getBytes(StandardCharsets.UTF_8));
        jar.closeEntry();

        jar.finish();
        jar.flush();
    }

    private static Manifest makeManifest() {
        Manifest manifest = new Manifest();
        Attributes attribs = manifest.getMainAttributes();
        attribs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attribs.put(CREATED_BY, "dx " + Version.VERSION);
        attribs.putValue("Dex-Location", DEX_IN_JAR_NAME);
        return manifest;
    }

    @Override
    public void close() throws IOException {
        jar.close();
    }
}
