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

import com.gg.plugins.mongo.model.MongoCollection
import com.gg.plugins.mongo.view.MongoExplorerPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import java.awt.event.KeyEvent

class ViewCollectionValuesAction(private val mongoExplorerPanel: MongoExplorerPanel) :
    AnAction("View Collection Content", "View collection content", AllIcons.Nodes.DataSchema), DumbAware {
    init {
        registerCustomShortcutSet(KeyEvent.VK_F4, 0, mongoExplorerPanel)
    }

    override fun update(event: AnActionEvent) {
        val selectedNode = mongoExplorerPanel.selectedNode
        event.presentation.isVisible = selectedNode is MongoCollection
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        mongoExplorerPanel.loadSelectedCollectionValues(mongoExplorerPanel.selectedNode as MongoCollection)
    }
}