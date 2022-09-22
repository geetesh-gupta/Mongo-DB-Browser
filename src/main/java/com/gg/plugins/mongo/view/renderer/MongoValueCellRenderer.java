/*
 * Copyright (c) 2022. Geetesh Gupta
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
