/*
 * Copyright (c) 2018 David Boissier.
 * Modifications Copyright (c) 2022 Geetesh Gupta.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gg.plugins.mongo.action.explorer;

import com.gg.plugins.mongo.model.MongoCollection;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class ViewCollectionValuesAction extends AnAction implements DumbAware {

	private final MongoExplorerPanel mongoExplorerPanel;

	public ViewCollectionValuesAction(MongoExplorerPanel mongoExplorerPanel) {
		super("View Collection Content", "View collection content", AllIcons.Nodes.DataSchema);
		this.mongoExplorerPanel = mongoExplorerPanel;

		registerCustomShortcutSet(KeyEvent.VK_F4, 0, mongoExplorerPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		Object selectedNode = mongoExplorerPanel.getSelectedNode();
		event.getPresentation().setVisible(selectedNode instanceof MongoCollection);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		mongoExplorerPanel.loadSelectedCollectionValues((MongoCollection) mongoExplorerPanel.getSelectedNode());
	}
}
