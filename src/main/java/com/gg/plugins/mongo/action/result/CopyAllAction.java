/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoResultPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

public class CopyAllAction extends AnAction implements DumbAware {

	private final MongoResultPanel mongoResultPanel;

	public CopyAllAction(MongoResultPanel mongoResultPanel) {
		super("Copy Results", "Copy results to clipboard", AllIcons.Actions.Copy);
		this.mongoResultPanel = mongoResultPanel;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		CopyPasteManager.getInstance().setContents(new StringSelection(mongoResultPanel.getStringifiedResult()));
	}
}
