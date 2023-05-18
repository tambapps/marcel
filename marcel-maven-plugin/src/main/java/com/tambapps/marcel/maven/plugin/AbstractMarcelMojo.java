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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Arrays;
import java.util.List;



/**
 * The base mojo class, which all other mojos extend.
 *
 * @author Keegan Witt
 * @since 1.0-beta-1
 */
public abstract class AbstractMarcelMojo extends AbstractMojo {

    /**
     * The pattern defining Marcel files.
     */
    protected static final List<String> MARCEL_SOURCES_PATTERNS = Arrays.asList(
        "**" + File.separator + "*.mcl",
        "**" + File.separator + "*.marcel"
    );

    /**
     * The pattern defining Java stub files.
     */
    protected static final String JAVA_SOURCES_PATTERN = "**" + File.separator + "*.java";


    // note that all supported parameter expressions can be found here: https://git-wip-us.apache.org/repos/asf?p=maven.git;a=blob;f=maven-core/src/main/java/org/apache/maven/plugin/PluginParameterExpressionEvaluator.java;hb=HEAD

    /**
     * The Maven project this plugin is being used on.
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    /**
     * The Maven Session this plugin is being used on.
     */
    @Parameter(property = "session", required = true, readonly = true)
    protected MavenSession session;

    /**
     * The plugin dependencies.
     */
    @Parameter(property = "plugin.artifacts", required = true, readonly = true)
    protected List<Artifact> pluginArtifacts;

    /**
     * The plugin's mojo execution.
     */
    @Parameter(property = "mojoExecution", required = true, readonly = true)
    protected MojoExecution mojoExecution;


}
