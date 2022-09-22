/*
 * Copyright (c) 2018 David Boissier.
 * Modifications Copyright (c) 2022 Geetesh Gupta.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
