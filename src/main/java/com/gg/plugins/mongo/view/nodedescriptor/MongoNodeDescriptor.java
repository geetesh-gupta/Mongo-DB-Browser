/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.nodedescriptor;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;

public interface MongoNodeDescriptor {

	int MAX_LENGTH = 150;

	void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded);

	void renderNode(ColoredTreeCellRenderer cellRenderer);

	String getKey();

	String getFormattedValue();

	Object getValue();

	void setValue(Object value);

	String pretty();
}
