/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.edition;

import com.gg.plugins.mongo.view.edition.AddKeyDialog;
import com.gg.plugins.mongo.view.edition.MongoEditionPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class AddKeyAction extends AnAction implements DumbAware {

	private final MongoEditionPanel mongoEditionPanel;

	public AddKeyAction(MongoEditionPanel mongoEditionPanel) {
		super("Add a Key", "Add a key", AllIcons.General.Add);
		registerCustomShortcutSet(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK, mongoEditionPanel);
		this.mongoEditionPanel = mongoEditionPanel;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		AddKeyDialog dialog = AddKeyDialog.createDialog(mongoEditionPanel);
		dialog.show();

		if (!dialog.isOK()) {
			return;
		}

		mongoEditionPanel.addKey(dialog.getKey(), dialog.getValue());
	}
}
