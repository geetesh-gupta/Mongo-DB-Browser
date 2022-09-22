/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ExecuteQuery extends AnAction implements DumbAware {
	private final MongoPanel mongoPanel;

	public ExecuteQuery(MongoPanel mongoPanel) {
		super("Execute Query", "Execute query with options", AllIcons.Actions.Execute);
		this.mongoPanel = mongoPanel;

		registerCustomShortcutSet(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK, mongoPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(mongoPanel.getCurrentWayPoint() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		mongoPanel.executeQuery();
	}
}
