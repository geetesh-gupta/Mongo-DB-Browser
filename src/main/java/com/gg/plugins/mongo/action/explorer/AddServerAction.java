/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.config.MongoConfiguration;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.view.ConfigurationDialog;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

import java.util.Objects;

public class AddServerAction extends AnAction implements DumbAware {
	private final MongoExplorerPanel mongoExplorerPanel;

	public AddServerAction(MongoExplorerPanel mongoExplorerPanel) {
		super("Add Server", "Add a Mongo server configuration", AllIcons.General.Add);

		this.mongoExplorerPanel = mongoExplorerPanel;
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		ServerConfiguration serverConfiguration = ServerConfiguration.byDefault();

		ConfigurationDialog dialog =
				new ConfigurationDialog(event.getProject(), mongoExplorerPanel, serverConfiguration);
		dialog.setTitle("Add a Mongo Server");
		dialog.show();
		if (!dialog.isOK()) {
			return;
		}

		MongoConfiguration mongoConfiguration =
				MongoConfiguration.getInstance(Objects.requireNonNull(event.getProject()));
		mongoConfiguration.addServerConfiguration(serverConfiguration);
		mongoExplorerPanel.addConfiguration(serverConfiguration);
	}
}
