/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.view.MongoConfigurable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OpenPluginSettingsAction extends AnAction implements DumbAware {

	public OpenPluginSettingsAction() {
		super("Mongo General Settings", "Edit the Mongo settings for the current project", AllIcons.General.Settings);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {
		showSettingsFor(getProject(event));
	}

	private static void showSettingsFor(Project project) {
		ShowSettingsUtil.getInstance().showSettingsDialog(project, MongoConfigurable.PLUGIN_SETTINGS_NAME);
	}

	private static Project getProject(AnActionEvent event) {
		DataContext dataContext = event.getDataContext();
		return PlatformDataKeys.PROJECT.getData(dataContext);
	}
}

