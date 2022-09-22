/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoResultPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

public class CopyNodeAction extends AnAction implements DumbAware {

	private final MongoResultPanel mongoResultPanel;

	public CopyNodeAction(MongoResultPanel mongoResultPanel) {
		super("Copy...", "Copy selected node to clipboard", null);
		this.mongoResultPanel = mongoResultPanel;

		registerCustomShortcutSet(KeyEvent.VK_C,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				mongoResultPanel);
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setVisible(mongoResultPanel.getSelectedNode() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		CopyPasteManager.getInstance()
		                .setContents(new StringSelection(mongoResultPanel.getSelectedNodeStringifiedValue()));
	}
}
