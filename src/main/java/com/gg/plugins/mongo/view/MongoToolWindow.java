/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.GuiUtils;

import javax.swing.*;
import java.net.URL;

public class MongoToolWindow {

	private static final URL pluginSettingsUrl = GuiUtils.class.getResource("/general/add.png");

	private JPanel rootPanel;

	private JPanel toolBarPanel;

	private JPanel containerPanel;

	private JPanel MongoToolWindowContent;

	public MongoToolWindow(ToolWindow toolWindow) {
	}

	public JPanel getContent() {
		return MongoToolWindowContent;
	}

}