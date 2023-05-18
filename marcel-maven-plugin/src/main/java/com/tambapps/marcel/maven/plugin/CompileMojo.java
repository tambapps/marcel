/*
 * Copyright (C) 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tambapps.marcel.maven.plugin;

import com.tambapps.marcel.compiler.exception.MarcelCompilerException;
import com.tambapps.marcel.lexer.MarcelLexerException;
import com.tambapps.marcel.parser.exception.MarcelParserException;
import com.tambapps.marcel.parser.exception.MarcelSemanticException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;


/**
 * Compiles the main sources.
 * Note that this mojo requires Marcel &gt;= 1.5.0, and &gt;= 2.0.0-beta-3 (the indy version) for compiling with invokedynamic option.
 *
 * @author Keegan Witt
 * @since 1.0-beta-1
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class CompileMojo extends AbstractCompileMojo {

    /**
     * The Marcel source files (relative paths).
     * Default: "${project.basedir}/src/main/marcel/&#42;&#42;/&#42;.marcel"
     */
    @Parameter
    protected FileSet[] sources;

    /**
     * The location for the compiled classes.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    protected File outputDirectory;

    /**
     * Executes this mojo.
     *
     * @throws MojoExecutionException If an unexpected problem occurs (causes a "BUILD ERROR" message to be displayed)
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            try {
                getLog().debug("Project compile classpath:\n" + project.getCompileClasspathElements());
            } catch (DependencyResolutionRequiredException e) {
                getLog().debug("Unable to log project compile classpath");
            }
            doCompile(getFiles(sources, false), project.getCompileClasspathElements(), outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("An unexpected error occurred", e);
        } catch (MarcelLexerException | MarcelParserException | MarcelSemanticException | MarcelCompilerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Compile dependencies weren't resolved.", e);
        }
    }

}
