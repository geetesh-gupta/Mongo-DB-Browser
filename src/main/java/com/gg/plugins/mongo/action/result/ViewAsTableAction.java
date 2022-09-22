/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoPanel;
import com.gg.plugins.mongo.view.MongoResultPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ViewAsTableAction extends AnAction implements DumbAware {
	private final MongoPanel mongoPanel;

	public ViewAsTableAction(MongoPanel mongoPanel) {
		super("View as Table", "See results as table", AllIcons.Nodes.DataTables);
		this.mongoPanel = mongoPanel;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		mongoPanel.setViewMode(MongoResultPanel.ViewMode.TABLE);
	}
}
