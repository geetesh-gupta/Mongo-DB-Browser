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

package com.gg.plugins.mongo.view;

import com.gg.plugins.mongo.config.ConfigurationException;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.service.MongoService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigurationDialog extends DialogWrapper {

	private final Project project;

	private final MongoService mongoService;

	private final ServerConfiguration configuration;

	private ServerConfigurationPanel serverConfigurationPanel;

	public ConfigurationDialog(Project project, MongoExplorerPanel parent, ServerConfiguration configuration) {
		super(parent, true);
		this.project = project;
		this.mongoService = MongoService.getInstance(project);
		this.configuration = configuration;

		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		serverConfigurationPanel = new ServerConfigurationPanel(project, mongoService);
		serverConfigurationPanel.loadConfigurationData(configuration);
		return serverConfigurationPanel;
	}

	@Override
	protected void doOKAction() {
		try {
			serverConfigurationPanel.applyConfigurationData(configuration);
			super.doOKAction();
		} catch (ConfigurationException confEx) {
			serverConfigurationPanel.setErrorMessage(confEx.getMessage());
		}
	}
}
