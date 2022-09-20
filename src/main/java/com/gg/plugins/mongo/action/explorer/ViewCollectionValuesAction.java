/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class ViewCollectionValuesAction extends AnAction implements DumbAware {

	private final MongoExplorerPanel mongoExplorerPanel;

	public ViewCollectionValuesAction(MongoExplorerPanel mongoExplorerPanel) {
		super("View Collection Content", "View collection content", AllIcons.Nodes.DataSchema);
		this.mongoExplorerPanel = mongoExplorerPanel;

		registerCustomShortcutSet(KeyEvent.VK_F4, 0, mongoExplorerPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		//		event.getPresentation().setVisible(mongoExplorerPanel.getSelectedCollection() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		//		mongoExplorerPanel.loadSelectedCollectionValues(mongoExplorerPanel.getSelectedCollection());
	}
}
