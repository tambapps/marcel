package com.tambapps.marcel.maven.plugin;

import com.tambapps.marcel.compiler.CompilerConfiguration;
import com.tambapps.marcel.compiler.MarcelCompiler;
import com.tambapps.marcel.compiler.exception.MarcelCompilerException;
import com.tambapps.marcel.lexer.MarcelLexerException;
import com.tambapps.marcel.parser.exception.MarcelParserException;
import com.tambapps.marcel.parser.exception.MarcelSemanticException;
import marcel.lang.MarcelClassLoader;
import marcel.lang.URLMarcelClassLoader;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The base compile mojo, which all compile mojos extend.
 *
 * @author Keegan Witt
 * @since 1.0-beta-1
 */
public abstract class AbstractCompileMojo extends AbstractMarcelSourcesMojo {


    // TODO handle this argument
    @Parameter(property = "maven.compiler.target", defaultValue = "1.8")
    protected String targetBytecode;



    /**
     * Performs compilation of compile mojos.
     *
     * @param sources                the sources to compile
     * @param classpath              the classpath to use for compilation
     * @param compileOutputDirectory the directory to write the compiled class files to
     * @throws ClassNotFoundException    when a class needed for compilation cannot be found
     * @throws InstantiationException    when a class needed for compilation cannot be instantiated
     * @throws IllegalAccessException    when a method needed for compilation cannot be accessed
     * @throws InvocationTargetException when a reflection invocation needed for compilation cannot be completed
     * @throws MalformedURLException     when a classpath element provides a malformed URL
     */
    @SuppressWarnings({"rawtypes"})
    protected synchronized void doCompile(final Set<File> sources, final List<String> classpath, final File compileOutputDirectory)
            throws IOException, MarcelLexerException, MarcelParserException, MarcelSemanticException, MarcelCompilerException {
        if (sources == null || sources.isEmpty()) {
            getLog().info("No sources specified for compilation. Skipping.");
            return;
        }
        MarcelClassLoader marcelClassLoader = new URLMarcelClassLoader(getClass().getClassLoader());
        for (String path : classpath) {
            marcelClassLoader.addLibraryJar(new File(path));
        }


        CompilerConfiguration configuration = new CompilerConfiguration();
        MarcelCompiler compiler = new MarcelCompiler(configuration);

        AtomicInteger classesCount = new AtomicInteger();
        compiler.compile(marcelClassLoader, sources, (c) -> {
            String name = c.getClassName().replace('.', File.separatorChar) + ".class";
            File path = new File(compileOutputDirectory, name);
            // ensure the path is ready for the file
            File directory = path.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new MarcelCompilerException("Couldn't create directory " + directory);
            }
            try {
                Files.write(path.toPath(), c.getBytes());
            } catch (IOException e) {
                throw new MarcelCompilerException(e);
            }
            classesCount.incrementAndGet();
        });
        // log compiled classes
        getLog().info("Compiled " + classesCount.get() + " file" + (classesCount.get() != 1 ? "s" : "") + ".");
    }


    private static String translateJavacTargetToTargetBytecode(String targetBytecode) {
        Map<String, String> javacTargetToTargetBytecode = new HashMap<>();
        javacTargetToTargetBytecode.put("5", "1.5");
        javacTargetToTargetBytecode.put("6", "1.6");
        javacTargetToTargetBytecode.put("7", "1.7");
        javacTargetToTargetBytecode.put("8", "1.8");
        javacTargetToTargetBytecode.put("1.9", "9");
        return javacTargetToTargetBytecode.getOrDefault(targetBytecode, targetBytecode);
    }

}
