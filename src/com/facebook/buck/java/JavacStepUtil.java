/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.java;

import static com.facebook.buck.java.JavacStep.SuggestBuildRules;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildDependencies;
import com.google.common.base.Optional;

import java.nio.file.Path;
import java.util.Set;

public class JavacStepUtil {

  private JavacStepUtil() {}

  public static JavacStep createJavacStep(
      Path outputDirectory,
      Set<Path> javaSourceFilePaths,
      Set<Path> transitiveClasspathEntries,
      Set<Path> declaredClasspathEntries,
      JavacOptions javacOptions,
      Optional<BuildTarget> invokingRule,
      BuildDependencies buildDependencies,
      Optional<SuggestBuildRules> suggestBuildRules,
      Optional<Path> pathToSrcsList,
      BuildTarget buildTarget,
      Optional<Path> workingDirectory) {

    Javac javac;
    if (javacOptions.getJavaCompilerEnvironment().getJavacPath().isPresent()) {
      javac = new ExternalJavac(
          javaSourceFilePaths,
          javacOptions,
          invokingRule,
          pathToSrcsList,
          buildTarget,
          workingDirectory);
    } else {
      javac = new Jsr199Javac(
          javaSourceFilePaths,
          invokingRule,
          pathToSrcsList);
    }

    return new JavacStep(
        javac,
        outputDirectory,
        javaSourceFilePaths,
        transitiveClasspathEntries,
        declaredClasspathEntries,
        javacOptions,
        invokingRule,
        buildDependencies,
        suggestBuildRules);
  }
}
