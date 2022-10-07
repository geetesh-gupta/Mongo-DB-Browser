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
import com.gg.plugins.mongo.model.MongoServer
import com.gg.plugins.mongo.view.MongoExplorerPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import java.awt.Toolkit
import java.awt.event.KeyEvent

class DuplicateServerAction(private val mongoExplorerPanel: MongoExplorerPanel) :
    AnAction("Duplicate...", "Duplicate server configuration", AllIcons.Actions.Copy), DumbAware {
    init {
        registerCustomShortcutSet(
            KeyEvent.VK_D,
            Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx,
            mongoExplorerPanel
        )
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isVisible = mongoExplorerPanel.selectedNode is MongoServer
    }

    override fun actionPerformed(e: AnActionEvent) {
        val configuration = mongoExplorerPanel.getServerConfiguration(
            mongoExplorerPanel.selectedNode
        )
        val clonedConfiguration = configuration!!.clone()
        clonedConfiguration.label = "Copy of " + clonedConfiguration.label
        val mongoConfiguration: MongoConfiguration =
            MongoConfiguration.getInstance(e.project!!)
        mongoConfiguration.addServerConfiguration(clonedConfiguration)
        mongoExplorerPanel.addConfiguration(clonedConfiguration)
    }
}