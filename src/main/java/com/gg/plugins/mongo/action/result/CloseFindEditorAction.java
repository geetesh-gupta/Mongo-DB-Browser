/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoPanel;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class CloseFindEditorAction extends CloseTabToolbarAction {
	private final MongoPanel mongoPanel;

	public CloseFindEditorAction(MongoPanel mongoPanel) {
		getTemplatePresentation().setIcon(AllIcons.Actions.Close);
		registerCustomShortcutSet(KeyEvent.VK_ESCAPE, 0, mongoPanel);
		this.mongoPanel = mongoPanel;
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(false);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		mongoPanel.closeFindEditor();
	}
}