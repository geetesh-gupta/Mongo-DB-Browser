/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.console;

import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.openapi.project.Project;

import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;

public class MongoConsoleView extends LanguageConsoleImpl {
	public MongoConsoleView(Project project) {
		super(project, "Mongo Console", PLAIN_TEXT.getLanguage());
	}
}
