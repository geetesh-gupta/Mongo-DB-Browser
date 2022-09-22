/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.config.MongoConfiguration;
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
import java.util.Objects;

public class EditServerAction extends AnAction implements DumbAware {
	private final MongoExplorerPanel mongoExplorerPanel;

	public EditServerAction(MongoExplorerPanel mongoExplorerPanel) {
		super("Edit Server", "Edit the Mongo server configuration", AllIcons.Actions.Edit);

		this.mongoExplorerPanel = mongoExplorerPanel;

		registerCustomShortcutSet(KeyEvent.VK_E,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				mongoExplorerPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation()
		     .setVisible(mongoExplorerPanel.getSelectedNode() != null &&
		                 mongoExplorerPanel.getSelectedNode() instanceof MongoServer);
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		MongoServer selectedNode = (MongoServer) mongoExplorerPanel.getSelectedNode();
		ServerConfiguration serverConfiguration = mongoExplorerPanel.getServerConfiguration(selectedNode);

		ConfigurationDialog dialog =
				new ConfigurationDialog(event.getProject(), mongoExplorerPanel, serverConfiguration);
		dialog.setTitle("Edit a Mongo Server");
		dialog.show();
		if (!dialog.isOK()) {
			return;
		}

		MongoConfiguration mongoConfiguration =
				MongoConfiguration.getInstance(Objects.requireNonNull(event.getProject()));
		mongoConfiguration.updateServerConfiguration(serverConfiguration);
		if (selectedNode.isConnected()) {
			mongoExplorerPanel.openServer(selectedNode);
		}
	}
}
