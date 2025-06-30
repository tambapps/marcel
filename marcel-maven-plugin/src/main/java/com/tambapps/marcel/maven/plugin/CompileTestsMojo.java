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

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;
import org.slf4j.Logger;

import java.io.File;

/**
 * Compiles the test sources.
 * Note that this mojo requires Marcel &gt;= 1.5.0, and &gt;= 2.0.0-beta-3 (the indy version) for compiling with invokedynamic option.
 *
 * @author Keegan Witt
 * @since 1.0-beta-1
 */
@Slf4j
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
            doExecute(testSources, testOutputDirectory, "test");
        } else {
            getLog().info("Compilation of tests is skipped.");
        }
    }

    @Override
    Logger getLogger() {
        return log;
    }
}
