/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import com.gg.plugins.mongo.view.renderer.MongoTableCellRenderer;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellRenderer;
import java.util.List;
import java.util.Set;

public class JsonTableUtils {

	public static ListTableModel buildJsonTable(MongoCollectionResult mongoCollectionResult) {
		List<Document> resultObjects = mongoCollectionResult.getDocuments();
		if (resultObjects.isEmpty()) {
			return null;
		}

		ColumnInfo[] columnInfos = extractColumnNames(resultObjects.get(0));

		return new ListTableModel<>(columnInfos, resultObjects);
	}

	private static ColumnInfo[] extractColumnNames(final Document document) {
		Set<String> keys = document.keySet();
		ColumnInfo[] columnInfos = new ColumnInfo[keys.size()];
		int index = 0;
		for (final String key : keys) {
			columnInfos[index++] = new TableColumnInfo(key);
		}
		return columnInfos;
	}

	private static class TableColumnInfo extends ColumnInfo {
		private static final TableCellRenderer MONGO_TABLE_CELL_RENDERER = new MongoTableCellRenderer();

		private final String key;

		TableColumnInfo(String key) {
			super(key);
			this.key = key;
		}

		@Nullable
		@Override
		public Object valueOf(Object o) {
			Document document = (Document) o;
			return document.get(key);
		}

		@Nullable
		@Override
		public TableCellRenderer getRenderer(Object o) {
			return MONGO_TABLE_CELL_RENDERER;
		}
	}
}
