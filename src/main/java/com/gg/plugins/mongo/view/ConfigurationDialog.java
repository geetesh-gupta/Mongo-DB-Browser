/*
 * Copyright (c) 2022. Geetesh Gupta
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
