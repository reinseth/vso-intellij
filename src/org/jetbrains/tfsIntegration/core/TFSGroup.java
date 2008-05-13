/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.jetbrains.tfsIntegration.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.actions.StandardVcsGroup;

/**
 * Created by IntelliJ IDEA.
 * Date: 27.01.2008
 * Time: 17:25:40
 */
public class TFSGroup extends StandardVcsGroup {

  public AbstractVcs getVcs(Project project) {
    return TFSVcs.getInstance(project);
  }

  @Override
  public String getVcsName(final Project project) {
    return "TFS";
  }
}
