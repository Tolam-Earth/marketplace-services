/*
 * Copyright 2022 Tolam Earth
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

package com.tolamearth.marketplace;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HemServices implements Plugin<Project> {
    public void apply(Project project) {
        //System.out.println("Applying HemServices to "  + project.getProjectDir().getAbsolutePath()); // TODO: add as a debug log
        if ("shared".equals(project.getProjectDir().getParentFile().getName())) {
            project.getPluginManager().apply("hem-services-lib");
        } else {
            project.getPluginManager().apply("hem-services-endpoint");
        }
    }
}