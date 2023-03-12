package marcel.lang.android.dex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class DexUtils {

    public static boolean isDexJar(File jarFile) {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(jarFile.toPath(), StandardOpenOption.READ))) {
            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                System.out.println(entry.getName());
                if ("classes.dex".equals(entry.getName())) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
