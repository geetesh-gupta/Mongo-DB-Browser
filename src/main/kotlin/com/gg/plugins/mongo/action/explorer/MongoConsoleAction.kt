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
package com.gg.plugins.mongo.action.explorer

import com.gg.plugins.mongo.config.MongoConfiguration
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.model.MongoDatabase
import com.gg.plugins.mongo.utils.GuiUtils
import com.gg.plugins.mongo.view.MongoExplorerPanel
import com.gg.plugins.mongo.view.console.MongoConsoleRunner
import com.intellij.execution.ExecutionException
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import org.apache.commons.lang.StringUtils

class MongoConsoleAction(private val mongoExplorerPanel: MongoExplorerPanel) :
    AnAction("Mongo Shell...", "Select a database to enable it", GuiUtils.loadIcon("toolConsole.png")), DumbAware {
    override fun update(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT)
        val mongoDatabase = mongoExplorerPanel.selectedNode
        if (project != null && mongoDatabase is MongoDatabase) {
            e.presentation.isEnabled = true
            val configuration = mongoExplorerPanel.getServerConfiguration(mongoDatabase)
            e.presentation.isVisible = isShellPathSet(project) && isSingleServerInstance(configuration)
        } else {
            e.presentation.isEnabled = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT)!!
        runShell(project)
    }

    private fun runShell(project: Project?) {
        val selectedNode = mongoExplorerPanel.selectedNode
        val serverConfiguration = mongoExplorerPanel.getServerConfiguration(selectedNode)
        val consoleRunner = MongoConsoleRunner(project!!, serverConfiguration!!, selectedNode as MongoDatabase)
        try {
            consoleRunner.initAndRun()
        } catch (e1: ExecutionException) {
            throw RuntimeException(e1)
        }
    }

    private fun isShellPathSet(project: Project): Boolean {
        val configuration: MongoConfiguration = MongoConfiguration.getInstance(project)
        return StringUtils.isNotBlank(configuration.shellPath)
    }

    private fun isSingleServerInstance(serverConfiguration: ServerConfiguration?): Boolean {
        return serverConfiguration != null && serverConfiguration.isSingleServer()
    }
}