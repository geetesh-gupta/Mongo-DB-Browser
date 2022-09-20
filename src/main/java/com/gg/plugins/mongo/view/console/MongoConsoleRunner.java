
/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.console;

import com.gg.plugins.mongo.config.MongoConfiguration;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.model.MongoDatabase;
import com.gg.plugins.mongo.service.Notifier;
import com.gg.plugins.mongo.utils.MongoUtils;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.console.ConsoleHistoryController;
import com.intellij.execution.console.ConsoleRootType;
import com.intellij.execution.console.ProcessBackedConsoleExecuteActionHandler;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MongoConsoleRunner extends AbstractConsoleRunnerWithHistory<MongoConsoleView> {

	private static final Key<Boolean> MONGO_SHELL_FILE = Key.create("MONGO_SHELL_FILE");

	private final ServerConfiguration serverConfiguration;

	private final MongoDatabase database;

	private final Notifier notifier;

	public MongoConsoleRunner(@NotNull Project project,
			ServerConfiguration serverConfiguration,
			MongoDatabase database) {
		super(project, "Mongo Shell", "/tmp");
		notifier = Notifier.getInstance(project);
		this.serverConfiguration = serverConfiguration;
		this.database = database;
	}

	@Override
	protected MongoConsoleView createConsoleView() {
		MongoConsoleView res = new MongoConsoleView(getProject());

		PsiFile file = res.getFile();
		assert file.getContext() == null;
		file.putUserData(MONGO_SHELL_FILE, Boolean.TRUE);

		return res;
	}

	@Nullable
	@Override
	protected Process createProcess() throws ExecutionException {
		String shellPath = MongoConfiguration.getInstance(getProject()).getShellPath();
		GeneralCommandLine commandLine = MongoUtils.buildCommandLine(shellPath, serverConfiguration, database);
		notifier.notifyInfo("Running " + commandLine.getCommandLineString());

		return commandLine.createProcess();
	}

	@Override
	protected OSProcessHandler createProcessHandler(Process process) {
		return new ColoredProcessHandler(process, MongoConfiguration.getInstance(getProject()).getShellPath());
	}

	@NotNull
	@Override
	protected ProcessBackedConsoleExecuteActionHandler createExecuteActionHandler() {
		ProcessBackedConsoleExecuteActionHandler handler =
				new ProcessBackedConsoleExecuteActionHandler(getProcessHandler(), false) {
					@Override
					public String getEmptyExecuteAction() {
						return "Mongo.Shell.Execute";
					}
				};
		new ConsoleHistoryController(new ConsoleRootType("Mongo Shell", null) {}, null, getConsoleView()).install();
		return handler;
	}
}
