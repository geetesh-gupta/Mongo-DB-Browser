/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GoToMongoDocumentAction extends AnAction implements DumbAware {

	private final MongoPanel mongoPanel;

	public GoToMongoDocumentAction(MongoPanel mongoPanel) {
		super("View Reference");
		this.mongoPanel = mongoPanel;

		registerCustomShortcutSet(KeyEvent.VK_B,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				this.mongoPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(mongoPanel.getResultPanel().getSelectedDBRef() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		mongoPanel.goToReferencedDocument();
	}

}
