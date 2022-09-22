/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoResultPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class DeleteMongoDocumentAction extends AnAction implements DumbAware {

	private final MongoResultPanel resultPanel;

	public DeleteMongoDocumentAction(MongoResultPanel resultPanel) {
		super("Delete", "Delete this document", AllIcons.Actions.DeleteTag);
		this.resultPanel = resultPanel;

		if (SystemInfo.isMac) {
			registerCustomShortcutSet(KeyEvent.VK_BACK_SPACE, 0, resultPanel);
		} else {
			registerCustomShortcutSet(KeyEvent.VK_DELETE, 0, resultPanel);
		}
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(resultPanel.isSelectedNodeId());
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		resultPanel.deleteSelectedMongoDocument();
	}
}
