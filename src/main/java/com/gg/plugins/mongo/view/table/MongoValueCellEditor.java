/*
 * Copyright (c) 2022. Geetesh Gupta
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
