/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

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
		//		MongoServer mongoServer = mongoExplorerPanel.getSelectedServer();
		//		event.getPresentation().setVisible(mongoServer != null);
		//		if (mongoServer == null) {
		//			return;
		//		}
		//
		//		boolean isLoading = MongoServer.Status.LOADING.equals(mongoServer.getStatus());
		//		event.getPresentation().setEnabled(!isLoading);
		//
		//		boolean isConnected = mongoServer.isConnected();
		//		event.getPresentation().setIcon(isConnected ? REFRESH_ICON : CONNECT_ICON);
		//		event.getPresentation().setText(isConnected ? REFRESH_TEXT : CONNECT_TEXT);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		//		MongoServer selectedMongoServer = mongoExplorerPanel.getSelectedServer();
		//		mongoExplorerPanel.openServer(selectedMongoServer);
	}
}
