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

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.view.MongoPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GoToMongoDocumentAction extends AnAction implements DumbAware {

	private final MongoPanel mongoPanel;

	public GoToMongoDocumentAction(MongoPanel mongoPanel) {
		super("View Reference");
		this.mongoPanel = mongoPanel;

		registerCustomShortcutSet(KeyEvent.VK_B,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
				this.mongoPanel);
	}

	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(mongoPanel.getResultPanel().getSelectedDBRef() != null);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		mongoPanel.goToReferencedDocument();
	}

}
