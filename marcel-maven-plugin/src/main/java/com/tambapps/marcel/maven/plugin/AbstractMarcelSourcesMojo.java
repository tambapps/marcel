/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tambapps.marcel.maven.plugin;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.singletonList;


/**
 * This mojo provides access to the Marcel sources.
 *
 * @author Keegan Witt
 * @since 1.0-beta-2
 */
public abstract class AbstractMarcelSourcesMojo extends AbstractMarcelMojo {

    /**
     * Main source directory name.
     */
    protected static final String MAIN = "main";

    /**
     * Test source directory name.
     */
    protected static final String TEST = "test";

    /**
     * Gets the set of included files from the specified source files or source directory (if sources are null).
     *
     * @param fromSources        The sources to get the included files from
     * @param includeJavaSources Whether to include Java sources
     * @return The included files from the specified sources
     */
    protected SortedSet<File> getFiles(final FileSet[] fromSources, final boolean includeJavaSources) {
        SortedSet<File> files = new TreeSet<>();
        FileSetManager fileSetManager = new FileSetManager(getLog());

        for (FileSet fileSet : getFilesets(fromSources, includeJavaSources)) {
            for (String include : fileSetManager.getIncludedFiles(fileSet)) {
                files.add(new File(fileSet.getDirectory(), include));
            }
        }

        return files;
    }

    /**
     * Gets the set of included files from the specified source files or source directory (if sources are null).
     *
     * @param fromSources        The sources to get the included files from
     * @param includeJavaSources Whether to include Java sources
     * @return The included files from the specified sources
     */
    protected SortedSet<File> getTestFiles(final FileSet[] fromSources, final boolean includeJavaSources) {
        SortedSet<File> files = new TreeSet<>();
        FileSetManager fileSetManager = new FileSetManager(getLog());

        for (FileSet fileSet : getTestFilesets(fromSources, includeJavaSources)) {
            for (String include : fileSetManager.getIncludedFiles(fileSet)) {
                files.add(new File(fileSet.getDirectory(), include));
            }
        }

        return files;
    }

    /**
     * Gets the set of included filesets from the specified source files or source directory (if sources are null).
     *
     * @param fromSources        The sources to get the included files from
     * @param includeJavaSources Whether to include Java sources
     * @return The included filesets from the specified sources
     */
    protected FileSet[] getFilesets(final FileSet[] fromSources, final boolean includeJavaSources) {
        FileSet[] result;
        FileSet[] marcelFileSets;

        if (fromSources != null) {
            marcelFileSets = fromSources;
        } else {
            FileSet marcelFileSet = new FileSet();
            String marcelDirectory = "src" + File.separator + MAIN + File.separator + "marcel";
            marcelFileSet.setDirectory(project.getBasedir() + File.separator + marcelDirectory);
            marcelFileSet.setIncludes(MARCEL_SOURCES_PATTERNS);
            marcelFileSets = new FileSet[]{marcelFileSet};
        }

        if (includeJavaSources) {
            List<FileSet> javaFileSets = new ArrayList<>();
            for (String sourceRoot : project.getCompileSourceRoots()) {
                FileSet javaFileSet = new FileSet();
                javaFileSet.setDirectory(sourceRoot);
                javaFileSet.setIncludes(singletonList(JAVA_SOURCES_PATTERN));
                javaFileSets.add(javaFileSet);
            }
            FileSet[] javaFileSetsArr = javaFileSets.toArray(new FileSet[0]);
            result = Arrays.copyOf(marcelFileSets, marcelFileSets.length + javaFileSetsArr.length);
            System.arraycopy(javaFileSetsArr, 0, result, marcelFileSets.length, javaFileSetsArr.length);
        } else {
            result = marcelFileSets;
        }

        return result;
    }

    /**
     * Gets the set of included filesets from the specified source files or source directory (if sources are null).
     *
     * @param fromSources        The sources to get the included files from
     * @param includeJavaSources Whether to include Java sources
     * @return The included filesets from the specified sources
     */
    protected FileSet[] getTestFilesets(final FileSet[] fromSources, final boolean includeJavaSources) {
        FileSet[] result;
        FileSet[] marcelFileSets;

        if (fromSources != null) {
            marcelFileSets = fromSources;
        } else {
            FileSet marcelFileSet = new FileSet();
            String marcelDirectory = "src" + File.separator + TEST + File.separator + "marcel";
            marcelFileSet.setDirectory(project.getBasedir() + File.separator + marcelDirectory);
            marcelFileSet.setIncludes(MARCEL_SOURCES_PATTERNS);
            marcelFileSets = new FileSet[]{marcelFileSet};
        }

        if (includeJavaSources) {
            List<FileSet> javaFileSets = new ArrayList<>();
            for (String sourceRoot : project.getTestCompileSourceRoots()) {
                FileSet javaFileSet = new FileSet();
                javaFileSet.setDirectory(sourceRoot);
                javaFileSet.setIncludes(singletonList(JAVA_SOURCES_PATTERN));
                javaFileSets.add(javaFileSet);
            }
            FileSet[] javaFileSetsArr = javaFileSets.toArray(new FileSet[0]);
            result = Arrays.copyOf(marcelFileSets, marcelFileSets.length + javaFileSetsArr.length);
            System.arraycopy(javaFileSetsArr, 0, result, marcelFileSets.length, javaFileSetsArr.length);
        } else {
            result = marcelFileSets;
        }

        return result;
    }
}
