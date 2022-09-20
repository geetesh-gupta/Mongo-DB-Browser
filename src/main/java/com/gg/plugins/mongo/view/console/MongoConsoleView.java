/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.console;

import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;

public class MongoConsoleView extends LanguageConsoleImpl {
	public MongoConsoleView(Project project) {
		super(project, "Mongo Console", StdFileTypes.PLAIN_TEXT.getLanguage());
	}
}
