/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.config.MongoConfiguration;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.model.MongoDatabase;
import com.gg.plugins.mongo.utils.GuiUtils;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.gg.plugins.mongo.view.console.MongoConsoleRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;

public class MongoConsoleAction extends AnAction implements DumbAware {

	private final MongoExplorerPanel mongoExplorerPanel;

	public MongoConsoleAction(MongoExplorerPanel mongoExplorerPanel) {
		super("Mongo Shell...", "Select a database to enable it", GuiUtils.loadIcon("toolConsole.png"));
		this.mongoExplorerPanel = mongoExplorerPanel;
	}

	@Override
	public void update(AnActionEvent e) {
		final Project project = e.getData(PlatformDataKeys.PROJECT);
		Object mongoDatabase = mongoExplorerPanel.getSelectedNode();

		if (project != null && mongoDatabase instanceof MongoDatabase) {
			e.getPresentation().setEnabled(true);
			ServerConfiguration configuration = mongoExplorerPanel.getServerConfiguration(mongoDatabase);
			e.getPresentation().setVisible(isShellPathSet(project) && isSingleServerInstance(configuration));
		} else {
			e.getPresentation().setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getData(PlatformDataKeys.PROJECT);
		assert project != null;

		runShell(project);
	}

	private void runShell(Project project) {
		Object selectedNode = mongoExplorerPanel.getSelectedNode();
		ServerConfiguration serverConfiguration = mongoExplorerPanel.getServerConfiguration(selectedNode);
		MongoConsoleRunner consoleRunner =
				new MongoConsoleRunner(project, serverConfiguration, (MongoDatabase) selectedNode);
		try {
			consoleRunner.initAndRun();
		} catch (ExecutionException e1) {
			throw new RuntimeException(e1);
		}
	}

	private boolean isShellPathSet(Project project) {
		MongoConfiguration configuration = MongoConfiguration.getInstance(project);
		return configuration != null && StringUtils.isNotBlank(configuration.getShellPath());
	}

	private boolean isSingleServerInstance(ServerConfiguration serverConfiguration) {
		return serverConfiguration != null && serverConfiguration.isSingleServer();
	}
}
