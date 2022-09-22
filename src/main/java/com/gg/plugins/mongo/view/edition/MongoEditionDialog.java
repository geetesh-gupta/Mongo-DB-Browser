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

package com.gg.plugins.mongo.view.edition;

import com.gg.plugins.mongo.view.MongoPanel;
import com.gg.plugins.mongo.view.MongoResultPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MongoEditionDialog extends DialogWrapper {

	private final MongoEditionPanel editionPanel;

	private MongoEditionDialog(@Nullable Project project,
			MongoPanel.MongoDocumentOperations operations,
			MongoResultPanel.ActionCallback actionCallback) {
		super(project, true);
		editionPanel = new MongoEditionPanel(operations, actionCallback);
		init();
	}

	public static MongoEditionDialog create(@Nullable Project project,
			MongoPanel.MongoDocumentOperations operations,
			MongoResultPanel.ActionCallback actionCallback) {
		return new MongoEditionDialog(project, operations, actionCallback);
	}

	@Nullable
	@Override
	protected ValidationInfo doValidate() {
		if (!editionPanel.save()) {
			return new ValidationInfo("Unable to save the document", editionPanel);
		}
		return null;
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return editionPanel;
	}

	public MongoEditionDialog initDocument(Document mongoDocument) {
		String dialogTitle = "New Document";
		if (mongoDocument != null) {
			dialogTitle = "Edition";
		}
		setTitle(dialogTitle);
		editionPanel.updateEditionTree(mongoDocument);
		return this;
	}
}
