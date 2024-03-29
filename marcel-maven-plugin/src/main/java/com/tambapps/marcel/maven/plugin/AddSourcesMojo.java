/*
 * Copyright (C) 2012 the original author or authors.
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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.slf4j.Logger;


/**
 * This mojo adds Marcel sources to the project's sources.
 *
 * @author Keegan Witt
 * @since 1.0-beta-1
 */
@Slf4j
@Mojo(name = "addSources", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class AddSourcesMojo extends AbstractMarcelSourcesMojo {

    /**
     * The Marcel source files (relative paths).
     * Default: "${project.basedir}/src/main/marcel/&#42;&#42;/&#42;.marcel"
     */
    @Parameter
    protected FileSet[] sources;

    /**
     * Executes this mojo.
     */
    @Override
    public void execute() {
        for (FileSet source : getFilesets(sources, false)) {
            addSourcePath(source.getDirectory());
        }
    }

    /**
     * Adds the specified source path to the project's main compile sources.
     *
     * @param path The source path to add to the project's main compile sources
     */
    protected void addSourcePath(final String path) {
        if (!project.getCompileSourceRoots().contains(path)) {
            getLog().debug("Added source directory: " + path);
            project.addCompileSourceRoot(path);
        }
    }

    @Override
    Logger getLogger() {
        return log;
    }
}
