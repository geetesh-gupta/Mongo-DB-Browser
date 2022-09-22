/*
 * Copyright (c) 2022. Geetesh Gupta
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
