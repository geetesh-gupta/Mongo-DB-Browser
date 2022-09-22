/*
 * Copyright (c) 2018 David Boissier.
 * Modifications Copyright (c) 2022 Geetesh Gupta.
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

package com.gg.plugins.mongo.view.table;

import com.gg.plugins.mongo.model.JsonTreeNode;
import com.intellij.ui.treeStructure.treetable.TreeTable;

import javax.swing.*;
import java.awt.*;

public class MongoValueCellEditor extends DefaultCellEditor {

	public MongoValueCellEditor() {
		super(new JTextField());
	}

	@Override
	public Object getCellEditorValue() {
		return ((JTextField) getComponent()).getText();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		JTextField stringEditor = (JTextField) getComponent();
		final JsonTreeNode jsonNode =
				(JsonTreeNode) ((TreeTable) table).getTree().getPathForRow(row).getLastPathComponent();

		stringEditor.setText(String.valueOf(jsonNode.getDescriptor().getValue()));

		return stringEditor;
	}
}
