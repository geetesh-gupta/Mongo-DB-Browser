/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.renderer;

import com.gg.plugins.mongo.model.JsonTreeNode;
import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor;
import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MongoKeyCellRenderer extends ColoredTreeCellRenderer {

	@Override
	public void customizeCellRenderer(@NotNull JTree tree,
			Object value,
			boolean selected,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
		MongoNodeDescriptor descriptor = ((JsonTreeNode) value).getDescriptor();

		descriptor.renderNode(this);
	}
}
