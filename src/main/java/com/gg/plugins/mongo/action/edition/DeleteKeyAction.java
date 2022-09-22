/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.edition;

import com.gg.plugins.mongo.view.edition.MongoEditionPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class DeleteKeyAction extends AnAction implements DumbAware {

	private final MongoEditionPanel mongoEditionPanel;

	public DeleteKeyAction(MongoEditionPanel mongoEditionPanel) {
		super("Delete This", "Delete the selected node", AllIcons.Actions.DeleteTag);
		registerCustomShortcutSet(KeyEvent.VK_DELETE, KeyEvent.ALT_DOWN_MASK, mongoEditionPanel);
		this.mongoEditionPanel = mongoEditionPanel;
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(mongoEditionPanel.getSelectedNode() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		mongoEditionPanel.removeSelectedKey();
	}
}
