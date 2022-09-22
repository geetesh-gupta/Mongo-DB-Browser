/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.nodedescriptor;

import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;

public class MongoResultDescriptor implements MongoNodeDescriptor {

	private final String formattedText;

	public MongoResultDescriptor() {
		this("");
	}

	public MongoResultDescriptor(String collectionName) {
		formattedText = String.format("results of '%s'", collectionName);
	}

	public void renderValue(ColoredTableCellRenderer cellRenderer, boolean isNodeExpanded) {
	}

	public void renderNode(ColoredTreeCellRenderer cellRenderer) {

	}

	public String getKey() {
		return formattedText;
	}

	@Override
	public String getFormattedValue() {
		return "";
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public void setValue(Object value) {

	}

	@Override
	public String pretty() {
		return formattedText;
	}
}
