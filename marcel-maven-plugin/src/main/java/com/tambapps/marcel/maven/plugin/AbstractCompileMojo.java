package com.tambapps.marcel.maven.plugin;

import com.tambapps.marcel.compiler.CompilerConfiguration;
import com.tambapps.marcel.compiler.MarcelCompiler;
import com.tambapps.marcel.compiler.exception.MarcelCompilerException;
import com.tambapps.marcel.lexer.MarcelLexerException;
import com.tambapps.marcel.parser.MarcelParserException;
import com.tambapps.marcel.semantic.analysis.SemanticConfiguration;
import com.tambapps.marcel.semantic.exception.MarcelSemanticException;
import marcel.lang.MarcelClassLoader;
import marcel.lang.URLMarcelClassLoader;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The base compile mojo, which all compile mojos extend.
 *
 * @author Keegan Witt
 * @since 1.0-beta-1
 */
public abstract class AbstractCompileMojo extends AbstractMarcelSourcesMojo {

    @Parameter(property = "maven.compiler.target", defaultValue = "1.8")
    protected String targetBytecode;


    public void doExecute(FileSet[] sources, File outputDirectory, String phase) throws MojoExecutionException {
        try {
            try {
                getLog().debug("Project " + phase + " classpath:\n" + project.getCompileClasspathElements());
            } catch (DependencyResolutionRequiredException e) {
                getLog().debug("Unable to log project " + phase + " classpath");
            }
            compile(getFiles(sources, false), project.getCompileClasspathElements(), outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("An unexpected error occurred", e);
        } catch (MarcelLexerException | MarcelParserException | MarcelSemanticException | MarcelCompilerException e) {
            // TODO add fileName on parser (and Lexer?)
            if (e instanceof MarcelSemanticException mse && mse.getFileName() != null) {
                throw new MojoExecutionException(mse.getFileName() + ": " + mse.getMessage(), e);
            }
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(phase + " dependencies weren't resolved.", e);
        }
    }
    /**
     * Performs compilation of compile mojos.
     *
     * @param sources                the sources to compile
     * @param classpath              the classpath to use for compilation
     * @param compileOutputDirectory the directory to write the compiled class files to
     * @throws MalformedURLException     when a classpath element provides a malformed URL
     */
    @SuppressWarnings({"rawtypes"})
    private synchronized void compile(final Set<File> sources, final List<String> classpath, final File compileOutputDirectory)
            throws IOException, MarcelLexerException, MarcelParserException, MarcelSemanticException, MarcelCompilerException {
        if (sources == null || sources.isEmpty()) {
            getLog().info("No sources specified for compilation. Skipping.");
            return;
        }
        MarcelClassLoader marcelClassLoader = new URLMarcelClassLoader(getClass().getClassLoader());
        for (String path : classpath) {
            marcelClassLoader.addJar(new File(path));
        }

        int classVersion = CompilerConfiguration.computeClassVersion();
        if (targetBytecode != null) {
            Integer version = CompilerConfiguration.getVERSION_MAP().get(targetBytecode);
            if (version == null) {
                throw new MarcelCompilerException(String.format("Version %d is not handled", targetBytecode));
            }
            classVersion = version;
        }
        CompilerConfiguration configuration = new CompilerConfiguration(new SemanticConfiguration(), classVersion, false);
        MarcelCompiler compiler = new MarcelCompiler(configuration);

        AtomicInteger classesCount = new AtomicInteger();
        compiler.compileFiles(sources, marcelClassLoader, (c) -> {
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
}
