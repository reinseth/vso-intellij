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
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.ChangesBrowserSettingsEditor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.tfsIntegration.core.tfs.ItemPath;
import org.jetbrains.tfsIntegration.core.tfs.WorkspaceInfo;
import org.jetbrains.tfsIntegration.core.tfs.Workstation;
import org.jetbrains.tfsIntegration.core.tfs.version.ChangesetVersionSpec;
import org.jetbrains.tfsIntegration.core.tfs.version.LatestVersionSpec;
import org.jetbrains.tfsIntegration.exceptions.TfsException;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.Changeset;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.VersionSpec;
import org.jetbrains.tfsIntegration.ui.TFSVersionFilterComponent;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TFSCommittedChangesProvider implements CachingCommittedChangesProvider<TFSChangeList, ChangeBrowserSettings> {
  private final Project myProject;
  private final TFSVcs myVcs;

  public TFSCommittedChangesProvider(TFSVcs vcs, final Project project) {
    myProject = project;
    myVcs = vcs;
  }

  public TFSCommittedChangesProvider(final Project project) {
    myProject = project;
    myVcs = TFSVcs.getInstance(myProject);
  }

  public ChangeBrowserSettings createDefaultSettings() {
    return new ChangeBrowserSettings();
  }

  public ChangesBrowserSettingsEditor<ChangeBrowserSettings> createFilterUI(final boolean showDateFilter) {
    return new TFSVersionFilterComponent(showDateFilter);
  }

  @Nullable
  public RepositoryLocation getLocationFor(final FilePath root) {
    try {
      WorkspaceInfo workspace = Workstation.getInstance().findWorkspace(root);
      if (workspace != null) {
        String serverPath = workspace.findServerPathByLocalPath(root);
        return new TFSRepositoryLocation(new ItemPath(root, serverPath), workspace);
      }
      else {
        return null;
      }
    }
    catch (TfsException e) {
      AbstractVcsHelper.getInstance(myProject).showError(new VcsException(e.getMessage(), e), TFSVcs.TFS_NAME);
      return null;
    }
  }

  public List<TFSChangeList> getCommittedChanges(final ChangeBrowserSettings settings,
                                                 final RepositoryLocation location,
                                                 final int maxCount) throws VcsException {

    try {
      TFSRepositoryLocation tfsRepositoryLocation = (TFSRepositoryLocation)location;
      final WorkspaceInfo workspace = tfsRepositoryLocation.getWorkspace();
      // TODO: deletion id

      VersionSpec versionFrom =
        settings.getChangeAfterFilter() != null ? new ChangesetVersionSpec((int)settings.getChangeAfterFilter().longValue()) : null;
      VersionSpec versionTo = settings.getChangeBeforeFilter() != null
                              ? new ChangesetVersionSpec((int)settings.getChangeBeforeFilter().longValue())
                              : LatestVersionSpec.INSTANCE;

      List<Changeset> changeSets = workspace.getServer().getVCS().queryHistory(workspace.getName(), workspace.getOwnerName(),
                                                                               tfsRepositoryLocation.getItemPath(), Integer.MIN_VALUE,
                                                                               settings.getUserFilter(), versionFrom, versionTo, maxCount);
      List<TFSChangeList> result = new ArrayList<TFSChangeList>(changeSets.size());
      for (Changeset changeset : changeSets) {
        result.add(new TFSChangeList(tfsRepositoryLocation, changeset.getCset(), changeset.getOwner(), changeset.getDate().getTime(),
                                     changeset.getComment(), myVcs));
      }
      return result;
    }
    catch (Exception e) {
      throw new VcsException(e);
    }
  }

  public ChangeListColumn[] getColumns() {
    return new ChangeListColumn[]{new ChangeListColumn.ChangeListNumberColumn("Revision"), ChangeListColumn.NAME, ChangeListColumn.DATE,
      ChangeListColumn.DESCRIPTION};
  }

  public int getFormatVersion() {
    return 1;
  }

  public void writeChangeList(final DataOutput stream, final TFSChangeList list) throws IOException {
    list.writeToStream(stream);
  }

  public TFSChangeList readChangeList(final RepositoryLocation location, final DataInput stream) throws IOException {
    return new TFSChangeList(myVcs, stream);
  }

  public boolean isMaxCountSupported() {
    return true;
  }

  public Collection<FilePath> getIncomingFiles(final RepositoryLocation location) throws VcsException {
    return null;
  }

  public boolean refreshCacheByNumber() {
    return true;
  }

  public String getChangelistTitle() {
    return "Changelist";
  }

  public boolean isChangeLocallyAvailable(final FilePath filePath,
                                          @Nullable final VcsRevisionNumber localRevision,
                                          final VcsRevisionNumber changeRevision,
                                          final TFSChangeList changeList) {
    // TODO?
    return localRevision != null && localRevision.compareTo(changeRevision) >= 0;
  }

  public boolean refreshIncomingWithCommitted() {
    return false;
  }
}
