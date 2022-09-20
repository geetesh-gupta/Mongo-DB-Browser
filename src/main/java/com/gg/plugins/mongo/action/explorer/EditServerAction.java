/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.model.MongoServer;
import com.gg.plugins.mongo.view.ConfigurationDialog;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

import java.awt.*;
import java.awt.event.KeyEvent;

public class EditServerAction extends AnAction implements DumbAware {
	private final MongoExplorerPanel mongoExplorerPanel;

	public EditServerAction(MongoExplorerPanel mongoExplorerPanel) {
		super("Edit Server", "Edit the Mongo server configuration", AllIcons.Actions.Edit);

		this.mongoExplorerPanel = mongoExplorerPanel;

		registerCustomShortcutSet(KeyEvent.VK_E,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
				mongoExplorerPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(mongoExplorerPanel.getSelectedServer() != null);
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		MongoServer mongoServer = mongoExplorerPanel.getSelectedServer();
		ServerConfiguration configuration = mongoServer.getConfiguration();

		ConfigurationDialog dialog = new ConfigurationDialog(event.getProject(), mongoExplorerPanel, configuration);
		dialog.setTitle("Edit a Mongo Server");
		dialog.show();
		if (!dialog.isOK()) {
			return;
		}
		if (mongoServer.isConnected()) {
			mongoExplorerPanel.openServer(mongoServer);
		}
	}
}
