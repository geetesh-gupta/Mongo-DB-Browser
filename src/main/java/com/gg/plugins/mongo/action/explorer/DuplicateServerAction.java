/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

import java.awt.*;
import java.awt.event.KeyEvent;

public class DuplicateServerAction extends AnAction implements DumbAware {

	private final MongoExplorerPanel mongoExplorerPanel;

	public DuplicateServerAction(MongoExplorerPanel mongoExplorerPanel) {
		super("Duplicate...", "Duplicate Server Configuration", AllIcons.Actions.Copy);
		this.mongoExplorerPanel = mongoExplorerPanel;

		registerCustomShortcutSet(KeyEvent.VK_D,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
				mongoExplorerPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(mongoExplorerPanel.getSelectedServer() != null);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		ServerConfiguration configuration = mongoExplorerPanel.getSelectedServer().getConfiguration();
		ServerConfiguration clonedConfiguration = configuration.clone();
		clonedConfiguration.setLabel("Copy of " + clonedConfiguration.getLabel());
		mongoExplorerPanel.addConfiguration(clonedConfiguration);
	}
}
