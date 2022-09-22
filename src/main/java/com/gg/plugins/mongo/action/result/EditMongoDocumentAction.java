/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoResultPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;

public class EditMongoDocumentAction extends AnAction implements DumbAware {

	private final MongoResultPanel resultPanel;

	public EditMongoDocumentAction(MongoResultPanel resultPanel) {
		super("Edit", "Edit mongo document", AllIcons.Actions.Edit);
		this.resultPanel = resultPanel;

		registerCustomShortcutSet(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(), resultPanel);
	}

	@Override
	public void update(@NotNull AnActionEvent event) {
		super.update(event);
		event.getPresentation().setEnabled(resultPanel.isSelectedNodeId());
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		resultPanel.editSelectedMongoDocument();
	}
}
