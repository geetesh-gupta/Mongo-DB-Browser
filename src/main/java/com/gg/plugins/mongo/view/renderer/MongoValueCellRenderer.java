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

package com.gg.plugins.mongo.view.renderer;

import com.gg.plugins.mongo.model.JsonTreeNode;
import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;

public class MongoValueCellRenderer extends ColoredTableCellRenderer {

	@Override
	protected void customizeCellRenderer(@NotNull JTable table,
			Object value,
			boolean selected,
			boolean hasFocus,
			int row,
			int column) {

		TreeTableTree tree = ((TreeTable) table).getTree();
		TreePath pathForRow = tree.getPathForRow(row);

		final JsonTreeNode node = (JsonTreeNode) pathForRow.getLastPathComponent();

		MongoNodeDescriptor descriptor = node.getDescriptor();
		descriptor.renderValue(this, tree.isExpanded(pathForRow));

	}
}
