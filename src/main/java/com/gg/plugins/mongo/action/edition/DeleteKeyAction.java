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

package com.gg.plugins.mongo.action.edition;

import com.gg.plugins.mongo.view.edition.MongoEditionPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;

public class DeleteKeyAction extends AnAction implements DumbAware {

	private final MongoEditionPanel mongoEditionPanel;

	public DeleteKeyAction(MongoEditionPanel mongoEditionPanel) {
		super("Delete This", "Delete the selected node", AllIcons.Actions.DeleteTag);
		registerCustomShortcutSet(KeyEvent.VK_DELETE, KeyEvent.ALT_DOWN_MASK, mongoEditionPanel);
		this.mongoEditionPanel = mongoEditionPanel;
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(mongoEditionPanel.getSelectedNode() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		mongoEditionPanel.removeSelectedKey();
	}
}
