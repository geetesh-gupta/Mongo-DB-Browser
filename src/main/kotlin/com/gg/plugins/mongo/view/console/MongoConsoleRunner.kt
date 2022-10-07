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
package com.gg.plugins.mongo.view.console

import com.gg.plugins.mongo.config.MongoConfiguration
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.model.MongoDatabase
import com.gg.plugins.mongo.service.Notifier
import com.gg.plugins.mongo.utils.MongoUtils
import com.intellij.execution.ExecutionException
import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.console.ConsoleRootType
import com.intellij.execution.console.ProcessBackedConsoleExecuteActionHandler
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.runners.AbstractConsoleRunnerWithHistory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class MongoConsoleRunner(
    project: Project,
    serverConfiguration: ServerConfiguration,
    database: MongoDatabase
) : AbstractConsoleRunnerWithHistory<MongoConsoleView>(project, "Mongo Shell", "/tmp") {
    private val serverConfiguration: ServerConfiguration
    private val database: MongoDatabase
    private val notifier: Notifier

    init {
        notifier = Notifier.getInstance(project)
        this.serverConfiguration = serverConfiguration
        this.database = database
    }

    override fun createConsoleView(): MongoConsoleView {
        val res = MongoConsoleView(project)
        val file = res.file
        assert(file.context == null)
        file.putUserData(MONGO_SHELL_FILE, java.lang.Boolean.TRUE)
        return res
    }

    @Throws(ExecutionException::class)
    override fun createProcess(): Process {
        val shellPath: String? = MongoConfiguration.getInstance(project).shellPath
        val commandLine = MongoUtils.buildCommandLine(shellPath, serverConfiguration, database)
        notifier.notifyInfo("Running " + commandLine.commandLineString)
        return commandLine.createProcess()
    }

    override fun createProcessHandler(process: Process): OSProcessHandler {
        return KillableColoredProcessHandler(process, MongoConfiguration.getInstance(project).shellPath)
    }

    override fun createExecuteActionHandler(): ProcessBackedConsoleExecuteActionHandler {
        val handler = ProcessBackedConsoleExecuteActionHandler(processHandler, false)
        ConsoleHistoryController(object : ConsoleRootType("Mongo Shell", null) {}, null, consoleView).install()
        return handler
    }

    companion object {
        private val MONGO_SHELL_FILE = Key.create<Boolean>("MONGO_SHELL_FILE")
    }
}