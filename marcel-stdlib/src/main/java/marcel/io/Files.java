package marcel.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

// TODO review File APIs
/**
 * Class providing util methods to handle files
 */
public class Files {

  public static File get(String path) {
    return new File(path);
  }

  public static File get(String parent, String child) {
    return new File(parent, child);
  }

  public static File get(File parent, String child) {
    return new File(parent, child);
  }

  public static String readText(String path) throws IOException {
    return readText(path, StandardCharsets.UTF_8);
  }

  public static String readText(String path, Charset charset) throws IOException {
    return readText(Paths.get(path), charset);
  }

  public static String readText(File file) throws IOException {
    return readText(file.toPath(), StandardCharsets.UTF_8);
  }

  public static String readText(File file, Charset charset) throws IOException {
    return readText(file.toPath(), charset);
  }

  public static String readText(Path path, Charset charset) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(java.nio.file.Files.newInputStream(path), charset))) {
      return reader.lines()
          .collect(Collectors.joining("\n"));
    }
  }

  public static byte[] readBytes(String path) throws IOException {
    return readBytes(Paths.get(path));
  }

  public static byte[] readBytes(Path path) throws IOException {
    return java.nio.file.Files.readAllBytes(path);
  }

  public static byte[] readBytes(File file) throws IOException {
    return readBytes(file.toPath());
  }

  public static List<String> readLines(String path) throws IOException {
    return readLines(Paths.get(path));
  }

  public static List<String> readLines(File file) throws IOException {
    return readLines(file.toPath());
  }

  public static List<String> readLines(Path path) throws IOException {
    return readLines(path, StandardCharsets.UTF_8);
  }

  public static List<String> readLines(Path path, Charset charset) throws IOException {
    try (BufferedReader reader = reader(path, charset)) {
      return reader.lines().collect(Collectors.toList());
    }
  }

  public static BufferedReader reader(String path) throws IOException {
    return reader(Paths.get(path));
  }

  public static BufferedReader reader(Path path) throws IOException {
    return reader(path, StandardCharsets.UTF_8);
  }

  public static BufferedReader reader(Path path, String charset) throws IOException {
    return reader(path, Charset.forName(charset));
  }

  public static BufferedReader reader(Path path, Charset charset) throws IOException {
    return new BufferedReader(
        new InputStreamReader(java.nio.file.Files.newInputStream(path), charset));
  }

  public static void write(String path, byte[] bytes) throws IOException {
    write(path, bytes, false);
  }

  public static void write(String path, byte[] bytes, boolean append) throws IOException {
    write(Paths.get(path), bytes, append);
  }

  public static void write(File file, byte[] bytes) throws IOException {
    write(file.toPath(), bytes, false);
  }

  public static void write(File file, byte[] bytes, boolean append) throws IOException {
    write(file.toPath(), bytes, append);
  }

  public static void write(Path path, byte[] bytes, boolean append) throws IOException {
    if (append) {
      java.nio.file.Files.write(path, bytes, StandardOpenOption.APPEND);
    } else {
      java.nio.file.Files.write(path, bytes);
    }
  }

  public static void write(String path, String string) throws IOException {
    write(path, string, false);
  }

  public static void write(String path, String string, boolean append) throws IOException {
    write(Paths.get(path), string, StandardCharsets.UTF_8, append);
  }

  public static void write(String path, String string, Charset charset) throws IOException {
    write(Paths.get(path), string, charset, false);
  }

  public static void write(String path, String string, Charset charset, boolean append) throws IOException {
    write(Paths.get(path), string, charset, append);
  }

  public static void write(Path path, String string, Charset charset, boolean append) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        append ? java.nio.file.Files.newOutputStream(path, StandardOpenOption.APPEND)
            : java.nio.file.Files.newOutputStream(path), charset))) {
      writer.write(string);
    }
  }

  public static void append(String path, String string) throws IOException {
    write(path, string, true);
  }

  public static void append(String path, String string, Charset charset) throws IOException {
    write(path, string, charset, true);
  }

  public static void append(Path path, String string, Charset charset) throws IOException {
    write(path, string, charset, true);
  }

  public static InputStream inputStream(String path) throws IOException {
    return java.nio.file.Files.newInputStream(Paths.get(path));
  }

  public static OutputStream outputStream(String path) throws IOException {
    return outputStream(path, false);
  }
  public static OutputStream outputStream(String path, boolean append) throws IOException {
    return outputStream(Paths.get(path), append);
  }
  public static OutputStream outputStream(Path path, boolean append) throws IOException {
    return append ? java.nio.file.Files.newOutputStream(path, StandardOpenOption.APPEND)
        : java.nio.file.Files.newOutputStream(path);
  }

  public static boolean exists(String path) {
    return exists(Paths.get(path));
  }

  public static boolean exists(Path path) {
    return java.nio.file.Files.exists(path);
  }
}
