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
