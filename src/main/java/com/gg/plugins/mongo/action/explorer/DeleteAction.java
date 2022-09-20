/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.SystemInfo;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class DeleteAction extends AnAction implements DumbAware {
	private final MongoExplorerPanel mongoExplorerPanel;

	public DeleteAction(MongoExplorerPanel mongoExplorerPanel) {
		super("Delete...", "Delete selected item", null);

		this.mongoExplorerPanel = mongoExplorerPanel;

		if (SystemInfo.isMac) {
			registerCustomShortcutSet(KeyEvent.VK_BACK_SPACE, 0, mongoExplorerPanel);
		} else {
			registerCustomShortcutSet(KeyEvent.VK_DELETE, 0, mongoExplorerPanel);
		}
	}

	@Override
	public void update(AnActionEvent event) {
		//		event.getPresentation().setVisible(mongoExplorerPanel.getSelectedItem() != null);
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		//		Object selectedItem = mongoExplorerPanel.getSelectedItem();
		//
		//		if (selectedItem instanceof MongoServer) {
		//			MongoServer mongoServer = (MongoServer) selectedItem;
		//			deleteItem("server", mongoServer.getLabel(), () -> mongoExplorerPanel.removeSelectedServer
		//			(mongoServer));
		//			return;
		//		}
		//
		//		if (selectedItem instanceof MongoDatabase) {
		//			MongoDatabase mongoDatabase = (MongoDatabase) selectedItem;
		//			deleteItem("database",
		//					mongoDatabase.getName(),
		//					() -> mongoExplorerPanel.removeSelectedDatabase(mongoDatabase));
		//			return;
		//		}
		//
		//		if (selectedItem instanceof MongoCollection) {
		//			MongoCollection mongoCollection = (MongoCollection) selectedItem;
		//			deleteItem("collection",
		//					mongoCollection.getName(),
		//					() -> mongoExplorerPanel.removeSelectedCollection(mongoCollection));
		//		}
	}

	private void deleteItem(String itemTypeLabel, String itemLabel, Runnable deleteOperation) {
		int result = JOptionPane.showConfirmDialog(null,
				String.format("Do you REALLY want to remove the '%s' %s?", itemLabel, itemTypeLabel),
				"Warning",
				JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			deleteOperation.run();
		}
	}
}
