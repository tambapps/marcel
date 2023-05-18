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
 * Compiles the test sources.
 * Note that this mojo requires Marcel &gt;= 1.5.0, and &gt;= 2.0.0-beta-3 (the indy version) for compiling with invokedynamic option.
 *
 * @author Keegan Witt
 * @since 1.0-beta-1
 */
@Mojo(name = "compileTests", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class CompileTestsMojo extends AbstractCompileMojo {

    /**
     * The Marcel test source files (relative paths).
     * Default: "${project.basedir}/src/test/marcel/&#42;&#42;/&#42;.marcel"
     */
    @Parameter
    protected FileSet[] testSources;

    /**
     * The location for the compiled test classes.
     */
    @Parameter(defaultValue = "${project.build.testOutputDirectory}")
    protected File testOutputDirectory;

    /**
     * Flag to allow test compilation to be skipped.
     */
    @Parameter(property = "maven.test.skip", defaultValue = "false")
    protected boolean skipTests;

    /**
     * Executes this mojo.
     *
     * @throws MojoExecutionException If an unexpected problem occurs (causes a "BUILD ERROR" message to be displayed)
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (!skipTests) {
            try {
                try {
                    getLog().debug("Project test classpath:\n" + project.getTestClasspathElements());
                } catch (DependencyResolutionRequiredException e) {
                    getLog().debug("Unable to log project test classpath");
                }
                doCompile(getTestFiles(testSources, false), project.getTestClasspathElements(), testOutputDirectory);
            } catch (IOException e) {
                throw new MojoExecutionException("An unexpected error occurred", e);
            } catch (MarcelLexerException | MarcelParserException | MarcelSemanticException | MarcelCompilerException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (DependencyResolutionRequiredException e) {
                throw new MojoExecutionException("Compile dependencies weren't resolved.", e);
            }
        } else {
            getLog().info("Compilation of tests is skipped.");
        }
    }

}
