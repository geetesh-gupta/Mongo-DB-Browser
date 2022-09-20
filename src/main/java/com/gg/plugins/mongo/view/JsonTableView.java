/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view;

import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;

class JsonTableView extends TableView {

	@SuppressWarnings("unchecked")
	JsonTableView(ListTableModel tableModel) {
		super(tableModel);
	}
}
