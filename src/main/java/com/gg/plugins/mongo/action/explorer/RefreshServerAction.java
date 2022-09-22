/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.model.MongoDatabase;
import com.gg.plugins.mongo.model.MongoServer;
import com.gg.plugins.mongo.utils.GuiUtils;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RefreshServerAction extends AnAction implements DumbAware {

	private static final Icon CONNECT_ICON = GuiUtils.loadIcon("connector.png", "connector_dark.png");

	private static final Icon REFRESH_ICON = AllIcons.Actions.Refresh;

	private static final String REFRESH_TEXT = "Refresh this server";

	private static final String CONNECT_TEXT = "Connect to this server";

	private final MongoExplorerPanel mongoExplorerPanel;

	public RefreshServerAction(MongoExplorerPanel mongoExplorerPanel) {
		super(REFRESH_TEXT);
		this.mongoExplorerPanel = mongoExplorerPanel;
	}

	@Override
	public void update(@NotNull AnActionEvent event) {
		Object selectedNode = mongoExplorerPanel.getSelectedNode();
		if (selectedNode instanceof MongoServer || selectedNode instanceof MongoDatabase) {
			event.getPresentation().setVisible(true);
			if (selectedNode instanceof MongoServer) {
				MongoServer mongoServer = (MongoServer) selectedNode;

				boolean isLoading = MongoServer.Status.LOADING.equals(mongoServer.getStatus());
				event.getPresentation().setEnabled(!isLoading);

				boolean isConnected = mongoServer.isConnected();
				event.getPresentation().setIcon(isConnected ? REFRESH_ICON : CONNECT_ICON);
				event.getPresentation().setText(isConnected ? REFRESH_TEXT : CONNECT_TEXT);
			} else {
				event.getPresentation().setText("Refresh This Database");
				event.getPresentation().setEnabled(true);
			}
		} else {
			event.getPresentation().setEnabled(false);
			event.getPresentation().setVisible(false);
		}
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		Object selectedNode = mongoExplorerPanel.getSelectedNode();
		if (selectedNode instanceof MongoServer)
			mongoExplorerPanel.openServer((MongoServer) selectedNode);
		else if (selectedNode instanceof MongoDatabase)
			mongoExplorerPanel.loadDatabase((MongoDatabase) selectedNode);
	}
}
