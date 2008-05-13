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

package org.jetbrains.tfsIntegration.ui;

import org.jetbrains.tfsIntegration.core.TFSVcs;
import org.jetbrains.tfsIntegration.core.tfs.WorkspaceInfo;
import org.jetbrains.tfsIntegration.exceptions.TfsException;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.VersionControlLabel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;


public class SelectLabelPanel {
    private JTextField nameTextField;
    private JTextField ownerTextField;
    private JButton findButton;
    private JTable labelsTable;
    private JPanel panel;

    private LabelsTableModel labelsTableModel;

    private enum Column {
      Name("Name") {
        public String getValue(VersionControlLabel label) {
          return label.getName();
        }
      },
      Scope("Scope") {
        public String getValue(VersionControlLabel label) {
          return label.getScope();
        }
      },
      Owner("Owner") {
        public String getValue(VersionControlLabel label) {
            return label.getOwner();
        }
      };

      private String myCaption;

      Column(String caption) {
        myCaption = caption;
      }

      public String getCaption() {
        return myCaption;
      }

      public abstract String getValue(VersionControlLabel label);

    }

    private static class LabelsTableModel extends AbstractTableModel {
      private List<VersionControlLabel> myLabels;

      public void setLabels(List<VersionControlLabel> labels) {
        myLabels = labels;
        fireTableDataChanged();
      }

      public List<VersionControlLabel> getLabels() {
        return myLabels;
      }

      public String getColumnName(final int column) {
        return Column.values()[column].getCaption();
      }

      public int getRowCount() {
        return myLabels != null ? myLabels.size() : 0;
      }

      public int getColumnCount() {
        return Column.values().length;
      }

      public Object getValueAt(final int rowIndex, final int columnIndex) {
        return Column.values()[columnIndex].getValue(myLabels.get(rowIndex));
      }
    }

    public SelectLabelPanel(final TFSVcs tfsVcs, final WorkspaceInfo workspace) {

        labelsTableModel = new LabelsTableModel();
        labelsTable.setModel(labelsTableModel);
        labelsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        findButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    String owner = ownerTextField.getText().trim();
                    if ("".equals(owner)) {
                        owner = null;
                    }
                    String name = nameTextField.getText().trim();
                    if ("".equals(name)) {
                        name = null;
                    }
                    List<VersionControlLabel> labels =
                            workspace.getServer().getVCS().queryLabels(
                                    name,
                                    "$/",
                                    owner,
                                    false,
                                    null,
                                    null,
                                    false);
                    labelsTableModel.setLabels(labels);
                } catch (TfsException e) {
                    labelsTableModel.setLabels(Collections.<VersionControlLabel>emptyList());
                    // TODO: how to show error message? 
                }
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    public VersionControlLabel getLabel() {
        if (labelsTable.getSelectedRowCount() == 1) {
          return labelsTableModel.getLabels().get(labelsTable.getSelectedRow());
        }
        else {
          return null;
        }
    }
}
