/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.config.MongoConfiguration;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.model.MongoTreeNodeEnum;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class DuplicateServerAction extends AnAction implements DumbAware {

	private final MongoExplorerPanel mongoExplorerPanel;

	public DuplicateServerAction(MongoExplorerPanel mongoExplorerPanel) {
		super("Duplicate...", "Duplicate server configuration", AllIcons.Actions.Copy);
		this.mongoExplorerPanel = mongoExplorerPanel;

		registerCustomShortcutSet(KeyEvent.VK_D,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				mongoExplorerPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation()
		     .setVisible(mongoExplorerPanel.getSelectedNode() != null &&
		                 mongoExplorerPanel.getSelectedNode().getType() == MongoTreeNodeEnum.MongoServer);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		ServerConfiguration configuration =
				mongoExplorerPanel.getServerConfiguration(mongoExplorerPanel.getSelectedNode());
		ServerConfiguration clonedConfiguration = configuration.clone();
		clonedConfiguration.setLabel("Copy of " + clonedConfiguration.getLabel());

		MongoConfiguration mongoConfiguration = MongoConfiguration.getInstance(Objects.requireNonNull(e.getProject()));
		mongoConfiguration.addServerConfiguration(clonedConfiguration);
		mongoExplorerPanel.addConfiguration(clonedConfiguration);
	}
}
