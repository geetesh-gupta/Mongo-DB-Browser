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

public class NavigateBackwardAction extends AnAction implements DumbAware {
	private final MongoPanel mongoPanel;

	public NavigateBackwardAction(MongoPanel mongoPanel) {
		super("Navigate Backward", "Navigate backward", AllIcons.Actions.Back);
		this.mongoPanel = mongoPanel;
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(mongoPanel.hasNavigationHistory());
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		mongoPanel.navigateBackward();
	}
}
