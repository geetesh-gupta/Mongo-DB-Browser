/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.edition;

import com.gg.plugins.mongo.view.edition.AddValueDialog;
import com.gg.plugins.mongo.view.edition.MongoEditionPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class AddValueAction extends AnAction implements DumbAware {

	private final MongoEditionPanel mongoEditionPanel;

	public AddValueAction(MongoEditionPanel mongoEditionPanel) {
		super("Add a Value", "Add a value", AllIcons.General.Add);
		this.mongoEditionPanel = mongoEditionPanel;
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(mongoEditionPanel.canAddValue());
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		AddValueDialog dialog = AddValueDialog.createDialog(mongoEditionPanel);
		dialog.show();

		if (!dialog.isOK()) {
			return;
		}

		mongoEditionPanel.addValue(dialog.getValue());
	}
}