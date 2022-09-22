/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.renderer;

import com.gg.plugins.mongo.view.style.StyleAttributesProvider;
import com.intellij.ui.ColoredTableCellRenderer;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MongoTableCellRenderer extends ColoredTableCellRenderer {

	@Override
	protected void customizeCellRenderer(@NotNull JTable table,
			Object value,
			boolean selected,
			boolean hasFocus,
			int row,
			int column) {

		if (value == null) {
			append("null", StyleAttributesProvider.getNullAttribute());
		} else {
			if (value instanceof Number) {
				append(String.valueOf(value), StyleAttributesProvider.getNumberAttribute());
			} else if (value instanceof Boolean) {
				append(String.valueOf(value), StyleAttributesProvider.getBooleanAttribute());
			} else if (value instanceof Document) {
				append(((Document) value).toJson(), StyleAttributesProvider.getDocumentAttribute());
			} else if (value instanceof ObjectId) {
				append(String.valueOf(value), StyleAttributesProvider.getObjectIdAttribute());
			} else {
				append(String.valueOf(value), StyleAttributesProvider.getStringAttribute());
			}
		}
	}
}
